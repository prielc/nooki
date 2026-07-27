package com.nooki.app.ui.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nooki.app.R

/**
 * Gate for parent-only actions (FR-002; PRD §15 — any change to the approved-channel
 * list requires the PIN). Not used to gate routine app launches for the child.
 */
@Composable
fun ValidatePinScreen(
    validatePin: suspend (String) -> Boolean,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val firstDigitFocusRequester = remember { FocusRequester() }
    val errorText = stringResource(R.string.validate_pin_error)

    LaunchedEffect(currentInput) {
        if (currentInput.length < PIN_LENGTH) return@LaunchedEffect
        if (validatePin(currentInput)) {
            onSuccess()
        } else {
            currentInput = ""
            errorMessage = errorText
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
        Text(text = stringResource(R.string.validate_pin_title), fontSize = 32.sp)
        Text(
            text = stringResource(R.string.validate_pin_subtitle),
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
                    errorMessage = null
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
