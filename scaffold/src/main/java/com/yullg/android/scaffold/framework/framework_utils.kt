package com.yullg.android.scaffold.framework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

fun <T : ViewModel> defaultViewModelCreator(clazz: Class<T>, owner: ViewModelStoreOwner) =
    ViewModelProvider(owner)[clazz]