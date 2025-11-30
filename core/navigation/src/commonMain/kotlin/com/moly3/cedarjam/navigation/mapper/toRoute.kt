package com.moly3.cedarjam.navigation.mapper

import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.navigation.Route

fun IOpenNodeDataUseCase.Result.toRoute(): Route = when (this) {
    is IOpenNodeDataUseCase.Result.CollRow -> Route.CollRow(data)
    is IOpenNodeDataUseCase.Result.File -> Route.File(data)
    is IOpenNodeDataUseCase.Result.ToCollection -> Route.Collection(data)
    is IOpenNodeDataUseCase.Result.ToTag -> Route.Tag(data)
}
