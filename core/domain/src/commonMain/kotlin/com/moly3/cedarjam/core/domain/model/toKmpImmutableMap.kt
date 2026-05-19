package com.moly3.cedarjam.core.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

fun Map<String, List<String>>.toKmpImmutableMap(): ImmutableMap<String, ImmutableList<String>> {
    return this.mapValues { it.value.toImmutableList() }.toImmutableMap()
}