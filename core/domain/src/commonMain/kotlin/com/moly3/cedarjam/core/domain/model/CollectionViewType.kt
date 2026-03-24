package com.moly3.cedarjam.core.domain.model

enum class CollectionViewType(val num: Long) {
    Media(0), //pdf, youtube, img
    Word(1),
}

//Youtube(1),
//PDF(2),
//Anime(3),
//Japan(4),

fun Long?.toCollectionViewType(): CollectionViewType {
    return when (this) {
        CollectionViewType.Media.num -> CollectionViewType.Media
        CollectionViewType.Word.num -> CollectionViewType.Word
        else -> CollectionViewType.Media
    }
}