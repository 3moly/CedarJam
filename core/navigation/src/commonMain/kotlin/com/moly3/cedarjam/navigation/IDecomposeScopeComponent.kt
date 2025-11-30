package com.moly3.cedarjam.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeCallback

interface IDecomposeScopeComponent : KoinScopeComponent, KoinComponent, ComponentContext {
    fun onScopeClose(scope: Scope) {

    }
}

fun IDecomposeScopeComponent.componentScope() = lazy { createComponentScope() }

private fun IDecomposeScopeComponent.createComponentScope(): Scope {
    return getKoin().getScopeOrNull(getScopeId()) ?: createScopeForCurrentLifecycle()
}

private fun IDecomposeScopeComponent.createScopeForCurrentLifecycle(): Scope {
    val scopeId = getScopeId()
    val scope = getKoin()
        .createScope(
            scopeId,
            getScopeName(),
            this)
    scope.registerCallback(
        object : ScopeCallback {
            override fun onScopeClose(scope: Scope) =
                this@createScopeForCurrentLifecycle.onScopeClose(scope)
        },
    )
    lifecycle.doOnDestroy {
        if (scope.isNotClosed()) {
            scope.close()
        }
    }
    return scope
}