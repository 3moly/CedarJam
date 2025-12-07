package com.moly3.cedarjam.features.feature_settings.child.general.ui.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.ColorsType
import com.moly3.cedarjam.core.domain.model.settings.AppSettings
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.ui.JvmWindowScope
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJApplicationTheme
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJToggler
import com.moly3.cedarjam.core.ui.uikit.CJToolbar
import com.moly3.cedarjam.core.ui.uikit.LanguageComboBox
import com.moly3.cedarjam.features.feature_settings.child.SettingsContent
import com.moly3.cedarjam.features.feature_settings.child.general.Intent
import com.moly3.cedarjam.features.feature_settings.child.main.ui.SelectOption
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.dark
import com.moly3.cedarjam.ui.f_settings_general_appearance
import com.moly3.cedarjam.ui.f_settings_general_title
import com.moly3.cedarjam.ui.f_settings_options
import com.moly3.cedarjam.ui.language
import com.moly3.cedarjam.ui.light
import com.moly3.cedarjam.ui.theme
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun JvmWindowScope.SettingsGeneralUIContent(
    settings: WorkspaceSettings,
    onIntent: (Intent)->Unit
) {
    SettingsContent {
        Box {
            CJDraggableArea {
                CJToolbar(
                    title = stringResource(Res.string.f_settings_general_title),
                    onBack = {
                        onIntent(Intent.Back)
                    },
                    onClose = {
                        onIntent(Intent.Close)
                    }
                )
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth()) {
            CJText(
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
                text = stringResource(Res.string.f_settings_general_appearance),
                fontSize = 12.sp,
                color = LocalAppTheme.current.colors.secondaryFont
            )
            SelectOption(
                modifier = Modifier,
                text = stringResource(Res.string.language),
                isShowBorder = true
            ) {
                LanguageComboBox(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    selectedCode = settings.language ?: "en"
                ) {
                    onIntent(Intent.SetSettings(settings.copy(language = it.code)))
                }
            }
            SelectOption(
                modifier = Modifier,
                text = stringResource(Res.string.theme),
                isShowBorder = true
            ) {
                val lightKey = "Light"
                val darkKey = "Dark"

                val colorMode = remember(settings) {
                    when (settings.theme.colorsType) {
                        ColorsType.Dark -> darkKey
                        ColorsType.Light -> lightKey
                    }
                }
                CJToggler(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    list = persistentListOf(lightKey, darkKey),
                    selected = colorMode,
                    onDisplay = {
                        when (it) {
                            lightKey -> stringResource(Res.string.light)
                            darkKey -> stringResource(Res.string.dark)
                            else -> stringResource(Res.string.light)
                        }
                    },
                    onSelect = {
                        val switch = when (it) {
                            darkKey -> ColorsType.Dark
                            lightKey -> ColorsType.Light
                            else -> ColorsType.Dark
                        }
                        val colors = if (switch == ColorsType.Dark) {
                            AppColorsData.Dark
                        } else {
                            AppColorsData.Light
                        }
                        onIntent(Intent.SetSettings(
                            settings.copy(
                                theme = settings.theme.copy(
                                    colorsType = switch,
                                    colors = colors
                                )
                            )
                        ))
                    }
                )
            }
            SelectOption(
                text = "Upload new font",
                isShowBorder = true,
                onClick = {
                    onIntent(Intent.UploadFont)
                }
            )
        }
    }
}