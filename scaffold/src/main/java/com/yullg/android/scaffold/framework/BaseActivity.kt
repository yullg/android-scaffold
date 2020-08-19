package com.yullg.android.scaffold.framework

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.yullg.android.scaffold.ui.UIConfig
import com.yullg.android.scaffold.ui.util.ActionProxy
import com.yullg.android.scaffold.ui.util.ClickListenerProxy
import com.yullg.android.scaffold.ui.util.ClickableSpanProxy

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

    open val sharedThrottledActionProxy: ActionProxy by lazy {
        UIConfig.sharedThrottledActionProxyCreator()
    }

    open fun newSharedThrottledClickListener(listener: View.OnClickListener): ClickListenerProxy =
        ClickListenerProxy(sharedThrottledActionProxy, listener)

    open fun newSharedThrottledClickableSpan(listener: (View) -> Unit): ClickableSpanProxy =
        ClickableSpanProxy(sharedThrottledActionProxy, listener)

}