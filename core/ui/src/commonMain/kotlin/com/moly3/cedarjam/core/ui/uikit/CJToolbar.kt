package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.ToolbarHeight
import com.moly3.cedarjam.core.ui.compositions.LocalJvmToolbarState
import com.moly3.cedarjam.core.ui.func.drawUnder
import com.moly3.cedarjam.core.ui.vectors.CloseSM
import com.moly3.cedarjam.core.ui.vectors.Left2

@Composable
fun CJToolbar(title: String?, onBack: (() -> Unit)? = null, onClose: () -> Unit) {
    Box(
        Modifier
            .height(ToolbarHeight.dp)
            .fillMaxWidth()
            .drawUnder(borderThickness = 0.5.dp)
            .padding(horizontal = 16.dp)

    ) {
        if (onBack != null) {
            CJIcon(
                modifier = Modifier
                    .padding(start = LocalJvmToolbarState.current.controlsWidthToCut)
                    .size(24.dp)
                    .align(Alignment.CenterStart),
                painter = rememberVectorPainter(Left2)
            ) {
                onBack()
            }
        }
        if (title != null) {
            CJText(
                modifier = Modifier.align(Alignment.Center),
                text = title//
            )
        }
        CJIcon(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd),
            painter = rememberVectorPainter(CloseSM)
        ) {
            onClose()
        }
    }
}