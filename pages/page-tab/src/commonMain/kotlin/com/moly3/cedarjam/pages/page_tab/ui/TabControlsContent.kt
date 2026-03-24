package com.moly3.cedarjam.pages.page_tab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJTextField2
import vector.ArrowLeft
import vector.ArrowRight

@Composable
internal fun TabControlsContent(
    tabNameIcon: ImageVector? = null,
    canGoBack: Boolean,
    canGoForward: Boolean,
    textNameState: TextFieldState,
    isEditEnabled: Boolean,
    onRename: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    Box(Modifier.padding(vertical = 6.dp, horizontal = 12.dp).fillMaxWidth()) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (text) = createRefs()
            CJTextField2(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .constrainAs(text) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                iconPainter = tabNameIcon,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                ),
                text = textNameState,
                onDone = { onRename() },
                onLostFocus = { onRename() },
                enabled = isEditEnabled
            )
        }
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            CJIcon(
                modifier = Modifier,
                painter = rememberVectorPainter(ArrowLeft),
                isEnabled = canGoBack,
                onClick = onBack
            )
            CJIcon(
                modifier = Modifier,
                painter = rememberVectorPainter(ArrowRight),
                isEnabled = canGoForward,
                onClick = onForward
            )
        }
    }
}