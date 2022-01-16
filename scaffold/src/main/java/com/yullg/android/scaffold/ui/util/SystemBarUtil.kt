package com.yullg.android.scaffold.ui.util

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 提供系统栏相关的控制功能
 */
object SystemBarUtil {

    /**
     * 设置decor view是否应该适配insets
     *
     * @see WindowCompat.setDecorFitsSystemWindows
     */
    fun setDecorFitsSystemWindows(window: Window, decorFitsSystemWindows: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, decorFitsSystemWindows)
    }

    /**
     * 获取状态栏颜色
     *
     * @see Window.getStatusBarColor
     */
    @ColorInt
    fun getStatusBarColor(window: Window): Int {
        return window.statusBarColor
    }

    /**
     * 设置状态栏颜色
     *
     * 为了保证设置生效，在设置之前会先清除[WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS]标记，
     * 然后添加[WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS]标记。
     *
     * @see Window.setStatusBarColor
     */
    fun setStatusBarColor(window: Window, @ColorInt color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    /**
     * 返回当状态栏的颜色完全透明时，系统是否应该确保状态栏有足够的对比度。
     *
     * 当状态栏颜色有非零alpha值时，此返回值无效。
     *
     * @see Window.isStatusBarContrastEnforced
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isStatusBarContrastEnforced(window: Window): Boolean {
        return window.isStatusBarContrastEnforced
    }

    /**
     * 设置当状态栏的颜色完全透明时，系统是否应该确保状态栏有足够的对比度。
     *
     * 如果设置这个值，系统将判断是否需要scrim来确保状态栏与app的内容有足够的对比度，并相应地设置一个适当的有效的背景色。
     *
     * 当状态栏颜色具有非零alpha值时，此设置将不起作用。
     *
     * @see Window.setStatusBarContrastEnforced
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun setStatusBarContrastEnforced(window: Window, ensureContrast: Boolean) {
        window.isStatusBarContrastEnforced = ensureContrast
    }

    /**
     * 获取导航栏颜色
     *
     * @see Window.getNavigationBarColor
     */
    @ColorInt
    fun getNavigationBarColor(window: Window): Int {
        return window.navigationBarColor
    }

    /**
     * 设置导航栏颜色
     *
     * 为了保证设置生效，在设置之前会先清除[WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION]标记，
     * 然后添加[WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS]标记。
     *
     * @see Window.setNavigationBarColor
     */
    fun setNavigationBarColor(window: Window, @ColorInt color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
    }

    /**
     * 返回当导航栏的颜色完全透明时，系统是否应该确保导航栏有足够的对比度。
     *
     * 当导航栏颜色有非零alpha值时，此返回值无效。
     *
     * @see Window.isNavigationBarContrastEnforced
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isNavigationBarContrastEnforced(window: Window): Boolean {
        return window.isNavigationBarContrastEnforced
    }

    /**
     * 设置当导航栏的颜色完全透明时，系统是否应该确保导航栏有足够的对比度。
     *
     * 如果设置这个值，系统将判断是否需要scrim来确保导航栏与app的内容有足够的对比度，并相应地设置一个适当的有效的背景色。
     *
     * 当导航栏颜色具有非零alpha值时，此设置将不起作用。
     *
     * @see Window.setNavigationBarContrastEnforced
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun setNavigationBarContrastEnforced(window: Window, enforceContrast: Boolean) {
        window.isNavigationBarContrastEnforced = enforceContrast
    }

    /**
     * 获取导航栏分隔线颜色
     *
     * @see Window.getNavigationBarDividerColor
     */
    @RequiresApi(Build.VERSION_CODES.P)
    @ColorInt
    fun getNavigationBarDividerColor(window: Window): Int {
        return window.navigationBarDividerColor
    }

    /**
     * 设置导航栏分隔线颜色
     *
     * 为了保证设置生效，在设置之前会先清除[WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION]标记，
     * 然后添加[WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS]标记。
     *
     * @see Window.setNavigationBarDividerColor
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun setNavigationBarDividerColor(window: Window, @ColorInt color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarDividerColor = color
    }

    /**
     * 检查状态栏是否为浅色
     *
     * @see WindowInsetsControllerCompat.isAppearanceLightStatusBars
     */
    fun isAppearanceLightStatusBars(window: Window, view: View): Boolean =
        WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars ?: false

    /**
     * 如果[isLight]为true，则将状态栏改为浅色，以便可以清楚地阅读状态栏上的内容。
     * 如果[isLight]为false，则恢复到默认外观。
     *
     * @see WindowInsetsControllerCompat.setAppearanceLightStatusBars
     */
    fun setAppearanceLightStatusBars(window: Window, view: View, isLight: Boolean) {
        WindowCompat.getInsetsController(window, view)?.apply {
            isAppearanceLightStatusBars = isLight
        }
    }

    /**
     * 检查导航栏是否为浅色
     *
     * @see WindowInsetsControllerCompat.isAppearanceLightNavigationBars
     */
    fun isAppearanceLightNavigationBars(window: Window, view: View): Boolean =
        WindowCompat.getInsetsController(window, view)?.isAppearanceLightNavigationBars ?: false

    /**
     * 如果[isLight]为true，则将导航栏改为浅色，以便可以清楚地阅读导航栏上的内容。
     * 如果[isLight]为false，则恢复到默认外观。
     *
     * @see WindowInsetsControllerCompat.setAppearanceLightNavigationBars
     */
    fun setAppearanceLightNavigationBars(window: Window, view: View, isLight: Boolean) {
        WindowCompat.getInsetsController(window, view)?.apply {
            isAppearanceLightNavigationBars = isLight
        }
    }

    /**
     * 获取系统栏行为
     *
     * @see WindowInsetsControllerCompat.getSystemBarsBehavior
     */
    @SystemBarBehavior
    fun getSystemBarsBehavior(window: Window, view: View): Int =
        WindowCompat.getInsetsController(window, view)?.systemBarsBehavior
            ?: SystemBarBehavior.BEHAVIOR_SHOW_BARS_BY_TOUCH

    /**
     * 设置系统栏行为
     *
     * @see WindowInsetsControllerCompat.setSystemBarsBehavior
     */
    fun setSystemBarsBehavior(window: Window, view: View, @SystemBarBehavior behavior: Int) {
        WindowCompat.getInsetsController(window, view)?.apply {
            systemBarsBehavior = behavior
        }
    }

    /**
     * 隐藏系统栏
     *
     * @see WindowInsetsControllerCompat.hide
     * @see WindowInsetsCompat.Type.systemBars
     */
    fun hideSystemBars(window: Window, view: View) {
        hide(window, view, WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 隐藏状态栏
     *
     * @see WindowInsetsControllerCompat.hide
     * @see WindowInsetsCompat.Type.statusBars
     */
    fun hideStatusBars(window: Window, view: View) {
        hide(window, view, WindowInsetsCompat.Type.statusBars())
    }

    /**
     * 隐藏导航栏
     *
     * @see WindowInsetsControllerCompat.hide
     * @see WindowInsetsCompat.Type.navigationBars
     */
    fun hideNavigationBars(window: Window, view: View) {
        hide(window, view, WindowInsetsCompat.Type.navigationBars())
    }

    /**
     * 显示系统栏
     *
     * @see WindowInsetsControllerCompat.show
     * @see WindowInsetsCompat.Type.systemBars
     */
    fun showSystemBars(window: Window, view: View) {
        show(window, view, WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 显示状态栏
     *
     * @see WindowInsetsControllerCompat.show
     * @see WindowInsetsCompat.Type.statusBars
     */
    fun showStatusBars(window: Window, view: View) {
        show(window, view, WindowInsetsCompat.Type.statusBars())
    }

    /**
     * 显示导航栏
     *
     * @see WindowInsetsControllerCompat.show
     * @see WindowInsetsCompat.Type.navigationBars
     */
    fun showNavigationBars(window: Window, view: View) {
        show(window, view, WindowInsetsCompat.Type.navigationBars())
    }

    private fun hide(window: Window, view: View, @WindowInsetsCompat.Type.InsetsType types: Int) {
        WindowCompat.getInsetsController(window, view)?.apply {
            hide(types)
        }
    }

    private fun show(window: Window, view: View, @WindowInsetsCompat.Type.InsetsType types: Int) {
        WindowCompat.getInsetsController(window, view)?.apply {
            show(types)
        }
    }

}