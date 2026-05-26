package com.moly3.cedarjam.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.delay

@Composable
fun BoxScope.TopAlertServiceUI(alert: AlertService) {

    var currentMessage by remember { mutableStateOf<String?>(null) }

    // Collect alerts
    LaunchedEffect(Unit) {
        alert.sendFlow.collect { message ->
            currentMessage = message
        }
    }

    // Auto hide after 3 seconds
    LaunchedEffect(currentMessage) {
        if (currentMessage != null) {
            delay(3000)
            currentMessage = null
        }
    }
    // Top error banner
    AnimatedVisibility(
        visible = currentMessage != null,
        enter = slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { -it }
        ) + fadeIn(),
        exit = slideOutVertically(
            animationSpec = tween(300),
            targetOffsetY = { -it }
        ) + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD32F2F))
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            CJText(
                text = currentMessage ?: "",
                color = Color.White
            )
        }
    }
}