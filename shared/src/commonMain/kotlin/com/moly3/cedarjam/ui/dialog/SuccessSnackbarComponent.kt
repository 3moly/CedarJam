package com.moly3.cedarjam.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Custom SnackbarHostState replacement
class CustomSnackbarHostState {
    private var _currentMessage by mutableStateOf<String?>(null)
    private var _isVisible by mutableStateOf(false)

    val currentMessage: String? get() = _currentMessage
    val isVisible: Boolean get() = _isVisible

    suspend fun showSnackbar(message: String, duration: Long = 4000L) {
        _currentMessage = message
        _isVisible = true
        delay(duration)
        dismissSnackbar()
    }

    fun dismissSnackbar() {
        _isVisible = false
        _currentMessage = null
    }
}

val LocalSuccessSnackbarHostState = compositionLocalOf { CustomSnackbarHostState() }

@Composable
internal fun BoxScope.SuccessSnackbarComponent(
    messageService: IMessageService
) {
    val snackbarHostState = remember { CustomSnackbarHostState() }
    ScrimUI(snackbarHostState)
    SuccessSnackbar(snackbarHostState) {
        val scope = rememberCoroutineScope()
        val successMessage = messageService.getFlow().collectAsState(initial = null).value
        val errorMessageText = successMessage
        val snackBar = LocalSuccessSnackbarHostState.current
        LaunchedEffect(key1 = errorMessageText, block = {
            scope.launch {
                if (!errorMessageText.isNullOrEmpty()) {
                    snackBar.showSnackbar(errorMessageText)
                    messageService.sendMessage(null)
                }
            }
        })
    }
}

@Composable
fun ScrimUI(snackbarHostState: CustomSnackbarHostState) {
    if (snackbarHostState.isVisible) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Green.copy(alpha = 0.2f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        snackbarHostState.dismissSnackbar()
                    }
                })
    }
}

@Composable
internal fun SuccessSnackbarCard(
    modifier: Modifier = Modifier,
    message: String
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(79.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Green)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CJText(
                    text = message,
//                    style = Fonts.Normal16.copy(textAlign = TextAlign.Center)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SuccessSnackbar(
    snackbarHostState: CustomSnackbarHostState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSuccessSnackbarHostState provides snackbarHostState
    ) {
        content()

        // Custom snackbar host with animations
        AnimatedVisibility(
            visible = snackbarHostState.isVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            snackbarHostState.currentMessage?.let { message ->
                SuccessSnackbarCard(
                    message = message,
                    modifier = Modifier
                )
            }
        }
    }
}