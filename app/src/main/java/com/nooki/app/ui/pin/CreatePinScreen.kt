package com.nooki.app.ui.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

private const val PIN_LENGTH = 4

private enum class PinStage {
    ENTER,
    CONFIRM
}

/**
 * On-screen numeric keypad (no physical number keys on a TV remote, per PP-003)
 * driving the two-step "enter, then confirm" flow for FR-001 (Create PIN).
 */
@Composable
fun CreatePinScreen(createPin: suspend (String) -> Unit, modifier: Modifier = Modifier) {
    var stage by remember { mutableStateOf(PinStage.ENTER) }
    var firstPin by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val firstDigitFocusRequester = remember { FocusRequester() }
    val mismatchMessage = stringResource(R.string.create_pin_error_mismatch)

    LaunchedEffect(stage, currentInput) {
        if (currentInput.length < PIN_LENGTH) return@LaunchedEffect
        when (stage) {
            PinStage.ENTER -> {
                firstPin = currentInput
                currentInput = ""
                stage = PinStage.CONFIRM
            }
            PinStage.CONFIRM -> {
                if (currentInput == firstPin) {
                    createPin(currentInput)
                } else {
                    firstPin = ""
                    currentInput = ""
                    stage = PinStage.ENTER
                    errorMessage = mismatchMessage
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        firstDigitFocusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.create_pin_title), fontSize = 32.sp)
        Text(
            text = stringResource(
                if (stage == PinStage.ENTER) R.string.create_pin_subtitle_enter else R.string.create_pin_subtitle_confirm
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        PinDots(filledCount = currentInput.length, modifier = Modifier.padding(vertical = 24.dp))
        PinKeypad(
            firstDigitFocusRequester = firstDigitFocusRequester,
            onDigit = { digit ->
                if (currentInput.length < PIN_LENGTH) {
                    currentInput += digit
                }
            },
            onDelete = {
                if (currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                }
            }
        )
    }
}

@Composable
private fun PinDots(filledCount: Int, modifier: Modifier = Modifier) {
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
private fun PinKeypad(
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
