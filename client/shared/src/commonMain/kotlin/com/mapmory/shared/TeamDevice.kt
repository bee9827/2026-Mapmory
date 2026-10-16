package com.mapmory.shared

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** 설정의 앱 버전을 연속으로 눌러 팀원 기기 모드를 바꾸는 숨은 스위치 상태다. */
@Immutable
data class TeamDeviceState(
    val appVersion: String,
    val isTeamDevice: Boolean,
    val onTeamDeviceChange: (Boolean) -> Unit,
)

val LocalTeamDevice = staticCompositionLocalOf {
    TeamDeviceState(
        appVersion = "",
        isTeamDevice = false,
        onTeamDeviceChange = {},
    )
}

/** [requiredTaps]번째 탭마다 true를 돌려주고 다시 처음부터 센다. */
class TeamDeviceTapCounter(private val requiredTaps: Int = TeamDeviceRequiredTaps) {
    private var taps = 0

    fun tap(): Boolean {
        taps += 1
        if (taps < requiredTaps) return false
        taps = 0
        return true
    }
}

const val TeamDeviceRequiredTaps = 7
