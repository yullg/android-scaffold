package com.yullg.android.scaffold.framework

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel

object EmptyViewModel : ViewModel()

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
}