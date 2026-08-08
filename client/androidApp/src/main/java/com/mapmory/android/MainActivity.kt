package com.mapmory.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.res.painterResource
import com.mapmory.shared.MapmoryApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapmoryApp(
                globePainter = { painterResource(R.drawable.globe_hero) },
            )
        }
    }
}
