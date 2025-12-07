package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.vector.ImageVector
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.ui.vectors.Data
import com.moly3.cedarjam.core.ui.vectors.House
import com.moly3.cedarjam.core.ui.vectors.Note
import com.moly3.cedarjam.core.ui.vectors.Tag

fun PageNameData.PageType?.getPageTypeIcon(): ImageVector? {
    return when (this) {
        is PageNameData.PageType.Collection -> Data
        is PageNameData.PageType.CollectionRow -> Data
        is PageNameData.PageType.FileNode -> Note
        PageNameData.PageType.Graph -> Data
        PageNameData.PageType.Home -> House
        is PageNameData.PageType.Tag -> Tag
        PageNameData.PageType.Tags -> Tag
        null -> null
    }
}