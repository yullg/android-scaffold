package com.yullg.android.scaffold.framework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

interface IActivityComponent {

    fun onCreate(savedInstanceState: Bundle?): Unit {}

    fun onRestart(): Unit {}

    fun onStart(): Unit {}

    fun onResume(): Unit {}

    fun onPause(): Unit {}

    fun onStop(): Unit {}

    fun onDestroy(): Unit {}

}

object EmptyAC : IActivityComponent

open class AC<B : ViewDataBinding, M : ViewModel, P : LifecycleObserver>(
    protected val activity: AppCompatActivity,
    protected val dataBindingLayoutId: Int? = null,
    protected val dataBindingVariables: Map<Int, Any?>? = null,
    protected val dataBindingLifecycleOwner: LifecycleOwner? = activity,
    protected val viewModelCreator: (() -> M)? = null,
    protected val viewModelBindingId: Int? = null,
    protected val presenterCreator: (() -> P)? = null,
    protected val presenterLifecycleOwner: LifecycleOwner = activity,
    protected val presenterBindingId: Int? = null
) : IActivityComponent {

    val binding: B get() = _binding!!
    val viewModel: M get() = _viewModel!!
    val presenter: P get() = _presenter!!

    protected var _binding: B? = null
    protected var _viewModel: M? = null
    protected var _presenter: P? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        dataBindingLayoutId?.let {
            _binding = DataBindingUtil.setContentView(activity, it)
            dataBindingVariables?.forEach { (variableId, value) ->
                _binding?.setVariable(variableId, value)
            }
            if (dataBindingLifecycleOwner != null) {
                _binding?.lifecycleOwner = dataBindingLifecycleOwner
            }
        }
        viewModelCreator?.let {
            _viewModel = it()
            viewModelBindingId?.let { variableId ->
                _binding?.setVariable(variableId, _viewModel)
            }
        }
        presenterCreator?.let {
            _presenter = it().also { p ->
                presenterLifecycleOwner.lifecycle.addObserver(p)
            }
            presenterBindingId?.let { variableId ->
                _binding?.setVariable(variableId, _presenter)
            }
        }
    }

}

open class BAC<B : ViewDataBinding>(
    activity: AppCompatActivity,
    dataBindingLayoutId: Int,
    dataBindingVariables: Map<Int, Any?>? = null,
    dataBindingLifecycleOwner: LifecycleOwner? = activity
) : AC<B, ViewModel, LifecycleObserver>(
    activity = activity,
    dataBindingLayoutId = dataBindingLayoutId,
    dataBindingVariables = dataBindingVariables,
    dataBindingLifecycleOwner = dataBindingLifecycleOwner
)

open class MAC<M : ViewModel>(
    activity: AppCompatActivity,
    viewModelCreator: () -> M
) : AC<ViewDataBinding, M, LifecycleObserver>(
    activity = activity,
    viewModelCreator = viewModelCreator
)

open class PAC<P : LifecycleObserver>(
    activity: AppCompatActivity,
    presenterCreator: () -> P,
    presenterLifecycleOwner: LifecycleOwner = activity
) : AC<ViewDataBinding, ViewModel, P>(
    activity = activity,
    presenterCreator = presenterCreator,
    presenterLifecycleOwner = presenterLifecycleOwner
)

open class BMAC<B : ViewDataBinding, M : ViewModel>(
    activity: AppCompatActivity,
    dataBindingLayoutId: Int,
    dataBindingVariables: Map<Int, Any?>? = null,
    dataBindingLifecycleOwner: LifecycleOwner? = activity,
    viewModelCreator: () -> M,
    viewModelBindingId: Int? = null
) : AC<B, M, LifecycleObserver>(
    activity = activity,
    dataBindingLayoutId = dataBindingLayoutId,
    dataBindingVariables = dataBindingVariables,
    dataBindingLifecycleOwner = dataBindingLifecycleOwner,
    viewModelCreator = viewModelCreator,
    viewModelBindingId = viewModelBindingId
)

open class BPAC<B : ViewDataBinding, P : LifecycleObserver>(
    activity: AppCompatActivity,
    dataBindingLayoutId: Int,
    dataBindingVariables: Map<Int, Any?>? = null,
    dataBindingLifecycleOwner: LifecycleOwner? = activity,
    presenterCreator: () -> P,
    presenterLifecycleOwner: LifecycleOwner = activity,
    presenterBindingId: Int? = null
) : AC<B, ViewModel, P>(
    activity = activity,
    dataBindingLayoutId = dataBindingLayoutId,
    dataBindingVariables = dataBindingVariables,
    dataBindingLifecycleOwner = dataBindingLifecycleOwner,
    presenterCreator = presenterCreator,
    presenterLifecycleOwner = presenterLifecycleOwner,
    presenterBindingId = presenterBindingId
)

open class MPAC<M : ViewModel, P : LifecycleObserver>(
    activity: AppCompatActivity,
    viewModelCreator: () -> M,
    presenterCreator: () -> P,
    presenterLifecycleOwner: LifecycleOwner = activity
) : AC<ViewDataBinding, M, P>(
    activity = activity,
    viewModelCreator = viewModelCreator,
    presenterCreator = presenterCreator,
    presenterLifecycleOwner = presenterLifecycleOwner
)