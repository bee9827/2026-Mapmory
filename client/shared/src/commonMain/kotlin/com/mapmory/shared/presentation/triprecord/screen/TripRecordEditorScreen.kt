package com.mapmory.shared.presentation.triprecord.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.presentation.photo.MaxPhotosPerRecord
import com.mapmory.shared.presentation.photo.SelectedPhoto
import com.mapmory.shared.presentation.photo.rememberPhotoLibraryActions
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TripRecordEditorScreen(
    uiState: TripRecordEditorUiState,
    locations: List<Location>,
    onLocationSelected: (Location) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onPhotosAdded: (List<SelectedPhoto>) -> Unit = {},
    onPhotoRemoved: (String) -> Unit = {},
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onMapClick: () -> Unit = {},
    onRecordClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val selectableLocations = remember(locations) {
        locations
            .filter { it.type == LocationType.PROVINCE || it.type == LocationType.DISTRICT }
            .distinctBy(Location::regionCode)
    }
    var showLocationSheet by remember { mutableStateOf(false) }
    var locationSearchQuery by rememberSaveable { mutableStateOf("") }
    var photoMessage by remember { mutableStateOf<String?>(null) }
    var recommendedPhotos by remember { mutableStateOf(emptyList<SelectedPhoto>()) }
    var selectedRecommendationIds by remember { mutableStateOf(emptySet<String>()) }
    var showRecommendationSheet by remember { mutableStateOf(false) }
    val photoLibrary = rememberPhotoLibraryActions(
        onPhotosPicked = { photos ->
            photoMessage = null
            onPhotosAdded(photos)
        },
        onPhotosRecommended = { photos ->
            recommendedPhotos = photos
            selectedRecommendationIds = photos.map(SelectedPhoto::id).toSet()
            showRecommendationSheet = photos.isNotEmpty()
            photoMessage = if (photos.isEmpty()) {
                "선택한 지역에서 촬영된 GPS 사진을 찾지 못했어요."
            } else {
                null
            }
        },
        onMessage = { photoMessage = it },
    )
    val locationResultsListState = rememberLazyListState()
    val filteredLocations = remember(locationSearchQuery, selectableLocations) {
        selectableLocations.filter { location ->
            locationSearchQuery.isBlank() ||
                location.name.contains(locationSearchQuery, ignoreCase = true) ||
                location.regionCode.contains(locationSearchQuery, ignoreCase = true)
        }
    }

    TripRecordBackground(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            TripRecordTopBar(
                title = if (uiState.recordId == null) "새 기록 작성" else "기록 수정",
                onBackClick = onBackClick,
                trailing = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = !uiState.isSaving,
                    ) {
                        Text(
                            text = if (uiState.isSaving) "저장 중" else "완료",
                            color = if (uiState.isSaving) TripRecordPalette.muted else TripRecordPalette.accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    EditorSectionLabel("제목")
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = onTitleChanged,
                        placeholder = { Text("여행의 제목을 입력해 주세요") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }

                item {
                    EditorSectionLabel("장소")
                    LocationSelector(
                        selectedLocation = uiState.selectedLocation,
                        locations = locations,
                        onClick = { showLocationSheet = true },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                item {
                    EditorSectionLabel("여행 사진")
                    PhotoEditor(
                        photos = uiState.selectedPhotos,
                        onAddClick = photoLibrary.pickFromGallery,
                        onRecommendClick = {
                            val selectedLocation = uiState.selectedLocation
                            if (selectedLocation == null) {
                                photoMessage = "사진을 추천받으려면 장소를 먼저 선택해 주세요."
                            } else {
                                photoMessage = "${selectedLocation.name}에서 촬영된 사진을 찾고 있어요."
                                val parentName = locations
                                    .firstOrNull { it.id == selectedLocation.parentId }
                                    ?.name
                                photoLibrary.recommendForLocation(selectedLocation, parentName)
                            }
                        },
                        onRemoveClick = onPhotoRemoved,
                        recommendationsAvailable = photoLibrary.recommendationsAvailable,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    photoMessage?.let { message ->
                        Text(
                            text = message,
                            color = TripRecordPalette.muted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                item {
                    EditorSectionLabel("여행 날짜")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = uiState.startDate,
                            onValueChange = onStartDateChanged,
                            placeholder = { Text("YYYY.MM.DD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = uiState.endDate,
                            onValueChange = onEndDateChanged,
                            placeholder = { Text("종료일") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    EditorSectionLabel("기록")
                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = onContentChanged,
                        placeholder = { Text("여행에서 느낀 점을 기록해 보세요") },
                        minLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }

                uiState.errorMessage?.let { message ->
                    item {
                        Text(message, color = TripRecordPalette.danger, fontSize = 12.sp)
                    }
                }

                item { Spacer(Modifier.height(18.dp)) }
            }

            TripBottomBar(
                selected = TripBottomTab.CREATE,
                onMapClick = onMapClick,
                onRecordClick = onRecordClick,
                onProfileClick = onProfileClick,
            )
        }
    }

    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = TripRecordPalette.background,
            contentColor = TripRecordPalette.text,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = "장소 선택",
                    color = TripRecordPalette.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "국가, 시·도, 시·군·구를 검색해 보세요",
                    color = TripRecordPalette.muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                OutlinedTextField(
                    value = locationSearchQuery,
                    onValueChange = { locationSearchQuery = it },
                    placeholder = { Text("장소명 또는 코드 검색") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TripRecordPalette.text,
                        unfocusedTextColor = TripRecordPalette.text,
                        cursorColor = TripRecordPalette.accent,
                        focusedBorderColor = TripRecordPalette.accent,
                        unfocusedBorderColor = TripRecordPalette.line,
                        focusedPlaceholderColor = TripRecordPalette.muted,
                        unfocusedPlaceholderColor = TripRecordPalette.muted,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )
                Spacer(Modifier.height(14.dp))
                if (filteredLocations.isEmpty()) {
                    Text(
                        text = "검색 결과가 없습니다.",
                        color = TripRecordPalette.muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 28.dp),
                    )
                } else {
                    LazyColumn(
                        state = locationResultsListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .nestedScroll(consumeLocationListOverscrollConnection),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(filteredLocations) { location ->
                            LocationSearchResult(
                                location = location,
                                locations = locations,
                                selected = uiState.selectedLocation?.regionCode == location.regionCode,
                                onClick = {
                                    onLocationSelected(location)
                                    showLocationSheet = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRecommendationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecommendationSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = TripRecordPalette.background,
            contentColor = TripRecordPalette.text,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
            ) {
                Text("이 장소에서 찍은 사진", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "사진의 EXIF GPS가 선택한 행정구역과 일치하는 결과예요.",
                    color = TripRecordPalette.muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    recommendedPhotos.forEach { photo ->
                        val selected = photo.id in selectedRecommendationIds
                        RecommendedPhoto(
                            photo = photo,
                            selected = selected,
                            onClick = {
                                selectedRecommendationIds = if (selected) {
                                    selectedRecommendationIds - photo.id
                                } else {
                                    selectedRecommendationIds + photo.id
                                }
                            },
                        )
                    }
                }
                TextButton(
                    onClick = {
                        onPhotosAdded(
                            recommendedPhotos.filter { it.id in selectedRecommendationIds },
                        )
                        showRecommendationSheet = false
                        photoMessage = null
                    },
                    enabled = selectedRecommendationIds.isNotEmpty(),
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("선택한 사진 추가", color = TripRecordPalette.accent, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PhotoEditor(
    photos: List<SelectedPhoto>,
    onAddClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onRemoveClick: (String) -> Unit,
    recommendationsAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        photos.forEach { photo ->
            Box {
                PhotoPreview(photo = photo, modifier = Modifier.size(112.dp, 84.dp))
                Text(
                    text = "×",
                    color = TripRecordPalette.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onRemoveClick(photo.id) }
                        .background(TripRecordPalette.background.copy(alpha = 0.8f))
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                )
            }
        }
        if (photos.size < MaxPhotosPerRecord) {
            PhotoActionButton(icon = "＋", label = "사진 추가", onClick = onAddClick)
        }
        if (recommendationsAvailable) {
            PhotoActionButton(icon = "⌖", label = "장소 추천", onClick = onRecommendClick)
        }
    }
}

@Composable
private fun PhotoActionButton(icon: String, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(TripRecordPalette.surface)
            .border(1.dp, TripRecordPalette.line, RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, color = TripRecordPalette.accent, fontSize = 27.sp)
        Text(label, color = TripRecordPalette.muted, fontSize = 10.sp)
    }
}

@Composable
private fun RecommendedPhoto(
    photo: SelectedPhoto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.width(132.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) TripRecordPalette.accent else TripRecordPalette.line,
                    shape = RoundedCornerShape(18.dp),
                ),
        ) {
            PhotoPreview(photo, Modifier.size(132.dp, 100.dp))
            if (selected) {
                Text(
                    "✓",
                    color = TripRecordPalette.text,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                        .background(TripRecordPalette.accent, RoundedCornerShape(20.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
        }
        Text(
            text = photo.capturedAt ?: photo.displayName,
            color = TripRecordPalette.muted,
            fontSize = 10.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun PhotoPreview(photo: SelectedPhoto, modifier: Modifier = Modifier) {
    TripPhotoImage(
        previewBytes = photo.previewBytes,
        contentDescription = photo.displayName,
        modifier = modifier,
        placeholderVariant = photo.id.hashCode(),
    )
}
@Composable
private fun EditorSectionLabel(text: String) {
    Text(
        text = text,
        color = TripRecordPalette.muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun LocationSelector(
    selectedLocation: Location?,
    locations: List<Location>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(TripRecordPalette.surface)
            .border(1.dp, TripRecordPalette.line, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = selectedLocation?.name ?: "여행 장소를 선택해 주세요",
                color = if (selectedLocation == null) TripRecordPalette.muted else TripRecordPalette.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = selectedLocation?.let {
                    "${locationTypeLabel(it)} · ${locationContext(it, locations)}"
                } ?: "국가, 시·도, 시·군·구 검색",
                color = TripRecordPalette.muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
        Text(
            text = if (selectedLocation == null) "선택" else "변경",
            color = TripRecordPalette.accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LocationSearchResult(
    location: Location,
    locations: List<Location>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(if (selected) TripRecordPalette.accentSoft else TripRecordPalette.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = location.name,
                color = TripRecordPalette.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${locationTypeLabel(location)} · ${locationContext(location, locations)}",
                color = TripRecordPalette.muted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (selected) {
            Text(
                text = "선택됨",
                color = TripRecordPalette.accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun locationTypeLabel(location: Location): String = when {
    location.countryId != KoreaCountryId -> "국가"
    location.type == LocationType.PROVINCE -> "시·도"
    else -> "시·군·구"
}

private fun locationContext(location: Location, locations: List<Location>): String = when {
    location.countryId != KoreaCountryId -> "세계 지도"
    location.type == LocationType.PROVINCE -> "대한민국"
    else -> locations.firstOrNull { it.id == location.parentId }?.name ?: "대한민국"
}

private val consumeLocationListOverscrollConnection = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = Offset(x = 0f, y = available.y)

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity = Velocity(x = 0f, y = available.y)
}


private const val KoreaCountryId = 1L
