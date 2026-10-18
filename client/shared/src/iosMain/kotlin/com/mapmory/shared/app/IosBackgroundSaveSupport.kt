@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mapmory.shared.app

import platform.UserNotifications.*
import platform.UIKit.UIApplicationState.UIApplicationStateActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskInvalid

internal var iosBackgroundSaves: kotlinx.coroutines.flow.StateFlow<List<BackgroundTripRecordSave>>? = null

class IosBackgroundSaveExecution(namespace: String) : BackgroundSaveExecution {
    override fun attach(saver: BackgroundTripRecordSaver) { iosBackgroundSaves = saver.saves }
    private var lastNotificationBucket = -1
    override fun onProgress(save: BackgroundTripRecordSave) {
        if (UIApplication.sharedApplication.applicationState == UIApplicationStateActive) return
        val bucket = save.progressPercent / 20
        if (bucket == lastNotificationBucket) return
        lastNotificationBucket = bucket
        val content = UNMutableNotificationContent().apply {
            setTitle(save.title)
            setBody("${save.progressPercent}% 업로드 중")
        }
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier("record-progress", content, null),
        ) { _ -> }
    }
    override val store = IosBackgroundSaveStore(namespace)
    override fun onQueued() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
        ) { _, _ -> }
    }
    override fun onCompleted(save: BackgroundTripRecordSave) {
        lastNotificationBucket = -1
        UNUserNotificationCenter.currentNotificationCenter().removeDeliveredNotificationsWithIdentifiers(listOf("record-progress"))
        val content = UNMutableNotificationContent().apply {
            setTitle("사진 업로드 완료")
            setBody("${save.locationName} 기록이 저장됐어요")
            setSound(UNNotificationSound.defaultSound())
        }
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier("record-${save.id}", content, null),
        ) { _ -> }
    }

    override suspend fun <T> run(block: suspend () -> T): T = withContext(Dispatchers.Main) {
        val application = UIApplication.sharedApplication
        var taskIdentifier = UIBackgroundTaskInvalid
        taskIdentifier = application.beginBackgroundTaskWithName(
            taskName = "Mapmory trip record upload",
            expirationHandler = {
                if (taskIdentifier != UIBackgroundTaskInvalid) {
                    application.endBackgroundTask(taskIdentifier)
                    taskIdentifier = UIBackgroundTaskInvalid
                }
            },
        )
        try {
            block()
        } finally {
            if (taskIdentifier != UIBackgroundTaskInvalid) {
                application.endBackgroundTask(taskIdentifier)
            }
        }
    }
}
