package com.yullg.android.scaffold.ui.dialog

import android.view.View
import androidx.annotation.StyleRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
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
 * 一个[DialogHandler]的抽象实现，它使用[BottomSheetDialog]创建Dialog，具体实现只需要关心Dialog的视图。
 */
abstract class BottomSheetDialogHandler<M : BottomSheetDialogMetadata>(
    fragmentManager: FragmentManager,
    @StyleRes private val themeResId: Int = 0
) : AbstractDialogHandler<M, BottomSheetPlatformDialogWrapper>(fragmentManager) {

    final override fun createDialog(
        metadata: M,
        inbuiltDismissListener: (BottomSheetPlatformDialogWrapper) -> Unit
    ): BottomSheetPlatformDialogWrapper {
        return BottomSheetPlatformDialogWrapper(
            createDialogCallback = { platformDialogWrapper ->
                platformDialogWrapper.platformDialog.isCancelable = metadata.cancelable
                BottomSheetDialog(
                    platformDialogWrapper.platformDialog.requireContext(),
                    themeResId
                ).apply {
                    setContentView(createDialogView(platformDialogWrapper, metadata))
                    if (metadata.showDuration > 0 || metadata.onShowListener != null) {
                        setOnShowListener {
                            try {
                                if (metadata.showDuration > 0) {
                                    currentCoroutineScope?.launch {
                                        try {
                                            delay(metadata.showDuration)
                                            platformDialogWrapper.dismiss()
                                        } catch (e: Exception) {
                                            ScaffoldLogger.error(
                                                "[Dialog] Cannot dismiss this dialog [$platformDialogWrapper]",
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
            dismissDialogCallback = { platformDialogWrapper ->
                try {
                    metadata.onDismissListener?.invoke()
                } finally {
                    inbuiltDismissListener(platformDialogWrapper)
                }
            }
        )
    }

    final override fun updateDialog(dialog: BottomSheetPlatformDialogWrapper, metadata: M) {
        updateDialogView(dialog, metadata)
    }

    protected abstract fun createDialogView(
        platformDialogWrapper: BottomSheetPlatformDialogWrapper,
        metadata: M
    ): View

    protected abstract fun updateDialogView(
        platformDialogWrapper: BottomSheetPlatformDialogWrapper,
        metadata: M
    )

}