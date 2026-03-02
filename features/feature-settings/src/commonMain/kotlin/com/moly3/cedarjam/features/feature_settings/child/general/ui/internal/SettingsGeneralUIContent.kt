package com.moly3.cedarjam.features.feature_settings.child.general.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.AppColorsData
import com.moly3.cedarjam.core.domain.model.ColorsType
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalSystemDensity
import com.moly3.cedarjam.core.ui.uikit.CJDraggableArea
import com.moly3.cedarjam.core.ui.uikit.CJSlider
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
import com.moly3.cedarjam.ui.language
import com.moly3.cedarjam.ui.light
import com.moly3.cedarjam.ui.theme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

@OptIn(FlowPreview::class)
@Composable
internal fun SettingsGeneralUIContent(
    settings: WorkspaceSettings,
    onIntent: (Intent) -> Unit
) {
    SettingsContent {
        CJDraggableArea(modifier = Modifier, {
            CJToolbar(
                title = stringResource(Res.string.f_settings_general_title),
                onBack = {
                    onIntent(Intent.Back)
                },
                onClose = {
                    onIntent(Intent.Close)
                }
            )
        })
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
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
                    onIntent(Intent.SetLanguage(it.code))
                }
            }
            SelectOption(
                modifier = Modifier,
                text = "Primary color",
                isShowBorder = true
            ) {
                val color = LocalAppTheme.current.primaryColor
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            color = color,
                            shape = RoundedCornerShape(30.dp)
                        )
                        .border(
                            1.dp,
                            color = LocalAppTheme.current.colors.divide,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .clickable { onIntent(Intent.ChangePrimaryColor(color)) }
                )
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
                        onIntent(
                            Intent.SetTheme(
                                colorsType = switch,
                                colors = colors
                            )
                        )
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
            val localDensityState = remember() {
                mutableStateOf(settings.density)
            }
            val localFontScaleState = remember() {
                mutableStateOf(settings.density)
            }
            LaunchedEffect(settings) {
                localDensityState.value = settings.density
                localFontScaleState.value = settings.fontScale
            }
            SelectOption(
                modifier = Modifier,
                text = "Density",
                isShowBorder = true
            ) {
                CJSlider(
                    modifier = Modifier.width(100.dp).align(Alignment.CenterEnd),
                    value = localDensityState.value,
                    valueRange = 0.75f..3f,
                    onValueChange = {
                        localDensityState.value = it
                    }
                )
            }
            SelectOption(
                modifier = Modifier,
                text = "Font scale",
                isShowBorder = true
            ) {
                CJSlider(
                    modifier = Modifier.width(100.dp).align(Alignment.CenterEnd),
                    value = localFontScaleState.value,
                    valueRange = 0.75f..3f,
                    onValueChange = {
                        localFontScaleState.value = it
                    }
                )
            }
            LaunchedEffect(Unit) {
                snapshotFlow {
                    localDensityState.value to localFontScaleState.value
                }
                    .debounce(1000) // wait 1s after last change
                    .distinctUntilChanged()
                    .collect { (density, fontScale) ->
                        onIntent(
                            Intent.SetDensity(
                                density = density,
                                fontScale = fontScale
                            )
                        )
                    }
            }
            val systemDensity = LocalSystemDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = localDensityState.value * systemDensity.density,
                    fontScale = localFontScaleState.value * systemDensity.fontScale
                ),
            ) {
                CJText(text = "Test text")
            }
        }
    }
}