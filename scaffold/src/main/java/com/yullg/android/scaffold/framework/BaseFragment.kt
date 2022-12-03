package com.yullg.android.scaffold.framework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yullg.android.scaffold.support.permission.PermissionSupport

abstract class BaseFragment<T : IFragmentComponent> : Fragment() {

    val permissionRequester = PermissionSupport.register(this)

    lateinit var fc: T
        private set

    protected abstract fun newFC(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fc = newFC()
        fc.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return fc.onCreateView(inflater, container, savedInstanceState)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fc.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        fc.onStart()
    }

    override fun onResume() {
        super.onResume()
        fc.onResume()
    }

    override fun onPause() {
        fc.onPause()
        super.onPause()
    }

    override fun onStop() {
        fc.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        fc.onDestroyView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        fc.onDestroy()
        super.onDestroy()
    }

}