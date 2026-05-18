package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.vector.ImageVector
import com.moly3.cedarjam.core.ui.model.PageNameData
import vector.Data
import vector.House
import vector.collection.Note
import vector.Tag

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