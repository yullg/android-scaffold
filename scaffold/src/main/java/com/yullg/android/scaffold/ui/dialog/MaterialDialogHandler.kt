package com.yullg.android.scaffold.ui.dialog

import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 适用于[MaterialDialogHandler]的Dialog元数据
 */
interface MaterialDialogMetadata : DialogMetadata {

    val cancelable: Boolean

    val showDuration: Long

    val onShowListener: (() -> Unit)?

    val onDismissListener: (() -> Unit)?

}

/**
 * 一个[DialogHandler]的抽象实现，它根据[MaterialAlertDialogBuilder]创建Dialog，具体实现只需要关心Dialog的视图。
 */
abstract class MaterialDialogHandler<M : MaterialDialogMetadata>(
    fragmentManager: FragmentManager,
    @StyleRes private val themeResId: Int = 0
) : AbstractDialogHandler<M, NormalDialogShell>(fragmentManager) {

    final override fun createDialogShell(
        metadata: M,
        inbuiltDismissListener: (NormalDialogShell) -> Unit
    ): NormalDialogShell {
        return NormalDialogShell(
            createDialogCallback = { dialogShell ->
                MaterialAlertDialogBuilder(dialogShell.requireContext(), themeResId)
                    .setView(createDialogView(dialogShell, metadata))
                    .create()
                    .apply {
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

    final override fun updateDialogShell(dialogShell: NormalDialogShell, metadata: M) {
        updateDialogView(dialogShell, metadata)
    }

    protected abstract fun createDialogView(dialogShell: NormalDialogShell, metadata: M): View

    protected abstract fun updateDialogView(dialogShell: NormalDialogShell, metadata: M)

}