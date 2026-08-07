package com.mapmory.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mapmory.shared.MapmoryApp
import com.mapmory.shared.data.remote.TravelRecordRemoteRepository
import com.mapmory.shared.data.remote.createHttpClient
import io.ktor.client.HttpClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.maps.MapView

class MainActivity : ComponentActivity() {
    private var httpClient: HttpClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiBaseUrl = getString(R.string.mapmory_api_base_url).trim()
        val repository = apiBaseUrl.takeIf(String::isNotEmpty)?.let { baseUrl ->
            httpClient = createHttpClient()
            TravelRecordRemoteRepository(httpClient!!, baseUrl, memberId = 10)
        }

        setContent {
            MapmoryApp(travelRecordRepository = repository) {
                MapLibreMap(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onDestroy() {
        httpClient?.close()
        super.onDestroy()
    }
}

@Composable
private fun MapLibreMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = {
            mapView.apply {
                onCreate(null)
                getMapAsync { map ->
                    map.setStyle("https://demotiles.maplibre.org/style.json")
                }
            }
        },
        modifier = modifier,
    )

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
}
