package com.yullg.android.scaffold.support.permission

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.yullg.android.scaffold.core.ResultActivityLauncher
import java.lang.ref.WeakReference

/**
 * 封装权限授权流程
 */
abstract class PermissionRequester<T : ActivityResultCaller> internal constructor(caller: T) {

    private val callerRef = WeakReference(caller)
    private val singleLauncher =
        ResultActivityLauncher(caller, ActivityResultContracts.RequestPermission())
    private val multipleLauncher =
        ResultActivityLauncher(caller, ActivityResultContracts.RequestMultiplePermissions())

    /**
     * 单权限授权请求
     */
    fun request(permission: String, callback: PermissionRequestCallback<SinglePermissionResult>) {
        singleLauncher.launch(permission) {
            try {
                val result = SinglePermissionResult(
                    name = permission,
                    granted = it,
                    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale(
                        requireCaller(),
                        permission
                    )
                )
                callback.onResult(result)
            } catch (e: Throwable) {
                callback.onError(e)
            }
        }
    }

    /**
     * 多权限授权请求
     */
    fun request(
        permissions: Array<String>,
        callback: PermissionRequestCallback<MultiplePermissionResult>
    ) {
        multipleLauncher.launch(permissions) {
            try {
                val grantedArr = BooleanArray(permissions.size)
                val ssrprArr = BooleanArray(permissions.size)
                permissions.forEachIndexed { index, name ->
                    grantedArr[index] = it[name]
                        ?: throw IllegalStateException("The requested permission does not appear in the response: $name")
                    ssrprArr[index] = shouldShowRequestPermissionRationale(requireCaller(), name)
                }
                val result = MultiplePermissionResult(
                    nameArr = permissions,
                    grantedArr = grantedArr,
                    shouldShowRequestPermissionRationaleArr = ssrprArr
                )
                callback.onResult(result)
            } catch (e: Throwable) {
                callback.onError(e)
            }
        }
    }

    private fun requireCaller(): T {
        return callerRef.get() ?: throw IllegalStateException("Caller has been reclaimed")
    }

    protected abstract fun shouldShowRequestPermissionRationale(
        caller: T,
        permission: String
    ): Boolean

}

interface PermissionRequestCallback<T> {

    fun onResult(result: T)

    fun onError(error: Throwable) {
        // nothing
    }

}

class ActivityPermissionRequester(activity: ComponentActivity) :
    PermissionRequester<ComponentActivity>(activity) {

    override fun shouldShowRequestPermissionRationale(
        caller: ComponentActivity,
        permission: String
    ): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(caller, permission)

}

class FragmentPermissionRequester(fragment: Fragment) :
    PermissionRequester<Fragment>(fragment) {

    override fun shouldShowRequestPermissionRationale(
        caller: Fragment,
        permission: String
    ): Boolean = caller.shouldShowRequestPermissionRationale(permission)

}