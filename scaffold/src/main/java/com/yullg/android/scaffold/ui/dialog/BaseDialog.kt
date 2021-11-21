package com.yullg.android.scaffold.ui.dialog

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

abstract class AbstractDialogHandler<M : DialogMetadata>(activity: FragmentActivity) :
    BaseDialogHandler<M> {

    private val activityRef = WeakReference(activity)
    private var dscs: Pair<DialogShell, CoroutineScope>? = null

    protected val activity: FragmentActivity?
        get() = activityRef.get()

    protected val requireActivity: FragmentActivity
        get() = activityRef.get() ?: throw IllegalStateException("Activity has been reclaimed")

    protected val dialogShell: DialogShell?
        get() = dscs?.first

    protected val coroutineScope: CoroutineScope?
        get() = dscs?.second

    final override fun show(metadata: M) {
        synchronized(this) {
            dscs?.apply {
                updateDialog(first, metadata)
                return
            }
            val dialog = createDialog(metadata) { dismissedDialog ->
                afterDialogDismiss(dismissedDialog)
            }
            dscs = Pair(dialog, MainScope())
            dialog.show(requireActivity)
        }
    }

    final override fun dismiss() {
        synchronized(this) {
            dscs?.first?.let {
                try {
                    it.dismiss(requireActivity)
                } finally {
                    afterDialogDismiss(it)
                }
            }
        }
    }

    final override fun isShowing(): Boolean {
        return dscs?.first?.isShowing(requireActivity) ?: false
    }

    private fun afterDialogDismiss(dismissedDialog: DialogShell) {
        synchronized(this) {
            (dscs?.first === dismissedDialog).let {
                ScaffoldLogger.debug("Dialog dismiss [ ${dscs?.first.hashCode()}, ${dismissedDialog.hashCode()}, $it ]")
                if (it) {
                    try {
                        try {
                            dscs?.second?.cancel()
                        } finally {
                            dscs = null
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
        inbuiltDismissListener: (DialogShell) -> Unit
    ): DialogShell

    protected abstract fun updateDialog(dialog: DialogShell, metadata: M)

}