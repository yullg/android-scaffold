package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

interface DialogMetadata

abstract class BaseDialog<M : DialogMetadata>(protected val handler: BaseDialogHandler<M>) {

    open fun show() = handler.show(buildMetadata())

    open fun dismiss() = handler.dismiss()

    open fun isShowing() = handler.isShowing()

    open fun <T : DialogTemplate<*>> template(): T {
        return if (handler is DialogTemplateHandler<*>)
            handler.template as T
        else
            throw UnsupportedOperationException("${handler.javaClass.simpleName} is not DialogTemplateHandler")
    }

    protected abstract fun buildMetadata(): M

}

interface BaseDialogHandler<M : DialogMetadata> {

    fun show(metadata: M)

    fun dismiss()

    fun isShowing(): Boolean

}

abstract class AbstractDialogHandler<M : DialogMetadata> : BaseDialogHandler<M> {

    private var dialogAndCoroutineScope: Pair<Dialog, CoroutineScope>? = null

    protected val dialogCoroutineScope: CoroutineScope?
        get() = dialogAndCoroutineScope?.second

    final override fun show(metadata: M) {
        synchronized(this) {
            dialogAndCoroutineScope?.apply {
                updateDialog(first, metadata)
                return
            }
            val dialog = createDialog(metadata) { dismissedDialog ->
                afterDialogDismiss(dismissedDialog)
            }
            dialogAndCoroutineScope = Pair(dialog, MainScope())
            dialog.show()
        }
    }

    final override fun dismiss() {
        synchronized(this) {
            dialogAndCoroutineScope?.first?.let {
                try {
                    it.dismiss()
                } finally {
                    afterDialogDismiss(it)
                }
            }
        }
    }

    final override fun isShowing(): Boolean {
        return dialogAndCoroutineScope?.first?.isShowing ?: false
    }

    private fun afterDialogDismiss(dismissedDialog: DialogInterface) {
        synchronized(this) {
            (dialogAndCoroutineScope?.first === dismissedDialog).let {
                if (ScaffoldLogger.isDebugEnabled()) {
                    ScaffoldLogger.debug("Dialog dismiss [ ${dialogAndCoroutineScope?.first.hashCode()}, ${dismissedDialog.hashCode()}, $it ]")
                }
                if (it) {
                    try {
                        try {
                            dialogAndCoroutineScope?.second?.cancel()
                        } finally {
                            dialogAndCoroutineScope = null
                        }
                    } finally {
                        onDismiss()
                    }
                }
            }
        }
    }

    protected open fun onDismiss() {}

    protected abstract fun createDialog(
        metadata: M,
        inbuiltDismissListener: DialogInterface.OnDismissListener
    ): Dialog

    protected abstract fun updateDialog(dialog: Dialog, metadata: M)

}