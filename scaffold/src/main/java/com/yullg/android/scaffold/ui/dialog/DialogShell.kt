package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 定义与平台层Dialog的交互接口
 *
 * 此接口将平台层Dialog的构造部分分离出去，仅控制Dialog的打开和关闭等，以便框架设计更灵活易用的API。
 * 平台层Dialog API倾向于在编译期为每个用例的Dialog分别定义[DialogFragment]，这种做法限制了它的灵活性
 * （例如在运行时配置Dialog的类型、内容或绑定事件等都是比较麻烦的）。考虑到在大多数情况下当Dialog随着
 * Activity栈一起被系统回收后，系统之后能不能恢复Dialog并不太重要，所以此框架决定放弃这个功能来换取更灵活易用的API。
 *
 * 注意：在应用中打开Dialog后将应用置于后台，如果系统销毁了Activity栈，那么再将应用置于前台时Dialog是不能恢复的。
 */
interface DialogShell {

    fun show(manager: FragmentManager)

    fun dismiss()

    fun isShowing(): Boolean?

}

class NormalDialogShell(
    val createDialogCallback: ((NormalDialogShell) -> Dialog)? = null,
    val dismissDialogCallback: ((NormalDialogShell) -> Unit)? = null
) : DialogFragment(), DialogShell {

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

    override fun show(manager: FragmentManager) = show(manager, null)

    override fun dismiss() = dismissAllowingStateLoss()

    override fun isShowing(): Boolean? = dialog?.isShowing

}

class BottomSheetDialogShell(
    val createDialogCallback: ((BottomSheetDialogShell) -> Dialog)? = null,
    val dismissDialogCallback: ((BottomSheetDialogShell) -> Unit)? = null
) : BottomSheetDialogFragment(), DialogShell {

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

    override fun show(manager: FragmentManager) = show(manager, null)

    override fun dismiss() = dismissAllowingStateLoss()

    override fun isShowing(): Boolean? = dialog?.isShowing

}