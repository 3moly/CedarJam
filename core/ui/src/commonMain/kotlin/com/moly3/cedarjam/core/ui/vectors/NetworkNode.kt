package com.moly3.cedarjam.core.ui.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val NetworkNode: ImageVector
    get() {
        if (_NetworkNode != null) {
            return _NetworkNode!!
        }
        _NetworkNode = ImageVector.Builder(
            name = "NetworkNode",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5.5f, 22f)
                quadToRelative(-1.45f, 0f, -2.475f, -1.025f)
                reflectiveQuadTo(2f, 18.5f)
                quadToRelative(0f, -1.45f, 1.025f, -2.475f)
                reflectiveQuadTo(5.5f, 15f)
                quadToRelative(0.45f, 0f, 0.875f, 0.112f)
                reflectiveQuadToRelative(0.8f, 0.313f)
                lineTo(11f, 11.6f)
                lineTo(11f, 8.85f)
                quadToRelative(-1.1f, -0.325f, -1.8f, -1.238f)
                reflectiveQuadTo(8.5f, 5.5f)
                quadToRelative(0f, -1.45f, 1.025f, -2.475f)
                reflectiveQuadTo(12f, 2f)
                quadToRelative(1.45f, 0f, 2.475f, 1.025f)
                reflectiveQuadTo(15.5f, 5.5f)
                quadToRelative(0f, 1.2f, -0.7f, 2.113f)
                reflectiveQuadTo(13f, 8.85f)
                verticalLineToRelative(2.75f)
                lineToRelative(3.85f, 3.825f)
                quadToRelative(0.375f, -0.2f, 0.788f, -0.313f)
                reflectiveQuadTo(18.5f, 15f)
                quadToRelative(1.45f, 0f, 2.475f, 1.025f)
                reflectiveQuadTo(22f, 18.5f)
                quadToRelative(0f, 1.45f, -1.025f, 2.475f)
                reflectiveQuadTo(18.5f, 22f)
                quadToRelative(-1.45f, 0f, -2.475f, -1.025f)
                reflectiveQuadTo(15f, 18.5f)
                quadToRelative(0f, -0.45f, 0.112f, -0.875f)
                reflectiveQuadToRelative(0.313f, -0.8f)
                lineTo(12f, 13.4f)
                lineToRelative(-3.425f, 3.425f)
                quadToRelative(0.2f, 0.375f, 0.313f, 0.8f)
                reflectiveQuadTo(9f, 18.5f)
                quadToRelative(0f, 1.45f, -1.025f, 2.475f)
                reflectiveQuadTo(5.5f, 22f)
                close()
                moveTo(18.5f, 20f)
                quadToRelative(0.625f, 0f, 1.063f, -0.438f)
                reflectiveQuadTo(20f, 18.5f)
                quadToRelative(0f, -0.625f, -0.438f, -1.063f)
                reflectiveQuadTo(18.5f, 17f)
                quadToRelative(-0.625f, 0f, -1.063f, 0.438f)
                reflectiveQuadTo(17f, 18.5f)
                quadToRelative(0f, 0.625f, 0.438f, 1.063f)
                reflectiveQuadTo(18.5f, 20f)
                close()
                moveTo(12f, 7f)
                quadToRelative(0.625f, 0f, 1.063f, -0.438f)
                reflectiveQuadTo(13.5f, 5.5f)
                quadToRelative(0f, -0.625f, -0.438f, -1.063f)
                reflectiveQuadTo(12f, 4f)
                quadToRelative(-0.625f, 0f, -1.063f, 0.438f)
                reflectiveQuadTo(10.5f, 5.5f)
                quadToRelative(0f, 0.625f, 0.438f, 1.063f)
                reflectiveQuadTo(12f, 7f)
                close()
                moveTo(5.5f, 20f)
                quadToRelative(0.625f, 0f, 1.063f, -0.438f)
                reflectiveQuadTo(7f, 18.5f)
                quadToRelative(0f, -0.625f, -0.438f, -1.063f)
                reflectiveQuadTo(5.5f, 17f)
                quadToRelative(-0.625f, 0f, -1.063f, 0.438f)
                reflectiveQuadTo(4f, 18.5f)
                quadToRelative(0f, 0.625f, 0.438f, 1.063f)
                reflectiveQuadTo(5.5f, 20f)
                close()
            }
        }.build()

        return _NetworkNode!!
    }

@Suppress("ObjectPropertyName")
private var _NetworkNode: ImageVector? = null