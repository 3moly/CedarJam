package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.navigation.input.AnnotationPageInput
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData

interface IOpenNodeDataUseCase {
    sealed interface Result {
        data class ToAnnotation(val data: AnnotationPageInput) : Result
        data class ToCollection(val data: CollectionPageInput) : Result
        data class ToTag(val data: TagPageInput) : Result
        data class CollRow(val data: CollectionRowPageInput) : Result
        data class File(val data: FilePageInput) : Result
    }

    suspend fun invoke(data: ObsidianGraphData, isFromGraph: Boolean): ResultWrapper<Result, String>
}