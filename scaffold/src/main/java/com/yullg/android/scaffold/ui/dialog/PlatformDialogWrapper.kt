package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 定义平台层Dialog API的包装器接口，主要用于转换使用方式和解耦具体实现。
 *
 * 平台层Dialog API倾向于在编译期为每个用例的Dialog分别定义[DialogFragment]，这种做法限制了它的灵活性
 * （在运行期配置Dialog的类型、内容或绑定事件等都是比较麻烦的）。此接口将平台层Dialog的构造部分从原本的编译期
 * 延迟到运行期提供，以便框架设计更灵活易用的API。
 *
 * 注意：采用此方案后，系统重建Activity时无法恢复Dialog，例如在屏幕旋转后Dialog将丢失。
 */
interface PlatformDialogWrapper<T> {

    val platformDialog: T

    fun show(manager: FragmentManager)

    fun dismiss()

    fun isShowing(): Boolean

}

class NormalPlatformDialogWrapper(
    val createDialogCallback: ((NormalPlatformDialogWrapper) -> Dialog)? = null,
    val dismissDialogCallback: ((NormalPlatformDialogWrapper) -> Unit)? = null
) : PlatformDialogWrapper<NormalPlatformDialogFragment> {

    override val platformDialog = NormalPlatformDialogFragment(
        createDialogCallback = createDialogCallback?.let { c -> { c(this) } },
        dismissDialogCallback = dismissDialogCallback?.let { c -> { c(this) } }
    )

    override fun show(manager: FragmentManager) = platformDialog.show(manager, null)

    override fun dismiss() = platformDialog.dismissAllowingStateLoss()

    override fun isShowing(): Boolean = platformDialog.dialog?.isShowing ?: false

}

class BottomSheetPlatformDialogWrapper(
    val createDialogCallback: ((BottomSheetPlatformDialogWrapper) -> Dialog)? = null,
    val dismissDialogCallback: ((BottomSheetPlatformDialogWrapper) -> Unit)? = null
) : PlatformDialogWrapper<BottomSheetPlatformDialogFragment> {

    override val platformDialog = BottomSheetPlatformDialogFragment(
        createDialogCallback = createDialogCallback?.let { c -> { c(this) } },
        dismissDialogCallback = dismissDialogCallback?.let { c -> { c(this) } }
    )

    override fun show(manager: FragmentManager) = platformDialog.show(manager, null)

    override fun dismiss() = platformDialog.dismissAllowingStateLoss()

    override fun isShowing(): Boolean = platformDialog.dialog?.isShowing ?: false

}

class NormalPlatformDialogFragment(
    private val createDialogCallback: ((NormalPlatformDialogFragment) -> Dialog)? = null,
    private val dismissDialogCallback: ((NormalPlatformDialogFragment) -> Unit)? = null
) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showsDialog = (createDialogCallback != null && dismissDialogCallback != null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialogCallback?.invoke(this) ?: super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        try {
            dismissDialogCallback?.invoke(this)
        } finally {
            super.onDismiss(dialog)
        }
    }

}

class BottomSheetPlatformDialogFragment(
    private val createDialogCallback: ((BottomSheetPlatformDialogFragment) -> Dialog)? = null,
    private val dismissDialogCallback: ((BottomSheetPlatformDialogFragment) -> Unit)? = null
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showsDialog = (createDialogCallback != null && dismissDialogCallback != null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialogCallback?.invoke(this) ?: super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        try {
            dismissDialogCallback?.invoke(this)
        } finally {
            super.onDismiss(dialog)
        }
    }

}