package com.yullg.android.scaffold.framework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

interface IFragmentComponent {

    fun onCreate(savedInstanceState: Bundle?): Unit {}

    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit {}

    fun onStart(): Unit {}

    fun onResume(): Unit {}

    fun onPause(): Unit {}

    fun onStop(): Unit {}

    fun onDestroyView(): Unit {}

    fun onDestroy(): Unit {}

}

object EmptyFC : IFragmentComponent

open class FC<B : ViewDataBinding, M : ViewModel, P : LifecycleObserver>(
    protected val fragment: Fragment,
    protected val dataBindingLayoutId: Int? = null,
    protected val dataBindingVariables: Map<Int, Any?>? = null,
    protected val dataBindingLifecycleOwner: LifecycleOwner? = fragment,
    protected val viewModelCreator: (() -> M)? = null,
    protected val viewModelBindingId: Int? = null,
    protected val presenterCreator: (() -> P)? = null,
    protected val presenterLifecycleOwner: LifecycleOwner = fragment,
    protected val presenterBindingId: Int? = null
) : IFragmentComponent {

    val binding: B get() = _binding!!
    val viewModel: M get() = _viewModel!!
    val presenter: P get() = _presenter!!

    protected var _binding: B? = null
    protected var _viewModel: M? = null
    protected var _presenter: P? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBindingLayoutId?.let {
            _binding = DataBindingUtil.inflate(inflater, it, container, false)
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
        return _binding?.root
    }

}

open class BFC<B : ViewDataBinding>(
    fragment: Fragment,
    dataBindingLayoutId: Int,
    dataBindingVariables: Map<Int, Any?>? = null,
    dataBindingLifecycleOwner: LifecycleOwner? = fragment
) : FC<B, ViewModel, LifecycleObserver>(
    fragment = fragment,
    dataBindingLayoutId = dataBindingLayoutId,
    dataBindingVariables = dataBindingVariables,
    dataBindingLifecycleOwner = dataBindingLifecycleOwner
)

open class MFC<M : ViewModel>(
    fragment: Fragment,
    viewModelCreator: () -> M
) : FC<ViewDataBinding, M, LifecycleObserver>(
    fragment = fragment,
    viewModelCreator = viewModelCreator
)

open class PFC<P : LifecycleObserver>(
    fragment: Fragment,
    presenterCreator: () -> P,
    presenterLifecycleOwner: LifecycleOwner = fragment
) : FC<ViewDataBinding, ViewModel, P>(
    fragment = fragment,
    presenterCreator = presenterCreator,
    presenterLifecycleOwner = presenterLifecycleOwner
)

open class BMFC<B : ViewDataBinding, M : ViewModel>(
    fragment: Fragment,
    dataBindingLayoutId: Int,
    dataBindingVariables: Map<Int, Any?>? = null,
    dataBindingLifecycleOwner: LifecycleOwner? = fragment,
    viewModelCreator: () -> M,
    viewModelBindingId: Int? = null
) : FC<B, M, LifecycleObserver>(
    fragment = fragment,
    dataBindingLayoutId = dataBindingLayoutId,
    dataBindingVariables = dataBindingVariables,
    dataBindingLifecycleOwner = dataBindingLifecycleOwner,
    viewModelCreator = viewModelCreator,
    viewModelBindingId = viewModelBindingId
)

open class BPFC<B : ViewDataBinding, P : LifecycleObserver>(
    fragment: Fragment,
    dataBindingLayoutId: Int,
    dataBindingVariables: Map<Int, Any?>? = null,
    dataBindingLifecycleOwner: LifecycleOwner? = fragment,
    presenterCreator: () -> P,
    presenterLifecycleOwner: LifecycleOwner = fragment,
    presenterBindingId: Int? = null
) : FC<B, ViewModel, P>(
    fragment = fragment,
    dataBindingLayoutId = dataBindingLayoutId,
    dataBindingVariables = dataBindingVariables,
    dataBindingLifecycleOwner = dataBindingLifecycleOwner,
    presenterCreator = presenterCreator,
    presenterLifecycleOwner = presenterLifecycleOwner,
    presenterBindingId = presenterBindingId
)

open class MPFC<M : ViewModel, P : LifecycleObserver>(
    fragment: Fragment,
    viewModelCreator: () -> M,
    presenterCreator: () -> P,
    presenterLifecycleOwner: LifecycleOwner = fragment
) : FC<ViewDataBinding, M, P>(
    fragment = fragment,
    viewModelCreator = viewModelCreator,
    presenterCreator = presenterCreator,
    presenterLifecycleOwner = presenterLifecycleOwner
)