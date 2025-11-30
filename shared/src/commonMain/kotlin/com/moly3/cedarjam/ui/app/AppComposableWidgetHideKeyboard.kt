package com.moly3.cedarjam.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.ui.func.keyboardAsState
import com.moly3.cedarjam.core.ui.vectors.CommonIcHideKeyboard

@Composable
fun BoxScope.AppComposableWidgetHideKeyboard() {
    val isKeyboardShowed by keyboardAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    AnimatedVisibility(
        visible = isKeyboardShowed && getPlatform() == Platform.Ios,
        modifier = Modifier
            .padding(end = 24.dp)
            .align(Alignment.BottomEnd) // attach here instead
    ) {
        HideKeyboardCard(
            modifier = Modifier,
            onClick = { keyboard?.hide() }
        )
    }
}

@Composable
fun HideKeyboardCard(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(50.dp)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberVectorPainter(CommonIcHideKeyboard),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}