package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetDialog as GoogleBottomSheetDialog

interface BottomSheetDialogMetadata : DialogMetadata {

    val cancelable: Boolean

    val showDuration: Long

    val onShowListener: (() -> Unit)?

    val onDismissListener: (() -> Unit)?

}

abstract class BottomSheetDialog<M : DialogMetadata, S : BottomSheetDialog<M, S>>(
    handler: BaseDialogHandler<M>
) : BaseDialog<M>(handler) {

    protected var cancelable: Boolean? = null
    protected var showDuration: Long? = null
    protected var onShowListener: (() -> Unit)? = null
    protected var onDismissListener: (() -> Unit)? = null

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
            { it(self) }
        }
        return self
    }

    open fun setOnDismissListener(onDismissListener: ((S) -> Unit)?): S {
        this.onDismissListener = onDismissListener?.let {
            { it(self) }
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

abstract class BottomSheetDialogHandler<M : BottomSheetDialogMetadata>(
    activity: FragmentActivity,
    @StyleableRes private val defStyleAttr: Int = 0,
    @StyleRes private val defStyleRes: Int = 0
) : AbstractDialogHandler<M>(activity) {

    final override fun createDialog(
        metadata: M,
        inbuiltDismissListener: (DialogShell) -> Unit
    ): DialogShell {
        val dialogTheme =
            requireActivity.theme.obtainStyledAttributes(R.styleable.yg_ThemeAttrDeclare).run {
                try {
                    getResourceId(defStyleAttr, defStyleRes)
                } finally {
                    recycle()
                }
            }
        return BottomSheetDialogShell().apply {
            createDialogCallback = { dialogShell ->
                GoogleBottomSheetDialog(dialogShell.requireContext(), dialogTheme).apply {
                    setContentView(createDialogView(dialogShell.requireContext(), metadata))
                    if (metadata.showDuration > 0 || metadata.onShowListener != null) {
                        setOnShowListener {
                            try {
                                if (metadata.showDuration > 0) {
                                    coroutineScope?.launch {
                                        try {
                                            delay(metadata.showDuration)
                                            dialogShell.dismissAllowingStateLoss()
                                        } catch (e: Exception) {
                                            ScaffoldLogger.error(
                                                "Cannot dismiss this dialog [ $dialogShell ]",
                                                e
                                            )
                                        }
                                    }
                                }
                            } finally {
                                metadata.onShowListener?.invoke()
                            }
                        }
                    }
                }
            }
            dismissDialogCallback = { dialogShell ->
                try {
                    inbuiltDismissListener(dialogShell)
                } finally {
                    metadata.onDismissListener?.invoke()
                }
            }
            isCancelable = metadata.cancelable
        }
    }

    final override fun updateDialog(dialog: DialogShell, metadata: M) {
        updateDialogView(requireActivity, metadata)
    }

    protected abstract fun createDialogView(context: Context, metadata: M): View

    protected abstract fun updateDialogView(context: Context, metadata: M)

}