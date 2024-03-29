package com.yullg.android.scaffold.ui.dialog

import androidx.annotation.CallSuper

/**
 * Dialog交互接口的抽象基类
 *
 * 本质上，所有BaseDialog的实例仅负责创建[DialogMetadata]，然后将创建的[DialogMetadata]交给[DialogHandler]处理。
 */
abstract class BaseDialog<M : DialogMetadata, S : BaseDialog<M, S>>(
    protected val handler: DialogHandler<M>
) {

    var cancelable: Boolean? = null
    var showDuration: Long? = null
    var onShowListener: ((S) -> Unit)? = null
    var onDismissListener: ((S) -> Unit)? = null

    open fun show() = handler.show(buildMetadata())

    open fun dismiss() = handler.dismiss()

    open fun isShowing() = handler.isShowing()

    protected abstract fun buildMetadata(): M

    @CallSuper
    open fun resetMetadata() {
        cancelable = null
        showDuration = null
        onShowListener = null
        onDismissListener = null
    }

    protected fun convertOnShowOrDismissListener(self: S, listener: ((S) -> Unit)?): (() -> Unit)? {
        return listener?.let { lr ->
            { lr(self) }
        }
    }

}