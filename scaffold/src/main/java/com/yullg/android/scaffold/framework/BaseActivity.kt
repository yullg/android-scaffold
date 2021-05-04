package com.yullg.android.scaffold.framework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity<T : IActivityComponent> : AppCompatActivity() {

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