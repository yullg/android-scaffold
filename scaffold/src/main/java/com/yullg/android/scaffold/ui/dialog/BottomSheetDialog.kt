package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetDialog as GoogleBottomSheetDialog

interface BottomSheetDialogMetadata : DialogMetadata {

    val cancelable: Boolean

    val showDuration: Long

    val onShowListener: DialogInterface.OnShowListener?

    val onDismissListener: DialogInterface.OnDismissListener?

}

abstract class BottomSheetDialog<M : DialogMetadata, S : BottomSheetDialog<M, S>>(
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

abstract class BottomSheetDialogHandler<M : BottomSheetDialogMetadata>(
    fragmentActivity: FragmentActivity,
    @StyleableRes private val defStyleAttr: Int = 0,
    @StyleRes private val defStyleRes: Int = 0
) : AbstractDialogHandler<M>(fragmentActivity) {

    final override fun createDialog(
        metadata: M,
        inbuiltDismissListener: (DialogFragment) -> Unit
    ): DialogFragment {
        val dialogTheme =
            activity.theme.obtainStyledAttributes(R.styleable.yg_ThemeAttrDeclare).run {
                try {
                    getResourceId(defStyleAttr, defStyleRes)
                } finally {
                    recycle()
                }
            }
        return DialogFragmentImpl(
            createDialogCallback = { dialogFragmentImpl ->
                GoogleBottomSheetDialog(dialogFragmentImpl.requireActivity(), dialogTheme).apply {
                    setContentView(createDialogView(dialogFragmentImpl.requireActivity(), metadata))
                    if (metadata.showDuration > 0 || metadata.onShowListener != null) {
                        setOnShowListener { showedDialog ->
                            try {
                                if (metadata.showDuration > 0) {
                                    dialogCoroutineScope?.launch {
                                        try {
                                            delay(metadata.showDuration)
                                            dialogFragmentImpl.dismiss()
                                        } catch (e: Exception) {
                                            if (ScaffoldLogger.isErrorEnabled()) {
                                                ScaffoldLogger.error(
                                                    "Cannot dismiss this dialog [ $dialogFragmentImpl ]",
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
                }
            },
            dismissDialogCallback = { dialogFragmentImpl, dialogInterface ->
                try {
                    inbuiltDismissListener(dialogFragmentImpl)
                } finally {
                    metadata.onDismissListener?.onDismiss(dialogInterface)
                }
            }
        ).apply {
            isCancelable = metadata.cancelable
        }
    }

    final override fun updateDialog(dialog: DialogFragment, metadata: M) {
        updateDialogView(activity, metadata)
    }

    protected abstract fun createDialogView(context: Context, metadata: M): View

    protected abstract fun updateDialogView(context: Context, metadata: M)

}