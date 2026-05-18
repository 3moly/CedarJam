package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.TimeMachine

fun TimeMachine.dpSize(): DpSize {
    return when (this) {
        is TimeMachine.Collection -> DpSize(150.dp, 150.dp)
        is TimeMachine.FileNode -> DpSize(150.dp, 150.dp)
        is TimeMachine.Row -> DpSize(150.dp, 150.dp)

        is TimeMachine.Tag -> DpSize(250.dp, 50.dp)
        is TimeMachine.Annotation -> DpSize(250.dp, 150.dp)
    }
}