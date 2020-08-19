package com.yullg.android.scaffold.framework

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

object EmptyPresenter : LifecycleObserver

open class BasePresenter : DefaultLifecycleObserver, LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    }

}

open class BaseActivityPresenter<T : AppCompatActivity>(protected val activity: T) :
    BasePresenter()

open class BaseFragmentPresenter<T : Fragment>(protected val fragment: T) :
    BasePresenter()