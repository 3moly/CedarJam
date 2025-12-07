package com.moly3.cedarjam.features.feature_settings.child.general.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.LanguageComboBox
import com.moly3.cedarjam.core.ui.vectors.CloseSM
import com.moly3.cedarjam.features.feature_settings.child.general.ui.drawUnder
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsGeneralUIContent(
    settings: WorkspaceSettings,
    onSetSettings: (WorkspaceSettings) -> Unit,
    onClose: () -> Unit
) {
    val appTheme = LocalAppTheme.current
    Box(
        Modifier
            .fillMaxSize()
            .background(appTheme.colors.backgroundPrimary)
            .flatClickable {}
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column {
            Box(Modifier.height(46.dp).fillMaxWidth().drawUnder(borderThickness = 0.5.dp)) {
                CJText(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.f_settings_general_title)
                )
                CJIcon(
                    modifier = Modifier.padding(end = 16.dp).size(24.dp).align(Alignment.CenterEnd),
                    painter = rememberVectorPainter(CloseSM)
                ) {
                    onClose()
                }
            }

            Column(Modifier.weight(1f).fillMaxWidth()) {

                LanguageComboBox(selectedCode = settings.language ?: "en") {
                    onSetSettings(settings.copy(language = it.code))
                }
            }
        }
    }
}