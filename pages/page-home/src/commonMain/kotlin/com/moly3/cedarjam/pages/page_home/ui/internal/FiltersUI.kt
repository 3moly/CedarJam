package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.moly3.cedarjam.core.ui.uikit.ButtSnapType
import com.moly3.cedarjam.core.ui.uikit.CJButtSnap
import vectors.Data
import vectors.Tag
import com.moly3.cedarjam.pages.page_home.model.TimeMachineFilterType

@Composable
internal fun FiltersUI(
    modifier: Modifier = Modifier,
    value: TimeMachineFilterType,
    onSelect: (TimeMachineFilterType) -> Unit
) {
    Row(modifier = modifier) {
        CJButtSnap(
            painter = rememberVectorPainter(vectors.Home03),
            isSelected = value == TimeMachineFilterType.All,
            buttType = ButtSnapType.Start
        ) {
            onSelect(TimeMachineFilterType.All)
        }
        CJButtSnap(
            painter = rememberVectorPainter(vectors.Note),
            isSelected = value == TimeMachineFilterType.Text,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Text)
        }
        CJButtSnap(
            painter = rememberVectorPainter(vectors.Image02),
            isSelected = value == TimeMachineFilterType.Image,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Image)
        }
        CJButtSnap(
            painter = rememberVectorPainter(vectors.Dataflow03),
            isSelected = value == TimeMachineFilterType.Canvas,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Canvas)
        }
        CJButtSnap(
            painter = rememberVectorPainter(Tag),
            isSelected = value == TimeMachineFilterType.Tag,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Tag)
        }
        CJButtSnap(
            painter = rememberVectorPainter(vectors.Data),
            isSelected = value == TimeMachineFilterType.Row,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Row)
        }
        CJButtSnap(
            painter = rememberVectorPainter(vectors.File05),
            isSelected = value == TimeMachineFilterType.Pdf,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Pdf)
        }
        CJButtSnap(
            painter = rememberVectorPainter(vectors.FileAttach01),
            isSelected = value == TimeMachineFilterType.Annotation,
            buttType = ButtSnapType.End
        ) {
            onSelect(TimeMachineFilterType.Annotation)
        }
    }
}