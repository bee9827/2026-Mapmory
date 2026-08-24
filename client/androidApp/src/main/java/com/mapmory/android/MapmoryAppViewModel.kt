package com.mapmory.android

import androidx.lifecycle.ViewModel
import com.mapmory.shared.app.AppContainer
import com.mapmory.shared.app.createInMemoryAppContainer

class MapmoryAppViewModel : ViewModel() {
    val container: AppContainer = createInMemoryAppContainer()

    override fun onCleared() {
        container.close()
    }
}
