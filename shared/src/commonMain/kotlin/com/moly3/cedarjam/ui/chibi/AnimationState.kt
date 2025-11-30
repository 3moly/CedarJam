package com.moly3.cedarjam.ui.chibi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.uikit.CJButton
import kotlin.math.sin
import kotlin.random.Random

enum class AnimationState {
    IDLE, EATING, WAVING
}

data class SpriteTransform(
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f
)

@Composable
fun ChibiCharacter() {
    var currentState by remember { mutableStateOf(AnimationState.IDLE) }
    var timeMillis by remember { mutableStateOf(0L) }

    // Blinking state
    var isBlinking by remember { mutableStateOf(false) }

    // Pupil position (-1 left, 0 center, 1 right)
    var pupilTargetX by remember { mutableStateOf(0f) }
    var pupilCurrentX by remember { mutableStateOf(0f) }

    // Separate coroutine for blinking
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(Random.nextLong(2000, 5000))
            isBlinking = true
            kotlinx.coroutines.delay(150)
            isBlinking = false
        }
    }

    // Separate coroutine for pupil movement
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(Random.nextLong(1500, 4000))
            pupilTargetX = when (Random.nextInt(3)) {
                0 -> -1f // Left
                1 -> 0f  // Center
                else -> 1f // Right
            }
        }
    }

    // Frame-based animation loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frameTime ->
                timeMillis = frameTime

                // Smooth pupil interpolation
                pupilCurrentX += (pupilTargetX - pupilCurrentX) * 0.05f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Control buttons
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CJButton(text= "Idle",onClick = { currentState = AnimationState.IDLE })
            CJButton(text= "Eating",onClick = { currentState = AnimationState.EATING })
            CJButton(text= "Waving",onClick = { currentState = AnimationState.WAVING })
        }

        // Character canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            val time = timeMillis / 1000f

            // Calculate body sway for idle
            val bodySway = when (currentState) {
                AnimationState.IDLE -> sin(time * 0.8f) * 8f
                else -> 0f
            }

            val bodyRotation = when (currentState) {
                AnimationState.IDLE -> sin(time * 0.8f) * 2f
                else -> 0f
            }

            // Draw legs (static, bottom layer)
            drawSprite(
                name = "Left Leg",
                x = centerX - 40f,
                y = centerY + 80f,
                color = Color(0xFFFFB6C1)
            )
            drawSprite(
                name = "Right Leg",
                x = centerX + 40f,
                y = centerY + 80f,
                color = Color(0xFFFFB6C1)
            )

            // Draw body (with sway)
            translate(centerX + bodySway, centerY) {
                rotate(bodyRotation) {
                    drawSprite(
                        name = "Body",
                        x = 0f,
                        y = 0f,
                        color = Color(0xFFFFE4E1),
                        width = 100f,
                        height = 120f
                    )
                }

                // Draw arms
                val (leftArmRotation, rightArmRotation) = when (currentState) {
                    AnimationState.IDLE -> Pair(
                        sin(time * 1.2f) * 5f,
                        sin(time * 1.2f + 1f) * 5f
                    )

                    AnimationState.EATING -> Pair(
                        -45f + sin(time * 3f) * 5f,
                        -45f + sin(time * 3f + 0.5f) * 5f
                    )

                    AnimationState.WAVING -> Pair(
                        sin(time * 4f) * 30f - 20f,
                        -120f + sin(time * 8f) * 15f
                    )
                }

                // Left arm
                translate(-45f, -20f) {
                    rotate(leftArmRotation) {
                        drawSprite(
                            name = "L Upper Arm",
                            x = 0f,
                            y = 0f,
                            color = Color(0xFFFFE4E1),
                            width = 25f,
                            height = 40f
                        )
                        translate(0f, 35f) {
                            rotate(sin(time * 2f) * 10f) {
                                drawSprite(
                                    name = "L Lower Arm",
                                    x = 0f,
                                    y = 0f,
                                    color = Color(0xFFFFE4E1),
                                    width = 25f,
                                    height = 35f
                                )
                            }
                        }
                    }
                }

                // Right arm
                translate(45f, -20f) {
                    rotate(rightArmRotation) {
                        drawSprite(
                            name = "R Upper Arm",
                            x = 0f,
                            y = 0f,
                            color = Color(0xFFFFE4E1),
                            width = 25f,
                            height = 40f
                        )
                        translate(0f, 35f) {
                            rotate(sin(time * 2f + 1f) * 10f) {
                                drawSprite(
                                    name = "R Lower Arm",
                                    x = 0f,
                                    y = 0f,
                                    color = Color(0xFFFFE4E1),
                                    width = 25f,
                                    height = 35f
                                )
                            }
                        }
                    }
                }

                // Draw head
                translate(0f, -70f) {
                    val headBob = sin(time * 1.5f) * 2f
                    translate(0f, headBob) {
                        // Cat ears
                        drawSprite(
                            name = "L Ear",
                            x = -35f,
                            y = -35f,
                            color = Color(0xFFFFB6C1),
                            width = 30f,
                            height = 35f
                        )
                        drawSprite(
                            name = "R Ear",
                            x = 35f,
                            y = -35f,
                            color = Color(0xFFFFB6C1),
                            width = 30f,
                            height = 35f
                        )

                        // Head
                        drawSprite(
                            name = "Head",
                            x = 0f,
                            y = 0f,
                            color = Color(0xFFFFE4E1),
                            width = 80f,
                            height = 90f
                        )

                        // Eyes
                        val pupilOffsetX = pupilCurrentX * 4f

                        if (isBlinking) {
                            // Closed eyes
                            drawSprite(
                                name = "L Closed",
                                x = -18f,
                                y = 5f,
                                color = Color(0xFF8B4513),
                                width = 20f,
                                height = 4f
                            )
                            drawSprite(
                                name = "R Closed",
                                x = 18f,
                                y = 5f,
                                color = Color(0xFF8B4513),
                                width = 20f,
                                height = 4f
                            )
                        } else {
                            // Open eyes
                            drawSprite(
                                name = "L Eye",
                                x = -18f,
                                y = 5f,
                                color = Color.White,
                                width = 20f,
                                height = 22f
                            )
                            drawSprite(
                                name = "R Eye",
                                x = 18f,
                                y = 5f,
                                color = Color.White,
                                width = 20f,
                                height = 22f
                            )

                            // Pupils
                            drawSprite(
                                name = "L Pupil",
                                x = -18f + pupilOffsetX,
                                y = 8f,
                                color = Color(0xFF4A4A4A),
                                width = 10f,
                                height = 12f
                            )
                            drawSprite(
                                name = "R Pupil",
                                x = 18f + pupilOffsetX,
                                y = 8f,
                                color = Color(0xFF4A4A4A),
                                width = 10f,
                                height = 12f
                            )

                            // Eyelashes
                            drawSprite(
                                name = "L Lash",
                                x = -18f,
                                y = -2f,
                                color = Color(0xFF8B4513),
                                width = 22f,
                                height = 6f
                            )
                            drawSprite(
                                name = "R Lash",
                                x = 18f,
                                y = -2f,
                                color = Color(0xFF8B4513),
                                width = 22f,
                                height = 6f
                            )
                        }

                        // Mouth
                        val mouthState = when (currentState) {
                            AnimationState.EATING -> {
                                if (sin(time * 6f) > 0) 8f else 3f
                            }

                            else -> 3f
                        }

                        drawSprite(
                            name = "Mouth",
                            x = 0f,
                            y = 25f,
                            color = Color(0xFFFF69B4),
                            width = 25f,
                            height = mouthState
                        )
                    }
                }
            }
        }
    }
}

fun DrawScope.drawSprite(
    name: String,
    x: Float,
    y: Float,
    color: Color,
    width: Float = 40f,
    height: Float = 40f,
    rotation: Float = 0f
) {
    // Draw a placeholder rectangle for the sprite
    // In production, replace with actual image loading
    translate(x - width / 2, y - height / 2) {
        rotate(rotation, pivot = Offset(width / 2, height / 2)) {
            drawRect(
                color = color,
                size = androidx.compose.ui.geometry.Size(width, height)
            )
        }
    }
}