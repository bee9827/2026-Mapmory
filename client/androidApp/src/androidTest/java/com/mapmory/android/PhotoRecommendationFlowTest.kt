package com.mapmory.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.presentation.photo.PhotoLibraryActions
import com.mapmory.shared.presentation.photo.SelectedPhoto
import com.mapmory.shared.presentation.triprecord.screen.TripRecordEditorScreen
import com.mapmory.shared.presentation.triprecord.state.TripRecordEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PhotoRecommendationFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `선택한_장소의_추천_사진을_확인하고_추가할_수_있다`() {
        val seoul = province()
        val gangnam = district(parentId = seoul.id)
        val recommendedPhoto = SelectedPhoto(
            id = "gangnam-photo",
            displayName = "gangnam.jpg",
            previewBytes = null,
            latitude = 37.4979,
            longitude = 127.0276,
            capturedAt = "2026.08.21",
        )
        val laterLoadedPhoto = recommendedPhoto.copy(
            id = "gangnam-photo-2",
            displayName = "gangnam-2.jpg",
        )
        var requestedLocation: Location? = null
        var requestedParentName: String? = null
        var preparedPhotos = emptyList<SelectedPhoto>()
        var addedPhotos = emptyList<SelectedPhoto>()

        composeRule.setContent {
            TripRecordEditorScreen(
                uiState = TripRecordEditorUiState(selectedLocation = gangnam),
                locations = listOf(seoul, gangnam),
                onLocationSelected = {},
                onTitleChanged = {},
                onContentChanged = {},
                onStartDateChanged = {},
                onEndDateChanged = {},
                onPhotosAdded = { photos -> addedPhotos = photos },
                onSaveClick = {},
                onBackClick = {},
                photoLibraryActionsFactory = { _, onRecommended, _, _, _ ->
                    PhotoLibraryActions(
                        pickFromGallery = {},
                        recommendForLocation = { location, parentName ->
                            requestedLocation = location
                            requestedParentName = parentName
                            onRecommended(listOf(recommendedPhoto))
                            onRecommended(listOf(recommendedPhoto, laterLoadedPhoto))
                        },
                        prepareForAdding = { photos, onReady ->
                            preparedPhotos = photos
                            onReady(photos)
                        },
                    )
                },
            )
        }

        composeRule.onNodeWithText("위치 기반 사진\n불러오기").performClick()

        composeRule.onNodeWithText("이 장소에서 찍은 사진").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(gangnam, requestedLocation)
            assertEquals(seoul.name, requestedParentName)
        }

        composeRule.onNodeWithText("선택한 사진 추가").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(recommendedPhoto, laterLoadedPhoto), preparedPhotos)
            assertEquals(listOf(recommendedPhoto, laterLoadedPhoto), addedPhotos)
        }
    }

    @Test
    fun `장소를_선택하지_않으면_사진_라이브러리를_호출하지_않고_안내를_표시한다`() {
        var recommendationCalls = 0

        composeRule.setContent {
            TripRecordEditorScreen(
                uiState = TripRecordEditorUiState(),
                locations = listOf(province()),
                onLocationSelected = {},
                onTitleChanged = {},
                onContentChanged = {},
                onStartDateChanged = {},
                onEndDateChanged = {},
                onSaveClick = {},
                onBackClick = {},
                photoLibraryActionsFactory = { _, _, _, _, _ ->
                    PhotoLibraryActions(
                        pickFromGallery = {},
                        recommendForLocation = { _, _ -> recommendationCalls += 1 },
                    )
                },
            )
        }

        composeRule.onNodeWithText("위치 기반 사진\n불러오기").performClick()

        composeRule.onNodeWithText("사진을 추천받으려면 장소를 먼저 선택해 주세요.")
            .assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, recommendationCalls) }
    }

    @Test
    fun `기록_여행지는_국내_시도를_제외하고_시군구만_선택할_수_있다`() {
        val seoul = province()
        val gangnam = district(parentId = seoul.id)
        var selectedLocation: Location? = null

        composeRule.setContent {
            TripRecordEditorScreen(
                uiState = TripRecordEditorUiState(),
                locations = listOf(seoul, gangnam),
                onLocationSelected = { selectedLocation = it },
                onTitleChanged = {},
                onContentChanged = {},
                onStartDateChanged = {},
                onEndDateChanged = {},
                onSaveClick = {},
                onBackClick = {},
            )
        }

        composeRule.onNodeWithText("여행 장소를 선택해 주세요").performClick()

        assertEquals(0, composeRule.onAllNodesWithText("서울특별시").fetchSemanticsNodes().size)
        composeRule.onNodeWithText("장소명 또는 코드 검색").performTextInput("서울특별시")
        composeRule.onNodeWithText("강남구").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(gangnam, selectedLocation) }
    }

    private fun province() = Location(
        id = 1L,
        countryId = 1L,
        parentId = null,
        regionCode = "KR-11",
        name = "서울특별시",
        type = LocationType.PROVINCE,
    )

    private fun district(parentId: Long) = Location(
        id = 2L,
        countryId = 1L,
        parentId = parentId,
        regionCode = "11680",
        name = "강남구",
        type = LocationType.DISTRICT,
    )
}
