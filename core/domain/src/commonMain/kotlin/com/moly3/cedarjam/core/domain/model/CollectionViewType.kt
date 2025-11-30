package com.moly3.cedarjam.core.domain.model

enum class CollectionViewType(val num: Long) {
    DataGrid(0),
    Youtube(1),
    PDF(2),
    Anime(3),
    Japan(4)
}

fun Long?.toCollectionViewType(): CollectionViewType {
    return when (this) {
        CollectionViewType.DataGrid.num -> CollectionViewType.DataGrid
        CollectionViewType.Youtube.num -> CollectionViewType.Youtube
        CollectionViewType.PDF.num -> CollectionViewType.PDF
        CollectionViewType.Anime.num -> CollectionViewType.Anime
        CollectionViewType.Japan.num -> CollectionViewType.Japan
        else -> CollectionViewType.DataGrid
    }
}