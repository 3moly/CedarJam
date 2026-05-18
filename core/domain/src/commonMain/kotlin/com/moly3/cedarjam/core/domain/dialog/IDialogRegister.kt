package com.moly3.cedarjam.core.domain.dialog

interface IDialogRegister {
    fun register(dialog: GlobalDialog<*, *>)
}