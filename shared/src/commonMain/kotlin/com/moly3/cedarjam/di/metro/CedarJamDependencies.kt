package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.core.data.di.AppGraphServices
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import dev.zacsweers.metro.Inject

@Inject
class CedarJamDependencies(
    override val appContextProvider: AppContextProvider,
    override val fileHasher: IFileHasher,
    override val syncUseCase: ISyncUseCase
) : AppGraphServices
