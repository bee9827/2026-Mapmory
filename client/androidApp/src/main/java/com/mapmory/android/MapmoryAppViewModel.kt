package com.mapmory.android

import androidx.lifecycle.ViewModel
import com.mapmory.shared.createTripRecordsViewModel
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordsViewModel

class MapmoryAppViewModel : ViewModel() {
    val recordsViewModel: TripRecordsViewModel = createTripRecordsViewModel()
}
