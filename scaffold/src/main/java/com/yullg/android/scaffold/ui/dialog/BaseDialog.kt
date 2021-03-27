package com.yullg.android.scaffold.ui.dialog

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.ref.WeakReference

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

abstract class AbstractDialogHandler<M : DialogMetadata>(fragmentActivity: FragmentActivity) :
    BaseDialogHandler<M> {

    private val activityRef = WeakReference(fragmentActivity)
    private var dialogAndCoroutineScope: Pair<DialogFragment, CoroutineScope>? = null

    protected val activity: FragmentActivity
        get() = activityRef.get() ?: throw IllegalStateException("The activity has been cleared")

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
            dialog.show(activity.supportFragmentManager, null)
        }
    }

    final override fun dismiss() {
        synchronized(this) {
            dialogAndCoroutineScope?.first?.let {
                try {
                    it.dismissAllowingStateLoss()
                } finally {
                    afterDialogDismiss(it)
                }
            }
        }
    }

    final override fun isShowing(): Boolean {
        return dialogAndCoroutineScope?.first?.dialog?.isShowing ?: false
    }

    private fun afterDialogDismiss(dismissedDialog: DialogFragment) {
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
        inbuiltDismissListener: (DialogFragment) -> Unit
    ): DialogFragment

    protected abstract fun updateDialog(dialog: DialogFragment, metadata: M)

}