package com.mapmory.shared.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

@Composable
internal fun <T> MapmoryPhotoExpansion(
    photo: T?,
    content: @Composable (T?) -> Unit,
) {
    SharedTransitionLayout(Modifier.fillMaxSize()) {
        val sharedScope = this
        val painters = remember { mutableStateMapOf<String, Painter>() }
        AnimatedContent(
            targetState = photo,
            contentKey = { it != null },
            transitionSpec = {
                (fadeIn(tween(180)) togetherWith fadeOut(tween(180))).using(null)
            },
            label = "photo-expansion",
        ) { targetPhoto ->
            CompositionLocalProvider(
                LocalMapmoryImageTransitionScope provides MapmoryImageTransitionScope(sharedScope, this, painters),
            ) { content(targetPhoto) }
        }
    }
}

@Composable
internal fun MapmoryPhotoViewer(
    title: String,
    onClose: () -> Unit,
    closeContentDescription: String,
    trailingContent: @Composable () -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        content()
        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).background(Color.White.copy(alpha = 0.12f), CircleShape)
                    .clickable(onClick = onClose)
                    .semantics { contentDescription = closeContentDescription },
                contentAlignment = Alignment.Center,
            ) { Text("←", color = Color.White, fontSize = 24.sp) }
            Text(
                title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            trailingContent()
        }
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(16.dp),
        ) { bottomContent() }
    }
}
