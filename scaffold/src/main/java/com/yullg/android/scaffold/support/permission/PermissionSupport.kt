package com.yullg.android.scaffold.support.permission

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yullg.android.scaffold.app.Scaffold

/**
 * 提供系统权限相关功能的支持
 */
object PermissionSupport {

    /**
     * 检查是否授予权限
     *
     * @see ContextCompat.checkSelfPermission
     */
    fun checkSelfPermission(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(Scaffold.context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /**
     * 创建并注册可用于执行权限授权操作的[PermissionRequester]
     *
     * 注意：必须在[activity]初始化过程中调用，通常是字段初始化。
     *
     * @see ComponentActivity.registerForActivityResult
     * @see ActivityResultContracts.RequestPermission
     * @see ActivityResultContracts.RequestMultiplePermissions
     */
    fun registerForRequestPermission(activity: ComponentActivity) =
        ActivityPermissionRequester(activity)

    /**
     * 创建并注册可用于执行权限授权操作的[PermissionRequester]
     *
     * 注意：必须在[fragment]初始化过程中调用，通常是字段初始化。
     *
     * @see ComponentActivity.registerForActivityResult
     * @see ActivityResultContracts.RequestPermission
     * @see ActivityResultContracts.RequestMultiplePermissions
     */
    fun registerForRequestPermission(fragment: Fragment) =
        FragmentPermissionRequester(fragment)

}