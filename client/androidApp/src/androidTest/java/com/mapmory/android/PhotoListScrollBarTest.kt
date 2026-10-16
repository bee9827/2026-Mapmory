package com.mapmory.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.mapmory.shared.presentation.components.PhotoListScrollBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhotoListScrollBarTest {
    @get:Rule val composeRule = createComposeRule()
    private lateinit var state: LazyListState

    private fun showList() {
        composeRule.setContent {
            state = rememberLazyListState()
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(top = 24.dp, bottom = 36.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(300) { index ->
                        Text("사진 $index", Modifier.fillMaxWidth().height((48 + index % 5 * 16).dp))
                    }
                }
                PhotoListScrollBar(state, Modifier.align(Alignment.CenterEnd))
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun thumbDragSurvivesRemeasureAndCanReverseWithoutLiftingFinger() {
        showList()
        val bar = composeRule.onNodeWithTag("photo-list-scrollbar")
        bar.performTouchInput {
            down(Offset(centerX, 8f))
            moveTo(Offset(centerX, height * 0.3f), delayMillis = 120)
        }
        composeRule.waitForIdle()
        var firstIndex = 0
        composeRule.runOnIdle {
            firstIndex = state.firstVisibleItemIndex
            assertTrue(firstIndex > 0)
        }
        bar.performTouchInput { moveTo(Offset(centerX, height * 0.8f), delayMillis = 120) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(state.firstVisibleItemIndex > firstIndex) }
        bar.performTouchInput { moveTo(Offset(centerX, 8f), delayMillis = 120) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(0, state.firstVisibleItemIndex) }
        bar.performTouchInput { up() }
    }

    @Test
    fun tappingTrackStillSeeksWithoutDragging() {
        showList()
        composeRule.onNodeWithTag("photo-list-scrollbar").performTouchInput {
            down(Offset(centerX, height * 0.7f))
            up()
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertTrue(state.firstVisibleItemIndex > 100) }
    }
}
