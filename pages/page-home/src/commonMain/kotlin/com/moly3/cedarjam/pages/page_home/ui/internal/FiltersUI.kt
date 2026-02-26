package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.moly3.cedarjam.core.domain.model.CollectionViewType
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
            painter = rememberVectorPainter(Data),
            isSelected = value == TimeMachineFilterType.All,
            buttType = ButtSnapType.Left
        ) {
            onSelect(TimeMachineFilterType.All)
        }
        CJButtSnap(
            painter = rememberVectorPainter(Data),
            isSelected = value == TimeMachineFilterType.Image,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Image)
        }
        CJButtSnap(
            painter = rememberVectorPainter(Tag),
            isSelected = value == TimeMachineFilterType.Tag,
            buttType = ButtSnapType.Center
        ) {
            onSelect(TimeMachineFilterType.Tag)
        }
//        CJButtSnap(
//            painter = rememberVectorPainter(Data),
//            isSelected = state.collection.viewType == CollectionViewType.PDF,
//            buttType = ButtSnapType.Center
//        ) {
//            onSelect(TimeMachineFilterType.All)
//        }
//        CJButtSnap(
//            painter = rememberVectorPainter(Data),
//            isSelected = state.collection.viewType == CollectionViewType.Japan,
//            buttType = ButtSnapType.Right
//        ) {
//            //onIntent(Intent.ChangeViewType(CollectionViewType.Japan))
//        }
    }
}