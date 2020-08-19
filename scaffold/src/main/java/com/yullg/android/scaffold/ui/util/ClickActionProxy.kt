package com.yullg.android.scaffold.ui.util

import android.text.style.ClickableSpan
import android.view.View
import com.yullg.android.scaffold.ui.UIConfig

open class ClickListenerProxy(
    protected val actionProxy: ActionProxy,
    protected val listener: View.OnClickListener
) : View.OnClickListener {

    override fun onClick(v: View?) = actionProxy.run {
        listener.onClick(v)
    }

}

open class ClickableSpanProxy(
    protected val actionProxy: ActionProxy,
    protected val listener: (View) -> Unit
) : ClickableSpan() {

    override fun onClick(widget: View) = actionProxy.run {
        listener.invoke(widget)
    }

}

open class ThrottledClickListener(listener: View.OnClickListener) : ClickListenerProxy(
    UIConfig.throttledActionProxyCreator(),
    listener
)

open class ThrottledClickableSpan(listener: (View) -> Unit) : ClickableSpanProxy(
    UIConfig.throttledActionProxyCreator(),
    listener
)