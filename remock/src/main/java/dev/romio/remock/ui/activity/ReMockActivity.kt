package dev.romio.remock.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.romio.remock.ReMockGraph
import dev.romio.remock.ui.components.ReMockLibrary
import dev.romio.remock.ui.theme.ReMockTheme

class ReMockActivity: ComponentActivity() {

    companion object {
        @JvmStatic
        fun open(context: Context) {
            val intent = Intent(context, ReMockActivity::class.java)
            context.startActivity(intent)
        }
    }

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