package com.yullg.android.scaffold.app

import android.content.Context
import java.lang.ref.WeakReference

object Scaffold {

    @JvmStatic
    val context: Context
        get() = contextRef.let { _contextRef ->
            if (_contextRef != null) {
                _contextRef.get().let { _context ->
                    if (_context != null) {
                        return _context
                    } else {
                        throw IllegalStateException("Context has been reclaimed")
                    }
                }
            } else {
                throw IllegalStateException("Context not found")
            }
        }

    private var contextRef: WeakReference<Context>? = null

    internal fun activate(context: Context) {
        this.contextRef = WeakReference<Context>(context)
    }

}