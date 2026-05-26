package com.moly3.cedarjam.navigation.mapper

import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.Route.*

fun IOpenNodeDataUseCase.Result.toRoute(): Route = when (this) {
    is IOpenNodeDataUseCase.Result.CollRow -> Route.CollRow(data)
    is IOpenNodeDataUseCase.Result.File -> Route.File(data)
    is IOpenNodeDataUseCase.Result.ToCollection -> Collection(data)
    is IOpenNodeDataUseCase.Result.ToTag -> Tag(data)
    is IOpenNodeDataUseCase.Result.ToAnnotation -> {
        Route.Empty
    }
}
