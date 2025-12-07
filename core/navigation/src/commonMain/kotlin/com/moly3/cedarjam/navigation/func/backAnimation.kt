package com.moly3.cedarjam.navigation.func

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler

//@OptIn(ExperimentalDecomposeApi::class)
//expect fun <C : Any, T : Any> backAnimation(
//    backHandler: BackHandler,
//    onBack: () -> Unit,
//): StackAnimation<C, T>

@OptIn(ExperimentalDecomposeApi::class)
fun <C : Any, T : Any> backAnimation2(
    backHandler: BackHandler,
    onBack: () -> Unit,
    screenWidth: Int
): StackAnimation<C, T> =
    stackAnimation(
        animator = iosLikeSlide(),
        predictiveBackParams = {
            PredictiveBackParams(
                backHandler = backHandler,
                onBack = onBack,
                animatable = { backEvent ->
                    HorizontalPredictiveAnimatable(
                        fromLeft = backEvent.swipeEdge == BackEvent.SwipeEdge.LEFT,
                        screenWidth = screenWidth
                    )
                }
            )
        },
    )

@OptIn(ExperimentalDecomposeApi::class)
class HorizontalPredictiveAnimatable(
    private val fromLeft: Boolean,
    private val screenWidth: Int
) : PredictiveBackAnimatable {

    private val sign = if (fromLeft) 1f else -1f
    private val maxOffsetPx = screenWidth

    private val offsetAnim = Animatable(0f)

    override val enterModifier: Modifier
        get() = Modifier.graphicsLayer {
            //translationX = offsetAnim.value
        }

    override val exitModifier: Modifier
        get() = Modifier.graphicsLayer {
            translationX = offsetAnim.value
        }

    override suspend fun animate(event: BackEvent) {
        // event.progress 0f .. 1f
        offsetAnim.snapTo(event.progress * sign * maxOffsetPx)
    }

    override suspend fun finish() {
        // Complete swipe → fully move out
        offsetAnim.animateTo(sign * maxOffsetPx)
    }

    override suspend fun cancel() {
        // Cancel swipe → restore position
        offsetAnim.animateTo(0f)
    }
}

@OptIn(ExperimentalDecomposeApi::class)
private fun iosLikeSlide(animationSpec: FiniteAnimationSpec<Float> = tween()): StackAnimator =
    stackAnimator(animationSpec = animationSpec) { factor, direction ->
        Modifier
            .then(if (direction.isFront) Modifier else Modifier.fade(factor + 1F))
            .offsetXFactor(factor = if (direction.isFront) factor else factor * 0.5F)
    }

private fun Modifier.fade(factor: Float) =
    drawWithContent {
        drawContent()
        drawRect(color = Color(red = 0F, green = 0F, blue = 0F, alpha = (1F - factor) / 4F))
    }

private fun Modifier.offsetXFactor(factor: Float): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = (placeable.width.toFloat() * factor).toInt(), y = 0)
        }
    }