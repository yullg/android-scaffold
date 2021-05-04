package com.yullg.android.scaffold.ui.util

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

object SystemBarUtil {

    fun setDecorFitsSystemWindows(window: Window, decorFitsSystemWindows: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, decorFitsSystemWindows)
    }

    @ColorInt
    fun getStatusBarColor(window: Window): Int {
        return window.statusBarColor
    }

    fun setStatusBarColor(window: Window, @ColorInt color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    @ColorInt
    fun getNavigationBarColor(window: Window): Int {
        return window.navigationBarColor
    }

    fun setNavigationBarColor(window: Window, @ColorInt color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @ColorInt
    fun getNavigationBarDividerColor(window: Window): Int {
        return window.navigationBarDividerColor
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setNavigationBarDividerColor(window: Window, @ColorInt color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarDividerColor = color
    }

    fun isAppearanceLightStatusBars(window: Window, view: View): Boolean =
        WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars ?: false

    fun setAppearanceLightStatusBars(window: Window, view: View, isLight: Boolean) {
        WindowCompat.getInsetsController(window, view)?.apply {
            isAppearanceLightStatusBars = isLight
        }
    }

    fun isAppearanceLightNavigationBars(window: Window, view: View): Boolean =
        WindowCompat.getInsetsController(window, view)?.isAppearanceLightNavigationBars ?: false

    fun setAppearanceLightNavigationBars(window: Window, view: View, isLight: Boolean) {
        WindowCompat.getInsetsController(window, view)?.apply {
            isAppearanceLightNavigationBars = isLight
        }
    }

    @SystemBarBehavior
    fun getSystemBarsBehavior(window: Window, view: View): Int =
        WindowCompat.getInsetsController(window, view)?.systemBarsBehavior
            ?: SystemBarBehavior.BEHAVIOR_SHOW_BARS_BY_TOUCH

    fun setSystemBarsBehavior(window: Window, view: View, @SystemBarBehavior behavior: Int) {
        WindowCompat.getInsetsController(window, view)?.apply {
            systemBarsBehavior = behavior
        }
    }

    fun hideSystemBars(window: Window, view: View) {
        hide(window, view, WindowInsetsCompat.Type.systemBars())
    }

    fun hideStatusBars(window: Window, view: View) {
        hide(window, view, WindowInsetsCompat.Type.statusBars())
    }

    fun hideNavigationBars(window: Window, view: View) {
        hide(window, view, WindowInsetsCompat.Type.navigationBars())
    }

    fun showSystemBars(window: Window, view: View) {
        show(window, view, WindowInsetsCompat.Type.systemBars())
    }

    fun showStatusBars(window: Window, view: View) {
        show(window, view, WindowInsetsCompat.Type.statusBars())
    }

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