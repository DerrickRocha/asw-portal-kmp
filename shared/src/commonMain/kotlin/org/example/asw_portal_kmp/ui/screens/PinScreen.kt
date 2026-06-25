package org.example.asw_portal_kmp.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.asw_portal_kmp.ui.viewModels.PinScreenState
import org.example.asw_portal_kmp.ui.viewModels.PinScreenViewModel

@Composable
fun PinScreen(
    email: String,
    onContinueClicked: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: PinScreenViewModel = viewModel { PinScreenViewModel(email = email) }
    val state by viewModel.state.collectAsState()

    PinScreenSection(
        state = state,
        onPinDigitEntered = viewModel::onPinDigitEntered,
        onPinBackspace = viewModel::onPinBackspace,
        onPinClear = viewModel::onPinClear,
        onContinueClicked = onContinueClicked,
        onConfirmPin = viewModel::onConfirmPin,
        onResendPin = viewModel::onConfirmPin,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun PinScreenSection(
    state: PinScreenState,
    onPinDigitEntered: (Int) -> Unit,
    onPinBackspace: () -> Unit,
    onPinClear: () -> Unit,
    onContinueClicked: () -> Unit,
    onConfirmPin: () -> Unit,
    onResendPin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val pinLength = 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header with animated success checkmark
            if (state.isVerified) {
                Text(
                    text = "PIN Verified Successfully!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Your account has been confirmed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Manual continue button
                Button(
                    onClick = onContinueClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Continue")
                }
            } else {
                // Normal PIN Entry UI
                Text(
                    text = "Enter PIN",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "A 6-digit PIN has been sent to your email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // PIN Dots Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                        .padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pinLength) { index ->
                        /*PinDot(
                            isFilled = index < state.pin.length,
                            isError = state.error != null,
                            isVerified = state.isVerified
                        )*/
                        PinDigit(
                            digit = state.pin.getOrNull(index)?.toString() ?: "",
                            isActive = index == state.pin.length,
                            isFilled = index < state.pin.length,
                            isError = state.error != null,
                            isVerified = state.isVerified
                        )
                    }
                }

                // Error Message
                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Resend PIN Link
                TextButton(
                    onClick = onResendPin,
                    enabled = !state.isLoading && !state.isVerified,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    if (state.isResending) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sending...")
                        }
                    } else {
                        Text("Resend PIN")
                    }
                }

                // Numeric Keypad
                NumericKeypad(
                    onDigitClick = { digit ->
                        if (state.pin.length < pinLength && !state.isLoading && !state.isVerified) {
                            onPinDigitEntered(digit)
                        }
                    },
                    onBackspaceClick = {
                        if (state.pin.isNotEmpty() && !state.isLoading && !state.isVerified) {
                            onPinBackspace()
                        }
                    },
                    onClearClick = {
                        if (state.pin.isNotEmpty() && !state.isLoading && !state.isVerified) {
                            onPinClear()
                        }
                    },
                    isLoading = state.isLoading,
                    isVerified = state.isVerified,
                    isPinComplete = state.pin.length == pinLength,
                    onConfirm = {
                        if (state.pin.length == pinLength && !state.isLoading && !state.isVerified) {
                            onConfirmPin()
                        }
                    }
                )

                // Cancel Button
                TextButton(
                    onClick = onNavigateBack,
                    enabled = !state.isLoading,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun PinDigit(
    digit: String,
    isActive: Boolean,
    isFilled: Boolean,
    isError: Boolean,
    isVerified: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive && !isFilled) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "digit_scale"
    )

    val color = when {
        isVerified -> MaterialTheme.colorScheme.primary
        isError -> MaterialTheme.colorScheme.error
        isFilled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .border(
                width = if (isActive && !isFilled) 3.dp else 2.dp,
                color = if (isActive && !isFilled)
                    MaterialTheme.colorScheme.primary
                else
                    color,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isActive && !isFilled)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            },
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Text(
                text = digit,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
        } else if (isActive) {
            // Blinking cursor
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }

        // Success animation overlay
        if (isVerified) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Composable
fun PinDot(
    isFilled: Boolean,
    isError: Boolean,
    isVerified: Boolean
) {
    val dotSize = 48.dp // Slightly smaller
    val fillSize = 14.dp // Slightly smaller

    val animatedScale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dot_scale"
    )

    val color = when {
        isVerified -> MaterialTheme.colorScheme.primary
        isError -> MaterialTheme.colorScheme.error
        isFilled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(dotSize)
            .border(
                width = 2.dp,
                color = color,
                shape = CircleShape
            )
            .graphicsLayer {
                scaleX = if (isVerified) 1.2f else 1f
                scaleY = if (isVerified) 1.2f else 1f
            },
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Box(
                modifier = Modifier
                    .size(fillSize)
                    .scale(animatedScale)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }

        // Success animation overlay
        if (isVerified) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    isVerified: Boolean,
    isPinComplete: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val digit = index + 1
                KeypadButton(
                    text = digit.toString(),
                    onClick = { onDigitClick(digit) },
                    enabled = !isLoading && !isVerified,
                    modifier = Modifier.semantics {
                        contentDescription = "Digit $digit"
                    }
                )
            }
        }

        // Row 2: 4, 5, 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val digit = index + 4
                KeypadButton(
                    text = digit.toString(),
                    onClick = { onDigitClick(digit) },
                    enabled = !isLoading && !isVerified,
                    modifier = Modifier.semantics {
                        contentDescription = "Digit $digit"
                    }
                )
            }
        }

        // Row 3: 7, 8, 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val digit = index + 7
                KeypadButton(
                    text = digit.toString(),
                    onClick = { onDigitClick(digit) },
                    enabled = !isLoading && !isVerified,
                    modifier = Modifier.semantics {
                        contentDescription = "Digit $digit"
                    }
                )
            }
        }

        // Row 4: Clear, 0, Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Clear button
            KeypadButton(
                text = "C",
                onClick = onClearClick,
                enabled = !isLoading && !isVerified,
                modifier = Modifier.semantics {
                    contentDescription = "Clear PIN"
                }
            )

            // 0
            KeypadButton(
                text = "0",
                onClick = { onDigitClick(0) },
                enabled = !isLoading && !isVerified,
                modifier = Modifier.semantics {
                    contentDescription = "Digit 0"
                }
            )

            // Backspace
            KeypadButton(
                text = "⌫",
                onClick = onBackspaceClick,
                enabled = !isLoading && !isVerified,
                modifier = Modifier.semantics {
                    contentDescription = "Backspace"
                }
            )
        }

        // Confirm Button
        if (isPinComplete && !isVerified) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onConfirm,
                enabled = !isLoading && !isVerified,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .semantics { contentDescription = "Confirm PIN" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying...")
                    }
                } else {
                    Text("Confirm PIN")
                }
            }
        }

        // Success state - show verified badge
        if (isVerified) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✓ Verified!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(72.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .background(
                color = if (isPressed && enabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = if (text == "⌫") 28.sp else 32.sp
            ),
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Preview
@Composable
fun PinScreenPreview() {
    PinScreenSection(
        state = PinScreenState(
            pin = "1234",
            isLoading = false,
            isVerified = false,
            error = null,
            isResending = false
        ),
        onPinDigitEntered = {},
        onPinBackspace = {},
        onPinClear = {},
        onContinueClicked = {},
        onConfirmPin = {},
        onResendPin = {},
        onNavigateBack = {}
    )
}