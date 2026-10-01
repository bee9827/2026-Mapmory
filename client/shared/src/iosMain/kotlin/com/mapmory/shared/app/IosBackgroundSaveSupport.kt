@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.mapmory.shared.app

import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskInvalid
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

class IosBackgroundSaveExecution : BackgroundSaveExecution {
    override suspend fun <T> run(block: suspend () -> T): T {
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
        return try {
            block()
        } finally {
            if (taskIdentifier != UIBackgroundTaskInvalid) {
                application.endBackgroundTask(taskIdentifier)
            }
        }
    }
}

class IosBackgroundSaveFailureNotifier : BackgroundSaveFailureNotifier {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    init {
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
        ) { _, _ -> }
    }

    override fun notifyFailure(message: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle("여행 기록을 저장하지 못했어요")
            setBody(message)
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "trip-record-save-failed",
            content = content,
            trigger = null,
        )
        center.addNotificationRequest(request) {}
    }
}
