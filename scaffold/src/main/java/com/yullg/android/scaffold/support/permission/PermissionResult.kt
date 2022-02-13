package com.yullg.android.scaffold.support.permission

/**
 * 单权限授权结果
 */
data class SinglePermissionResult(
    val name: String,
    val granted: Boolean,
    val shouldShowRequestPermissionRationale: Boolean
) {
    /**
     * 是否拒绝且不再响应授权请求
     */
    val deniedForever get() = !granted && !shouldShowRequestPermissionRationale
}

/**
 * 多权限授权结果
 */
data class MultiplePermissionResult(
    val nameArr: Array<String>,
    val grantedArr: BooleanArray,
    val shouldShowRequestPermissionRationaleArr: BooleanArray
) {

    /**
     * 是否所有权限都已经授予
     */
    val allGranted get() = grantedArr.all { it }

    /**
     * 判断此实例中是否包含给定的权限
     */
    fun contains(permission: String): Boolean {
        return nameArr.indexOf(permission) >= 0
    }

    /**
     * 获取权限的`granted`值，如果给定的权限不存在此实例中则返回NULL。
     */
    fun grantedOf(permission: String): Boolean? {
        val index = nameArr.indexOf(permission)
        if (index < 0) return null
        return grantedArr[index]
    }

    /**
     * 获取权限的`shouldShowRequestPermissionRationale`值，如果给定的权限不存在此实例中则返回NULL。
     */
    fun shouldShowRequestPermissionRationaleOf(permission: String): Boolean? {
        val index = nameArr.indexOf(permission)
        if (index < 0) return null
        return shouldShowRequestPermissionRationaleArr[index]
    }

    /**
     * 获取权限的`deniedForever`值，如果给定的权限不存在此实例中则返回NULL。
     */
    fun deniedForeverOf(permission: String): Boolean? {
        val index = nameArr.indexOf(permission)
        if (index < 0) return null
        return !grantedArr[index] && !shouldShowRequestPermissionRationaleArr[index]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiplePermissionResult

        if (!nameArr.contentEquals(other.nameArr)) return false
        if (!grantedArr.contentEquals(other.grantedArr)) return false
        if (!shouldShowRequestPermissionRationaleArr.contentEquals(other.shouldShowRequestPermissionRationaleArr)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameArr.contentHashCode()
        result = 31 * result + grantedArr.contentHashCode()
        result = 31 * result + shouldShowRequestPermissionRationaleArr.contentHashCode()
        return result
    }

}