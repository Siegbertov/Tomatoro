package com.s1g1.tomatoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.s1g1.tomatoro.ui.MainAppScreen
import com.s1g1.tomatoro.ui.theme.TomatoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember{mutableStateOf(true)}
            TomatoroTheme(darkTheme = isDarkTheme) {
                MainAppScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {isDarkTheme = !isDarkTheme}
                )
            }
        }
    }
}