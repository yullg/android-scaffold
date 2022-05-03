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
 * 定义与Dialog的交互接口
 *
 * 此接口意在使用提供的[DialogMetadata]创建Dialog，封装Dialog相关的操作，
 * 使调用者在与Dialog交互时仅需要关心[DialogMetadata]的创建。
 */
interface DialogHandler<M : DialogMetadata> {

    fun show(metadata: M)

    fun dismiss()

    fun isShowing(): Boolean

}

/**
 * [DialogHandler]的抽象实现，使用提供的[DialogMetadata]创建[DialogShell]，将Dialog相关操作委托给[DialogShell]。
 */
abstract class AbstractDialogHandler<M : DialogMetadata, DS : DialogShell>(
    fragmentManager: FragmentManager
) : DialogHandler<M> {

    // 弱引用持有FragmentManager实例，用于启动DialogShell。
    private val fragmentManagerRef = WeakReference(fragmentManager)

    // 持有已创建的DialogShell及其关联的CoroutineScope，CoroutineScope将在DialogShell关闭时取消。
    // 每次创建DialogShell时也会创建一个CoroutineScope（MainScope实例）与之关联， 用在DialogShell生命周期内执行协程任务。
    private var dscs: Pair<DS, CoroutineScope>? = null

    protected val dialogShellCoroutineScope: CoroutineScope?
        get() = dscs?.second

    /**
     * 根据提供的[metadata]创建并打开[DialogShell]，如果有已经打开的[DialogShell]，那么就使用[metadata]更新它。
     */
    final override fun show(metadata: M) {
        synchronized(this) {
            dscs?.apply {
                updateDialogShell(first, metadata)
                return
            }
            val dialogShell = createDialogShell(metadata) { dismissedDialogShell ->
                afterDismiss(dismissedDialogShell)
            }
            dscs = Pair(dialogShell, MainScope())
            dialogShell.show(fragmentManagerRef.get()!!)
        }
    }

    /**
     * 关闭已打开的[DialogShell]
     */
    final override fun dismiss() {
        synchronized(this) {
            dscs?.first?.let {
                try {
                    it.dismiss()
                } finally {
                    afterDismiss(it)
                }
            }
        }
    }

    /**
     * 查询是否有已打开的[DialogShell]
     */
    final override fun isShowing(): Boolean {
        return dscs?.first?.isShowing() ?: false
    }

    /**
     * 释放[dismissedDialogShell]相关的所有资源
     */
    private fun afterDismiss(dismissedDialogShell: DS) {
        synchronized(this) {
            /*
             * 当调用dismiss()关闭一个已打开的DialogShell后又立即调用show()打开一个新的DialogShell，
             * 上一个关闭的DialogShell可能会在新的DialogShell打开后重复触发afterDismiss()，因为在dismiss()
             * 方法体内有对afterDismiss()的直接调用，此时通过DialogShell监听器触发的afterDismiss()是多余的，
             * 并且还可能释放本不属于它的资源，所以这里需要对比引用来判断资源所有者。
             *
             * 另一个不可取的做法是仅通过DialogShell监听器触发afterDismiss()，这会导致在调用dismiss()后，
             * DialogShell触发afterDismiss()之前的这段时间内调用show()方法在一个已关闭的DialogShell上执行更新操作的异常行为。
             */
            (dscs?.first === dismissedDialogShell).let {
                ScaffoldLogger.debug("[Dialog] DialogShell dismiss [${dscs?.first.hashCode()}, ${dismissedDialogShell.hashCode()}, $it]")
                if (it) {
                    try {
                        try {
                            dscs?.second?.cancel()
                        } finally {
                            dscs = null
                        }
                    } finally {
                        onDismiss(dismissedDialogShell)
                    }
                }
            }
        }
    }

    /**
     * 在DialogShell关闭后调用，当此方法调用时所有与DialogShell相关的资源都已释放。
     */
    protected open fun onDismiss(dismissedDialogShell: DS) {}

    /**
     * 根据给定的[metadata]创建[DialogShell]
     *
     * 注意：必须将提供的[inbuiltDismissListener]事件绑定到创建的[DialogShell]上，这样在[DialogShell]关闭时为其分配的资源才能被释放。
     */
    protected abstract fun createDialogShell(
        metadata: M,
        inbuiltDismissListener: (DS) -> Unit
    ): DS

    /**
     * 根据给定的[metadata]更新[DialogShell]
     */
    protected abstract fun updateDialogShell(dialogShell: DS, metadata: M)

}