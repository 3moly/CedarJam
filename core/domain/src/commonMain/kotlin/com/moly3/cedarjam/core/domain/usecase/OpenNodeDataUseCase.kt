package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphData

class OpenNodeDataUseCase(
    private val navigateToFileUseCase: INavigateToFileUseCase
) : IOpenNodeDataUseCase {
    override suspend fun invoke(
        data: ObsidianGraphData,
        isFromGraph: Boolean
    ): ResultWrapper<IOpenNodeDataUseCase.Result, String> {
        return resultBlock(onError = { e -> "" }) {
            when (data) {
                is ObsidianGraphData.Collection -> IOpenNodeDataUseCase.Result.ToCollection(
                    CollectionPageInput(
                        collectionId = data.id
                    )
                )

                is ObsidianGraphData.CollectionRow -> IOpenNodeDataUseCase.Result.CollRow(
                    CollectionRowPageInput(
                        collectionId = data.collectionId,
                        rowId = data.id
                    )
                )

                is ObsidianGraphData.File -> {
                    val result =
                        navigateToFileUseCase.invoke(
                            NavigateToFile.RelativePath(
                                data.relativePath
                            )
                        )
                    val timestamp = bind(result = result)
                    IOpenNodeDataUseCase.Result.File(
                        FilePageInput(
                            timestamp = timestamp
                        )
                    )
                }

                is ObsidianGraphData.Tag -> IOpenNodeDataUseCase.Result.ToTag(
                    TagPageInput(
                        id = data.id,
                        isOpenGraphDialog = isFromGraph
                    )
                )
            }
        }
    }
}