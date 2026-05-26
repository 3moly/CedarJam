package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class TagId(val value: Long)

@JvmInline
@Serializable
value class AnnotationId(val value: Long)

@JvmInline
@Serializable
value class CollectionId(val value: Long)

@JvmInline
@Serializable
value class RowId(val value: Long)

@JvmInline
@Serializable
value class TagToTagId(val value: Long)

@JvmInline
@Serializable
value class TagRowId(val value: Long)

@JvmInline
@Serializable
value class TagLinkId(val value: Long)

@JvmInline
@Serializable
value class TagAnnotationId(val value: Long)