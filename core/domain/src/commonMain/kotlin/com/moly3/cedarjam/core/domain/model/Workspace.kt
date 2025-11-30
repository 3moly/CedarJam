package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val name: String,
    val fullpath: String
)

@Serializable
data class WorkspaceInput(val name: String)

@Serializable
data class WorkspacePresentation(
    val name: String,
    val fullpath: String,
    val absolutePath: String
)