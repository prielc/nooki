package com.nooki.app.ui.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nooki.app.R

internal const val PIN_LENGTH = 4

/** Shared by CreatePinScreen (FR-001) and ValidatePinScreen (FR-002). */
@Composable
internal fun PinDots(filledCount: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(PIN_LENGTH) { index ->
            val filled = index < filledCount
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.borderVariant
                    )
            )
        }
    }
}

@Composable
internal fun PinKeypad(
    firstDigitFocusRequester: FocusRequester,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(null, "0", "delete")
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    when (key) {
                        null -> Box(modifier = Modifier.size(72.dp))
                        "delete" -> Button(onClick = onDelete, modifier = Modifier.height(72.dp)) {
                            Text(text = stringResource(R.string.pin_delete))
                        }
                        "1" -> Button(
                            onClick = { onDigit(key) },
                            modifier = Modifier.size(72.dp).focusRequester(firstDigitFocusRequester)
                        ) {
                            Text(text = key, fontSize = 24.sp)
                        }
                        else -> Button(onClick = { onDigit(key) }, modifier = Modifier.size(72.dp)) {
                            Text(text = key, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}
