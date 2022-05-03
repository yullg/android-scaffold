package com.yullg.android.scaffold.ui.dialog

import android.view.View
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 适用于[BottomSheetDialogHandler]的Dialog元数据
 */
interface BottomSheetDialogMetadata : DialogMetadata {

    val cancelable: Boolean

    val showDuration: Long

    val onShowListener: (() -> Unit)?

    val onDismissListener: (() -> Unit)?

}

/**
 * 一个[DialogHandler]的抽象实现，它根据[BottomSheetDialog]创建Dialog，具体实现只需要关心Dialog的视图。
 */
abstract class BottomSheetDialogHandler<M : BottomSheetDialogMetadata>(
    fragmentManager: FragmentManager,
    @StyleableRes private val defStyleAttr: Int = 0,
    @StyleRes private val defStyleRes: Int = 0
) : AbstractDialogHandler<M, BottomSheetDialogShell>(fragmentManager) {

    final override fun createDialogShell(
        metadata: M,
        inbuiltDismissListener: (BottomSheetDialogShell) -> Unit
    ): BottomSheetDialogShell {
        return BottomSheetDialogShell(
            createDialogCallback = { dialogShell ->
                val dialogTheme = dialogShell.requireContext().theme
                    .obtainStyledAttributes(R.styleable.yg_ThemeAttrDeclare)
                    .run {
                        try {
                            getResourceId(defStyleAttr, defStyleRes)
                        } finally {
                            recycle()
                        }
                    }
                BottomSheetDialog(dialogShell.requireContext(), dialogTheme).apply {
                    setContentView(createDialogView(dialogShell, metadata))
                    if (metadata.showDuration > 0 || metadata.onShowListener != null) {
                        setOnShowListener {
                            try {
                                if (metadata.showDuration > 0) {
                                    dialogShellCoroutineScope?.launch {
                                        try {
                                            delay(metadata.showDuration)
                                            dialogShell.dismiss()
                                        } catch (e: Exception) {
                                            ScaffoldLogger.error(
                                                "[Dialog] Cannot dismiss this dialog [$dialogShell]",
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
            },
            dismissDialogCallback = { dialogShell ->
                try {
                    inbuiltDismissListener(dialogShell)
                } finally {
                    metadata.onDismissListener?.invoke()
                }
            }
        ).apply {
            isCancelable = metadata.cancelable
        }
    }

    final override fun updateDialogShell(dialogShell: BottomSheetDialogShell, metadata: M) {
        updateDialogView(dialogShell, metadata)
    }

    protected abstract fun createDialogView(dialogShell: BottomSheetDialogShell, metadata: M): View

    protected abstract fun updateDialogView(dialogShell: BottomSheetDialogShell, metadata: M)

}