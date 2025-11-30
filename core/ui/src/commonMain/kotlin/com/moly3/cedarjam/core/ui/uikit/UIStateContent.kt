package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.UIState

//
//@Composable
//fun <T> UIStateContent(
//    modifier: Modifier = Modifier,
//    state: UIState<T>,
//    loading: (@Composable BoxScope.() -> Unit)? = null,
//    error: (@Composable BoxScope.(UIState.Error.ErrorData) -> Unit)? = null,
//    errorNotSign: (@Composable () -> Unit)? = null,
//    success: @Composable BoxScope.(T) -> Unit
//) {
//    Box(modifier = modifier, contentAlignment = Alignment.Center) {
//        when (state) {
//            is UIState.Error -> {
//                if (state.errorData is UIState.Error.ErrorData.NotSigned &&
//                    errorNotSign != null
//                ) {
//                    errorNotSign()
//                } else if (error != null) {
//                    error(state.errorData)
//                } else {
//                    Text(
//                        modifier = Modifier.align(Alignment.Center),
//                        text = state.errorMessage
//                    )
//                }
//            }
//
//            is UIState.Loading -> {
//                if (loading != null) {
//                    loading()
//                } else {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                }
//            }
//
//            is UIState.Success -> {
//                success(state.data)
//            }
//        }
//    }
//}

@Composable
fun <T, E> UIStateContentNoBox(
    boxModifier: Modifier = Modifier,
    state: UIState<T, E>,
    loading: (@Composable () -> Unit)? = null,
    error: (@Composable (E) -> Unit)? = null,
    success: @Composable (T) -> Unit
) {
    when (state) {
        is UIState.Error -> {
            if (error != null) {
                Box(boxModifier, contentAlignment = Alignment.Center) {
                    error(state.error)
                }
            } else {
                Box(boxModifier, contentAlignment = Alignment.Center) {
                    CJText(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = state.error.toString(),
                    )
                }
            }
        }

        is UIState.Loading -> {
            if (loading != null) {
                loading()
            } else {
                Box(boxModifier, contentAlignment = Alignment.Center) {
                    CJCircularProgressIndicator()
                    //todo CircularProgressIndicator(modifier = Modifier)
                }
            }
        }

        is UIState.Success -> {
            success(state.data)
        }
    }
}

fun <T, E> LazyListScope.UIStateContentLazy(
    state: UIState<T, E>,
    loading: (@Composable () -> Unit)? = null,
    error: @Composable (E) -> Unit = { e ->
        CJText(
            modifier = Modifier.padding(16.dp),
            text = e.toString(),
            color = Color.Red
        )
    },
    success: LazyListScope.(T) -> Unit
) {
    when (state) {
        is UIState.Error -> {
            item {
                error(state.error)
            }
        }

        is UIState.Loading -> {
            item {
                if (loading != null) {
                    loading()
                } else {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        //todo CircularProgressIndicator()
                    }
                }
            }
        }

        is UIState.Success -> {
            success(state.data) // lets you emit items {}
        }
    }
}