package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJButton2
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.launch

@Composable
fun DialogColorPickerUI(dialog: DialogColorPickerService) {
    val scope = rememberCoroutineScope()
    CJDialogGeneric(dialog = dialog) { data ->

        val controller = rememberColorPickerController()
        val colorState = remember { mutableStateOf<Color?>(null) }

        LaunchedEffect(Unit) {
            if (data != null) {
                controller.selectByColor(data, true)
            }
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
                Box(Modifier.fillMaxWidth().height(8.dp).background(controller.selectedColor.value))
                CJButton2(onClick = {
                    if (colorState.value != null) {
                        scope.launch {
                            dialog.setResult(colorState.value)
                        }
                    }

                }) {
                    CJText("Create")
                }
            }
        }
    }
}