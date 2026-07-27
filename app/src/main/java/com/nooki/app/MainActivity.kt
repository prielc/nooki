package com.nooki.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nooki.app.data.ProfileStore
import com.nooki.app.ui.pin.CreatePinScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileStore = ProfileStore(applicationContext)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    val isPinSet by profileStore.isPinSet.collectAsState(initial = null)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (isPinSet) {
                            null -> Unit
                            false -> CreatePinScreen(createPin = { pin -> profileStore.createPin(pin) })
                            true -> Text(text = "Nooki")
                        }
                    }
                }
            }
        }
    }
}
