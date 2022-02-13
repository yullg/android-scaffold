package com.yullg.android.scaffold.support.permission

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.yullg.android.scaffold.internal.ScaffoldLogger
import java.lang.ref.WeakReference

/**
 * 封装权限授权流程
 */
abstract class PermissionRequester<T : ActivityResultCaller> internal constructor(host: T) {

    private val hostRef = WeakReference(host)
    private val singleLauncher: ActivityResultLauncher<String>
    private val multipleLauncher: ActivityResultLauncher<Array<String>>

    private var singleRequestTask: SingleRequestTask? = null
    private var multipleRequestTask: MultipleRequestTask? = null

    init {
        this.singleLauncher = host.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            singleRequestTask?.let { task ->
                try {
                    task.callback(
                        SinglePermissionResult(
                            name = task.permission,
                            granted = it,
                            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale(
                                requireHost(),
                                task.permission
                            )
                        )
                    )
                } catch (error: Throwable) {
                    ScaffoldLogger.error(
                        "[Permission] Failed to consume SingleRequestTask",
                        error
                    )
                } finally {
                    singleRequestTask = null
                }
            }
        }
        this.multipleLauncher = host.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            multipleRequestTask?.let { task ->
                try {
                    val grantedArr = BooleanArray(task.permissions.size)
                    val ssrprArr = BooleanArray(task.permissions.size)
                    task.permissions.forEachIndexed { index, name ->
                        grantedArr[index] = it[name]
                            ?: throw IllegalStateException("The requested permission does not appear in the response: $name")
                        ssrprArr[index] = shouldShowRequestPermissionRationale(
                            requireHost(),
                            name
                        )
                    }
                    task.callback(
                        MultiplePermissionResult(
                            nameArr = task.permissions,
                            grantedArr = grantedArr,
                            shouldShowRequestPermissionRationaleArr = ssrprArr
                        )
                    )
                } catch (error: Throwable) {
                    ScaffoldLogger.error(
                        "[Permission] Failed to consume MultipleRequestTask",
                        error
                    )
                } finally {
                    multipleRequestTask = null
                }
            }
        }
    }

    /**
     * 单权限授权请求，返回请求是否被接受。
     *
     * 如果没有正在处理中的单权限授权请求，那么将接受并开始处理新请求（该方法返回true），否则将忽略新提交的请求（该方法返回false）。
     */
    fun request(permission: String, callback: (SinglePermissionResult) -> Unit): Boolean =
        synchronized(this) {
            if (this.singleRequestTask == null) {
                this.singleRequestTask = SingleRequestTask(permission, callback)
                this.singleLauncher.launch(permission)
                return true
            }
            return false
        }

    /**
     * 多权限授权请求，返回请求是否被接受。
     *
     * 如果没有正在处理中的多权限授权请求，那么将接受并开始处理新请求（该方法返回true），否则将忽略新提交的请求（该方法返回false）。
     */
    fun request(permissions: Array<String>, callback: (MultiplePermissionResult) -> Unit): Boolean =
        synchronized(this) {
            if (this.multipleRequestTask == null) {
                this.multipleRequestTask = MultipleRequestTask(permissions.copyOf(), callback)
                this.multipleLauncher.launch(permissions)
                return true
            }
            return false
        }

    private fun requireHost(): T {
        return hostRef.get() ?: throw IllegalStateException("Host has been reclaimed")
    }

    protected abstract fun shouldShowRequestPermissionRationale(
        host: T,
        permission: String
    ): Boolean

}

private class SingleRequestTask(
    val permission: String,
    val callback: (SinglePermissionResult) -> Unit
)

private class MultipleRequestTask(
    val permissions: Array<String>,
    val callback: (MultiplePermissionResult) -> Unit
)

class ActivityPermissionRequester(activity: ComponentActivity) :
    PermissionRequester<ComponentActivity>(activity) {

    override fun shouldShowRequestPermissionRationale(
        host: ComponentActivity,
        permission: String
    ): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(host, permission)

}

class FragmentPermissionRequester(fragment: Fragment) :
    PermissionRequester<Fragment>(fragment) {

    override fun shouldShowRequestPermissionRationale(
        host: Fragment,
        permission: String
    ): Boolean = host.shouldShowRequestPermissionRationale(permission)

}