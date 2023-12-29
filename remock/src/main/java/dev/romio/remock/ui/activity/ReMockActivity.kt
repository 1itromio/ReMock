package dev.romio.remock.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.romio.remock.ReMockGraph
import dev.romio.remock.ui.components.ReMockLibrary
import dev.romio.remock.ui.theme.ReMockTheme

class ReMockActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReMockGraph.initialize(this.applicationContext)
        setContent {
            ReMockTheme {
                ReMockLibrary()
            }
        }
    }
}