package com.moly3.cedarjam.features.feature_canvas

import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import kotlinx.coroutines.flow.StateFlow

interface IDialogCanvasComponent {
    val filesRepository: IFilesRepository
    val state: StateFlow<State>
    fun onIntent(intent: Intent)
}