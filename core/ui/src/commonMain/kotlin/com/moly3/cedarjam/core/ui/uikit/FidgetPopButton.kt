package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.vectors.Tag


@Composable
fun NeumorphicButton(
    modifier: Modifier = Modifier,
    accentColor: Color = Color.Blue,
    isPressed: Boolean = false,
    buttonShape: Shape = CircleShape, // Dynamic shape
    strength: Float = 1f,
    painter: Painter? = null,
    pressedColor: Color = Color.Gray,
    unpressedColor: Color = Color.Blue
) {
    val buttonShapeUpdated by rememberUpdatedState(buttonShape)
    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "press"
    )
    val imageColor by animateColorAsState(if (isPressed) pressedColor else unpressedColor)

    val raised = 1f - progress

    Box(
        modifier = modifier
            .size(100.dp)
            .graphicsLayer {
                shadowElevation = 15f * raised * strength
                shape = buttonShapeUpdated
                clip = false
            }
            .drawBehind {
                val centerOffset = Offset(size.width * 0.358f, size.height * 0.293f)

                // Convert the Generic Shape into a usable Outline for this specific size
                val outline = buttonShapeUpdated.createOutline(size, layoutDirection, this)

                // 1. DRAW YOUR ACCENT COLOR FIRST (Using the dynamic shape)
                drawOutline(
                    outline = outline,
                    color = accentColor
                )

                // 2. OVERLAY THE ORIGINAL GRAY GRADIENT (Preserving shadows)
                drawOutline(
                    outline = outline,
                    brush = Brush.radialGradient(
                        0.0f to Color(0xFFCFCFCF),
                        0.72f to Color(0xFFFCFCFA),
                        1.0f to Color(0xFFFCFCFA),
                        center = centerOffset,
                        radius = size.maxDimension
                    ),
                    blendMode = BlendMode.Multiply
                )
            }
            .innerShadow(shape = buttonShape) {
                this.color = Color.White.copy(alpha = 0.6f * raised)
                this.radius = 2f
                this.offset = Offset(1f, 1f)
            }
            .innerShadow(shape = buttonShape) {
                this.color = Color.Black.copy(alpha = 0.5f * raised)
                this.radius = 2f
                this.offset = Offset(-1f, -1f)
            }
            .innerShadow(shape = buttonShape) {
                this.color = Color.Black.copy(alpha = 0.4f * progress)
                this.radius = 40f * progress
                this.offset = Offset(15f * progress, 15f * progress)
            }
            .clip(buttonShape),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(imageColor)
            )
        }
    }
}

@Preview
@Composable
fun FidgetPoppinPreview() {
    Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {

        var shapeSize by remember { mutableStateOf(12f) }
        var isPressed by remember { mutableStateOf(false) }
        val modifier = Modifier
            .padding(24.dp)
            .align(Alignment.BottomEnd)
            .width(128.dp)
            .height(65.dp)
            .flatClickable({
                isPressed = !isPressed
            })
        CJSlider(modifier = Modifier, value = shapeSize, onValueChange = {
            shapeSize = it
        }, valueRange = 0f..100f)
        NeumorphicButton(
            modifier = modifier,
            isPressed = isPressed,
            buttonShape = RoundedCornerShape(shapeSize.dp),
            accentColor = Color(0xFFFF916D),
            painter = rememberVectorPainter(Tag)
        )
    }
}