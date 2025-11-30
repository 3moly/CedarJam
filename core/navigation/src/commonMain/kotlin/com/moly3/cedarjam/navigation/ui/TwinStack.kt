package com.moly3.cedarjam.navigation.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.ChildStack


class Keys(
    var set: Set<Any>
)


@OptIn(InternalDecomposeApi::class)
fun ChildStack<*, *>.getKeys(): Set<String> =
    items.mapTo(HashSet(), Child<*, *>::keyHashString2)

@Composable
private fun SaveableStateHolder.retainStates(currentKeys: Set<String>) {
    val keys = remember(this) { Keys(currentKeys) }

    DisposableEffect(this, currentKeys) {
        keys.set.forEach {
            if (it !in currentKeys) {
                removeState(it)
            }
        }

        keys.set = currentKeys

        onDispose {}
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@InternalDecomposeApi
fun Child<*, *>.keyHashString2(): String {
    val keyHash =  "${configuration::class.simpleName}_${configuration.toString()}"
    return keyHash
}

@OptIn(InternalDecomposeApi::class)
@ExperimentalDecomposeApi
@Composable
fun <C : Any, T : Any> ChildStack2(
    stack: ChildStack<C, T>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>,
    content: @Composable AnimatedVisibilityScope.(child: Child.Created<C, T>) -> Unit,
) {
    val keys = remember(stack.items){
        stack.getKeys()
    }
    val holder = rememberSaveableStateHolder()
    holder.retainStates(keys)

    val anim = animation

    anim(stack = stack, modifier = modifier) { child ->
        holder.SaveableStateProvider(child.keyHashString2()) {
            content(child)
        }
    }
}