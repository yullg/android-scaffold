package com.yullg.android.scaffold.helper

import android.widget.Toast
import com.yullg.android.scaffold.app.Scaffold

object ToastHelper {

    fun showShort(resId: Int) = show(resId, Toast.LENGTH_SHORT)

    fun showShort(text: CharSequence) = show(text, Toast.LENGTH_SHORT)

    fun showLong(resId: Int) = show(resId, Toast.LENGTH_LONG)

    fun showLong(text: CharSequence) = show(text, Toast.LENGTH_LONG)

    private var toast: Toast? = null

    private fun show(resId: Int, duration: Int) {
        synchronized(this) {
            toast?.cancel()
            toast = Toast.makeText(Scaffold.context, resId, duration)
            toast?.show()
        }
    }

    private fun show(text: CharSequence, duration: Int) {
        synchronized(this) {
            toast?.cancel()
            toast = Toast.makeText(Scaffold.context, text, duration)
            toast?.show()
        }
    }

}