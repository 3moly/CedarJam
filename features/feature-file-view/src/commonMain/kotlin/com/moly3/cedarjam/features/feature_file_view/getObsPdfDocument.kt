package com.moly3.cedarjam.features.feature_file_view

import androidx.compose.runtime.Composable

@Composable
expect fun getObsPdfDocument(absolutePath: String): ObsPdfDocument?