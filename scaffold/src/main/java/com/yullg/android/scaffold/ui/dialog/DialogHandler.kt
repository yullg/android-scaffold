package com.yullg.android.scaffold.ui.dialog

import androidx.fragment.app.FragmentManager
import com.yullg.android.scaffold.internal.ScaffoldLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.ref.WeakReference

/**
 * Dialog元数据，持有创建Dialog所需要的所有参数。
 */
interface DialogMetadata

/**
 * Dialog处理器，控制Dialog生命周期和查询其状态。
 */
interface DialogHandler<M : DialogMetadata> {

    fun show(metadata: M)

    fun dismiss()

    fun isShowing(): Boolean

}

/**
 * 一个[DialogHandler]的抽象实现，封装基础操作。
 */
abstract class AbstractDialogHandler<M : DialogMetadata, D : PlatformDialogWrapper<*>>(
    fragmentManager: FragmentManager
) : DialogHandler<M> {

    // 弱引用持有FragmentManager实例，用于显示Dialog。
    private val fragmentManagerRef = WeakReference(fragmentManager)

    // 持有已创建的Dialog及其关联的CoroutineScope，CoroutineScope将在Dialog销毁时取消。
    // 每次创建Dialog时也会创建一个CoroutineScope（MainScope的实例）与之关联， 用在Dialog生命周期内执行协程任务。
    private var dialogAndScopePair: Pair<D, CoroutineScope>? = null

    protected val currentCoroutineScope: CoroutineScope?
        get() = dialogAndScopePair?.second

    /**
     * 根据提供的[metadata]创建并显示Dialog，如果有已经创建的Dialog，那么就使用[metadata]更新它。
     */
    final override fun show(metadata: M) {
        dialogAndScopePair?.apply {
            updateDialog(first, metadata)
            return
        }
        val dialog = createDialog(metadata) { dismissedDialog ->
            afterDismiss(dismissedDialog)
        }
        dialogAndScopePair = Pair(dialog, MainScope())
        dialog.show(fragmentManagerRef.get()!!)
    }

    /**
     * 销毁已创建的Dialog
     */
    final override fun dismiss() {
        dialogAndScopePair?.first?.let {
            try {
                it.dismiss()
            } finally {
                afterDismiss(it)
            }
        }
    }

    /**
     * 判断是否有已显示的Dialog
     */
    final override fun isShowing(): Boolean {
        return dialogAndScopePair?.first?.isShowing() ?: false
    }

    /**
     * 释放Dialog相关的所有资源
     */
    private fun afterDismiss(dismissedDialog: D) {
        /*
         * 当调用dismiss()销毁一个已创建的Dialog后又立即调用show()创建一个新的Dialog，
         * 上一个销毁的Dialog可能会在新的Dialog创建后重复触发afterDismiss()，因为在dismiss()
         * 方法体内有对afterDismiss()的直接调用，此时通过Dialog监听器触发的afterDismiss()是多余的，
         * 并且还可能释放本不属于它的资源，所以这里需要对比引用来判断资源所有者。
         *
         * 另一个不可取的做法是仅通过Dialog监听器触发afterDismiss()，这会导致在调用dismiss()后，
         * Dialog触发afterDismiss()之前的这段时间内调用show()方法在一个已销毁的Dialog上执行更新操作的异常行为。
         */
        (dialogAndScopePair?.first === dismissedDialog).let {
            ScaffoldLogger.debug("[Dialog] Dialog dismiss [${dialogAndScopePair?.first.hashCode()}, ${dismissedDialog.hashCode()}, $it]")
            if (it) {
                try {
                    try {
                        dialogAndScopePair?.second?.cancel()
                    } finally {
                        dialogAndScopePair = null
                    }
                } finally {
                    onDismiss(dismissedDialog)
                }
            }
        }
    }

    /**
     * 子类通过重写此方法来在Dialog销毁后释放资源
     */
    protected open fun onDismiss(dismissedDialog: D) {}

    /**
     * 根据给定的[metadata]创建Dialog
     *
     * 注意：必须将提供的[inbuiltDismissListener]事件绑定到创建的Dialog上，否则将导致资源泄漏。
     */
    protected abstract fun createDialog(
        metadata: M,
        inbuiltDismissListener: (D) -> Unit
    ): D

    /**
     * 根据给定的[metadata]更新Dialog
     */
    protected abstract fun updateDialog(dialog: D, metadata: M)

}