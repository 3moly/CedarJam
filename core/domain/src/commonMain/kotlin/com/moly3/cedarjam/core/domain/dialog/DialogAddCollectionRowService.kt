package com.moly3.cedarjam.core.domain.dialog

import com.moly3.cedarjam.core.domain.model.CollectionRowDTO

class DialogAddCollectionRowService(register: IDialogRegister) :
    GlobalDialog<Unit, CollectionRowDTO?>(closeValue = null, register = register)