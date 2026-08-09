package com.mapmory.android

import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mapmory.shared.MapmoryApp
import com.mapmory.shared.MapmoryNavigation

private val SystemBarColor = Color(0xFF07171B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(SystemBarColor.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(SystemBarColor.toArgb()),
        )
        setContent {
            val navigation = remember { MapmoryNavigation() }
            var lastBackPressedAt by remember { mutableLongStateOf(0L) }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = SystemBarColor,
                contentWindowInsets = WindowInsets.safeDrawing,
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    MapmoryApp(navigation = navigation)
                    BackHandler {
                        if (navigation.popBackStack()) {
                            lastBackPressedAt = 0L
                        } else {
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastBackPressedAt < ExitBackPressIntervalMs) {
                                finish()
                            } else {
                                lastBackPressedAt = now
                                Toast.makeText(
                                    this@MainActivity,
                                    "한 번 더 누르면 앱을 종료합니다.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val ExitBackPressIntervalMs = 2_000L
