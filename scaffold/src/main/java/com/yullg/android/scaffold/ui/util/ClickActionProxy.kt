package com.yullg.android.scaffold.ui.util

import android.text.style.ClickableSpan
import android.view.View
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.core.ActionProxy

open class ClickListenerProxy(
    protected val actionProxy: ActionProxy<Unit>,
    protected val listener: View.OnClickListener
) : View.OnClickListener {

    override fun onClick(v: View?) = actionProxy.run {
        listener.onClick(v)
    }

}

open class ClickableSpanProxy(
    protected val actionProxy: ActionProxy<Unit>,
    protected val listener: (View) -> Unit
) : ClickableSpan() {

    override fun onClick(widget: View) = actionProxy.run {
        listener.invoke(widget)
    }

}

open class ThrottledClickListener(listener: View.OnClickListener) : ClickListenerProxy(
    ScaffoldConfig.UI.clickThrottledActionProxyCreator(),
    listener
)

open class ThrottledClickableSpan(listener: (View) -> Unit) : ClickableSpanProxy(
    ScaffoldConfig.UI.clickThrottledActionProxyCreator(),
    listener
)