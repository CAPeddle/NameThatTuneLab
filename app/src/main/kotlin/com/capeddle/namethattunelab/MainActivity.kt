package com.capeddle.namethattunelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.capeddle.namethattunelab.presentation.screen.MainScreen
import com.capeddle.namethattunelab.presentation.theme.NtlTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NtlTheme {
                MainScreen(
                    onOpenNotificationAccessSettings = {
                        NotificationAccessSettingsLauncher.launch(
                            context = this,
                            packageName = packageName
                        )
                    }
                )
            }
        }
    }
}
