package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.moly3.cedarjam.core.domain.dialog.DialogAppSettingsService
import com.moly3.cedarjam.core.domain.model.AppSettings
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.changeLanguage
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.WindowSize
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJButton2
import com.moly3.cedarjam.core.ui.uikit.CJDialogGenericAdaptive
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.common_typ
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

//import androidx.compose.ui.
@Composable
fun DialogAppSettingsUI(dialog: DialogAppSettingsService) {
    val windowSize by rememberWindowSize()

    val scope = rememberCoroutineScope()
    CJDialogGenericAdaptive(dialog = dialog) { data ->
        if (windowSize == WindowSize.Compact) {
            val controller = rememberColorPickerController()
            val colorState = remember { mutableStateOf<Color?>(null) }

            LaunchedEffect(Unit) {
                controller.selectByColor(data.theme.primaryColor, true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    CJText("select color")
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(10.dp),
                        controller = controller,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            // do something
                            colorState.value = colorEnvelope.color
                        }
                    )
                    BrightnessSlider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .height(35.dp),
                        controller = controller,
                    )
                    Box(
                        Modifier.fillMaxWidth().height(8.dp)
                            .background(controller.selectedColor.value)
                    )
                    CJButton2(onClick = {
                        colorState.value?.let {
                            scope.launch {
                                dialog.setResult(data.copy(theme = data.theme.copy(primaryColor = it)))
                            }
                        }
                    }) {
                        CJText("Create")
                    }
                }
            }
        } else {
            val appTheme = LocalAppTheme.current.colors
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .flatClickable {
                        scope.launch {
                            dialog.cancel()
                        }
                    }
            ) {
                Row(
                    Modifier
                        .padding(vertical = 60.dp, horizontal = 100.dp)
                        .fillMaxSize()
                        .background(appTheme.backgroundPrimary, shape = RoundedCornerShape(12.dp))
                        .border(volumedBorderStroke, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .flatClickable {}
                ) {
                    Box(
                        Modifier
                            .width(250.dp)
                            .fillMaxHeight()
                            .background(appTheme.backgroundSecondary)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CJText(text = stringResource(Res.string.common_typ))
                            CJButton(text = "change language") {
                                changeLanguage("ru2")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DialogAppSettingsUIPreview() {
    val scope = rememberCoroutineScope()
    val dialog = remember {
        val service = DialogAppSettingsService()
        scope.launch {
            service.open(AppSettings.defaultSettings)
        }
        service
    }
    DialogAppSettingsUI(dialog)
}