package com.mapmory.shared.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGAffineTransformMakeTranslation
import platform.UIKit.UIView

private object ScreenNavigationInfo : NavigationEventInfo()

@OptIn(InternalComposeUiApi::class, BetaInteropApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNavigationGestures(
    onBackStarted: () -> Boolean,
    onBackCancelled: () -> Unit,
    onForwardStarted: () -> Boolean,
    onForwardCancelled: () -> Unit,
) {
    val dispatcher = checkNotNull(LocalCompatNavigationEventDispatcherOwner.current)
        .navigationEventDispatcher
    val viewController = LocalUIViewController.current
    val scope = rememberCoroutineScope()
    val motion = LocalNavigationGestureMotion.current
    var preparation by remember { mutableStateOf<Job?>(null) }
    var destinationReady by remember { mutableStateOf(false) }
    var pendingProgress by remember { mutableStateOf(0f) }
    val progress = remember { Animatable(0f) }
    var direction by remember { mutableStateOf(0f) }
    var snapshotWidth by remember { mutableStateOf(0.0) }
    var snapshot by remember { mutableStateOf<UIView?>(null) }
    var navigationStarted by remember { mutableStateOf(false) }
    var settling by remember { mutableStateOf(false) }
    var translatedView by remember { mutableStateOf<UIView?>(null) }
    val latestBackStarted by rememberUpdatedState(onBackStarted)
    val latestBackCancelled by rememberUpdatedState(onBackCancelled)
    val latestForwardStarted by rememberUpdatedState(onForwardStarted)
    val latestForwardCancelled by rememberUpdatedState(onForwardCancelled)

    fun removeSnapshot() {
        preparation?.cancel()
        preparation = null
        destinationReady = false
        pendingProgress = 0f
        motion.isActive = false
        translatedView?.setTransform(CGAffineTransformMakeTranslation(0.0, 0.0))
        translatedView = null
        snapshot?.removeFromSuperview()
        snapshot = null
        snapshotWidth = 0.0
        direction = 0f
        navigationStarted = false
        settling = false
    }

    fun beginGesture(nextDirection: Float, navigate: () -> Boolean) {
        if (navigationStarted || settling) return
        removeSnapshot()
        val rootView = viewController.view
        val parent = rootView.superview ?: return
        val captured = rootView.snapshotViewAfterScreenUpdates(false) ?: return
        captured.userInteractionEnabled = false
        snapshotWidth = rootView.bounds.useContents { size.width }
        direction = nextDirection
        snapshot = captured
        if (nextDirection < 0f) {
            // Keep the current screen stationary underneath the live destination.
            // The restored forward screen enters from the right, covering this snapshot.
            captured.setFrame(rootView.frame)
            parent.insertSubview(captured, belowSubview = rootView)
            translatedView = rootView
            rootView.setTransform(CGAffineTransformMakeTranslation(snapshotWidth, 0.0))
        } else {
            captured.setFrame(rootView.bounds)
            rootView.addSubview(captured)
        }
        motion.isActive = true
        navigationStarted = navigate()
        if (!navigationStarted) removeSnapshot()
        if (navigationStarted) preparation = scope.launch {
            progress.snapTo(0f)
            // Navigation changes state synchronously, but Compose draws the destination later.
            // Keep the outgoing snapshot still until the destination has had a render frame.
            withFrameNanos { }
            withFrameNanos { }
            destinationReady = true
            if (!settling) progress.snapTo(pendingProgress)
        }
    }

    fun updateGesture(event: NavigationEvent) {
        if (!navigationStarted || settling) return
        pendingProgress = event.progress.coerceIn(0f, 1f)
        if (destinationReady) scope.launch { progress.snapTo(pendingProgress) }
    }

    fun cancelGesture(restore: () -> Unit) {
        if (!navigationStarted || settling) return
        settling = true
        scope.launch {
            preparation?.join()
            progress.animateTo(0f, tween(durationMillis = 180))
            // Restore under the fully covering snapshot (back) or offscreen destination
            // (forward), so the live screen never changes midway through the gesture.
            restore()
            withFrameNanos { }
            withFrameNanos { }
            removeSnapshot()
        }
    }

    fun completeGesture() {
        if (!navigationStarted || settling) return
        settling = true
        scope.launch {
            preparation?.join()
            progress.animateTo(1f, tween(durationMillis = 140))
            removeSnapshot()
            progress.snapTo(0f)
        }
    }

    val currentProgress = if (destinationReady) progress.value else 0f
    SideEffect {
        if (direction < 0f) {
            translatedView?.setTransform(CGAffineTransformMakeTranslation(
                tx = (1f - currentProgress) * snapshotWidth,
                ty = 0.0,
            ))
        } else {
            snapshot?.setTransform(CGAffineTransformMakeTranslation(
                tx = currentProgress * snapshotWidth,
                ty = 0.0,
            ))
        }
    }

    val handler = remember(dispatcher, viewController) {
        object : NavigationEventHandler<NavigationEventInfo>(
            initialInfo = ScreenNavigationInfo,
            isBackEnabled = true,
            isForwardEnabled = true,
        ) {
            override fun onBackStarted(event: NavigationEvent) {
                beginGesture(nextDirection = 1f) { latestBackStarted() }
            }

            override fun onBackProgressed(event: NavigationEvent) = updateGesture(event)

            override fun onBackCancelled() = cancelGesture { latestBackCancelled() }

            override fun onBackCompleted() = completeGesture()

            override fun onForwardStarted(event: NavigationEvent) {
                beginGesture(nextDirection = -1f) { latestForwardStarted() }
            }

            override fun onForwardProgressed(event: NavigationEvent) = updateGesture(event)

            override fun onForwardCancelled() = cancelGesture { latestForwardCancelled() }

            override fun onForwardCompleted() = completeGesture()
        }
    }
    DisposableEffect(dispatcher, handler) {
        dispatcher.addHandler(handler)
        onDispose {
            handler.remove()
            removeSnapshot()
        }
    }
}
