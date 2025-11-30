package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper

interface INavigateToFileUseCase {
    suspend operator fun invoke(navigate: NavigateToFile): ResultWrapper<Long, String>
}

