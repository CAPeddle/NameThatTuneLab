package com.capeddle.namethattunelab.presentation.screen

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.capeddle.namethattunelab.TestActivity
import com.capeddle.namethattunelab.presentation.component.PERMISSION_STATUS_BAR_TAG
import com.capeddle.namethattunelab.presentation.theme.NtlTheme
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun delayField_doneImeAction_shouldExposeSemantics_andClearFocus() {
        composeTestRule.setContent {
            NtlTheme {
                MainScreenContent(
                    uiState = MainUiState(voiceOverDelayMsInput = "1000"),
                    callbacks = MainScreenCallbacks(
                        onOpenNotificationAccessSettings = {},
                        onDismissError = {},
                        onMusicBrainzUserAgentChanged = {},
                        onVoiceOverDelayChanged = {},
                        onSaveSettings = {}
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(VOICE_OVER_DELAY_INPUT_TAG)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .assert(
                SemanticsMatcher("has ImeAction.Done") { node ->
                    node.config.getOrNull(SemanticsProperties.ImeAction) == ImeAction.Done
                }
            )
            .performImeAction()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(VOICE_OVER_DELAY_INPUT_TAG)
            .assertIsNotFocused()
    }

    @Test
    fun deniedPermissionBanner_tap_shouldInvokeOpenSettingsCallback_andMeetMinTouchTarget() {
        val openSettingsInvocations = AtomicInteger(0)

        composeTestRule.setContent {
            NtlTheme {
                MainScreenContent(
                    uiState = MainUiState(isNotificationAccessGranted = false),
                    callbacks = MainScreenCallbacks(
                        onOpenNotificationAccessSettings = { openSettingsInvocations.incrementAndGet() },
                        onDismissError = {},
                        onMusicBrainzUserAgentChanged = {},
                        onVoiceOverDelayChanged = {},
                        onSaveSettings = {}
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag(PERMISSION_STATUS_BAR_TAG)
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
            .performClick()

        composeTestRule.waitForIdle()

        org.junit.Assert.assertEquals(1, openSettingsInvocations.get())
    }

    @Test
    fun grantedPermissionBanner_shouldNotBeClickable_orInvokeOpenSettingsCallback() {
        val openSettingsInvocations = AtomicInteger(0)

        composeTestRule.setContent {
            NtlTheme {
                MainScreenContent(
                    uiState = MainUiState(isNotificationAccessGranted = true),
                    callbacks = MainScreenCallbacks(
                        onOpenNotificationAccessSettings = { openSettingsInvocations.incrementAndGet() },
                        onDismissError = {},
                        onMusicBrainzUserAgentChanged = {},
                        onVoiceOverDelayChanged = {},
                        onSaveSettings = {}
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag(PERMISSION_STATUS_BAR_TAG)
            .assertHasNoClickAction()

        org.junit.Assert.assertEquals(0, openSettingsInvocations.get())
    }

    private companion object {
        const val VOICE_OVER_DELAY_INPUT_TAG = "settings_voice_over_delay_input"
    }
}
