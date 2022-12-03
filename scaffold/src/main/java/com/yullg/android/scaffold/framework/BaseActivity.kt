package com.yullg.android.scaffold.framework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yullg.android.scaffold.support.permission.PermissionSupport

abstract class BaseActivity<T : IActivityComponent> : AppCompatActivity() {

    val permissionRequester = PermissionSupport.register(this)

    lateinit var ac: T
        private set

    protected abstract fun newAC(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ac = newAC()
        ac.onCreate(savedInstanceState)
    }

    override fun onRestart() {
        super.onRestart()
        ac.onRestart()
    }

    override fun onStart() {
        super.onStart()
        ac.onStart()
    }

    override fun onResume() {
        super.onResume()
        ac.onResume()
    }

    override fun onPause() {
        ac.onPause()
        super.onPause()
    }

    override fun onStop() {
        ac.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        ac.onDestroy()
        super.onDestroy()
    }

}