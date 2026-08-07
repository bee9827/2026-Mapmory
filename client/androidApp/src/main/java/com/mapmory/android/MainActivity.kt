package com.mapmory.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapmory.shared.MapmoryApp
import com.mapmory.shared.data.remote.TravelRecordRemoteRepository
import com.mapmory.shared.data.remote.createHttpClient
import io.ktor.client.HttpClient

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
                MapboxMap(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onDestroy() {
        httpClient?.close()
        super.onDestroy()
    }
}
