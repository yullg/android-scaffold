package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.annotation.UiContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

interface MaterialDialogMetadata : DialogMetadata {

    val cancelable: Boolean

    val showDuration: Long

    val onShowListener: DialogInterface.OnShowListener?

    val onDismissListener: DialogInterface.OnDismissListener?

}

abstract class MaterialDialog<M : DialogMetadata, S : MaterialDialog<M, S>>(
    handler: BaseDialogHandler<M>
) : BaseDialog<M>(handler) {

    protected var cancelable: Boolean? = null
    protected var showDuration: Long? = null
    protected var onShowListener: DialogInterface.OnShowListener? = null
    protected var onDismissListener: DialogInterface.OnDismissListener? = null

    private val self get() = this as S

    open fun setCancelable(cancelable: Boolean?): S {
        this.cancelable = cancelable
        return self
    }

    open fun setShowDuration(showDuration: Long?): S {
        this.showDuration = showDuration
        return self
    }

    open fun setOnShowListener(onShowListener: ((S) -> Unit)?): S {
        this.onShowListener = onShowListener?.let {
            DialogInterface.OnShowListener { _ -> it(self) }
        }
        return self
    }

    open fun setOnDismissListener(onDismissListener: ((S) -> Unit)?): S {
        this.onDismissListener = onDismissListener?.let {
            DialogInterface.OnDismissListener { _ -> it(self) }
        }
        return self
    }

    @CallSuper
    open fun resetMetadata(): S {
        cancelable = null
        showDuration = null
        onShowListener = null
        onDismissListener = null
        return self
    }

}

abstract class MaterialDialogHandler<M : MaterialDialogMetadata>(
    @UiContext context: Context,
    @StyleableRes private val defStyleAttr: Int = 0,
    @StyleRes private val defStyleRes: Int = 0
) : AbstractDialogHandler<M>() {

    private val contextRef = WeakReference(context)

    final override fun createDialog(
        metadata: M,
        inbuiltDismissListener: DialogInterface.OnDismissListener
    ): Dialog {
        val context =
            contextRef.get() ?: throw IllegalStateException("The context has been cleared")
        val dialogTheme =
            context.theme.obtainStyledAttributes(R.styleable.yg_ThemeAttrDeclare).run {
                try {
                    getResourceId(defStyleAttr, defStyleRes)
                } finally {
                    recycle()
                }
            }
        val dialog = MaterialAlertDialogBuilder(context, dialogTheme)
            .setView(createDialogView(context, metadata))
            .setCancelable(metadata.cancelable)
            .setOnDismissListener { dismissedDialog ->
                try {
                    inbuiltDismissListener.onDismiss(dismissedDialog)
                } finally {
                    metadata.onDismissListener?.onDismiss(dismissedDialog)
                }
            }
            .create()
        if (metadata.showDuration > 0 || metadata.onShowListener != null) {
            dialog.setOnShowListener { showedDialog ->
                try {
                    if (metadata.showDuration > 0) {
                        dialogCoroutineScope?.launch {
                            try {
                                delay(metadata.showDuration)
                                showedDialog.dismiss()
                            } catch (e: Exception) {
                                if (ScaffoldLogger.isErrorEnabled()) {
                                    ScaffoldLogger.error(
                                        "Cannot dismiss this dialog [ $showedDialog ]",
                                        e
                                    )
                                }
                            }
                        }
                    }
                } finally {
                    metadata.onShowListener?.onShow(showedDialog)
                }
            }
        }
        return dialog
    }

    final override fun updateDialog(dialog: Dialog, metadata: M) {
        val context =
            contextRef.get() ?: throw IllegalStateException("The context has been cleared")
        updateDialogView(context, metadata)
    }

    protected abstract fun createDialogView(context: Context, metadata: M): View

    protected abstract fun updateDialogView(context: Context, metadata: M)

}