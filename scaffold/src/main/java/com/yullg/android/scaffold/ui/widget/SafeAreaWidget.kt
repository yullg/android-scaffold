package com.yullg.android.scaffold.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.yullg.android.scaffold.R

open class SafeAreaWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    var saConsumeInsets: Boolean
    var saIgnoringVisibility: Boolean

    @SafeAreaApplyMode
    var saApplyMode: Int

    @WindowInsetsCompat.Type.InsetsType
    var saInsetsTypeMask: Int

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SafeAreaWidget,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                saConsumeInsets = getBoolean(
                    R.styleable.SafeAreaWidget_yg_saConsumeInsets,
                    true
                )
                saIgnoringVisibility = getBoolean(
                    R.styleable.SafeAreaWidget_yg_saIgnoringVisibility,
                    false
                )
                saApplyMode = getInteger(
                    R.styleable.SafeAreaWidget_yg_saApplyMode,
                    SafeAreaApplyMode.MARGIN
                )
                saInsetsTypeMask = getInteger(
                    R.styleable.SafeAreaWidget_yg_saInsetsType,
                    FLAG_TYPE_SYSTEM_BARS
                ).toInsetsTypeMask()
            } finally {
                recycle()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsetsCompat ->
            doOnApplyWindowInsets(windowInsetsCompat)
        }
    }

    private fun doOnApplyWindowInsets(windowInsetsCompat: WindowInsetsCompat): WindowInsetsCompat {
        val insets = if (saIgnoringVisibility) {
            windowInsetsCompat.getInsetsIgnoringVisibility(saInsetsTypeMask)
        } else {
            windowInsetsCompat.getInsets(saInsetsTypeMask)
        }
        when (saApplyMode) {
            SafeAreaApplyMode.MARGIN -> updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            SafeAreaApplyMode.PADDING -> updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom
            )
        }
        return if (saConsumeInsets) {
            WindowInsetsCompat.CONSUMED
        } else {
            windowInsetsCompat
        }
    }

}

private const val FLAG_TYPE_CAPTION_BAR = 1
private const val FLAG_TYPE_DISPLAY_CUTOUT = 2
private const val FLAG_TYPE_IME = 4
private const val FLAG_TYPE_MANDATORY_SYSTEM_GESTURES = 8
private const val FLAG_TYPE_NAVIGATION_BARS = 16
private const val FLAG_TYPE_STATUS_BARS = 32
private const val FLAG_TYPE_SYSTEM_BARS = 64
private const val FLAG_TYPE_SYSTEM_GESTURES = 128
private const val FLAG_TYPE_TAPPABLE_ELEMENT = 256

private fun Int.toInsetsTypeMask() = let {
    var result = 0
    if (it or FLAG_TYPE_CAPTION_BAR == it) {
        result = result or WindowInsetsCompat.Type.captionBar()
    }
    if (it or FLAG_TYPE_DISPLAY_CUTOUT == it) {
        result = result or WindowInsetsCompat.Type.displayCutout()
    }
    if (it or FLAG_TYPE_IME == it) {
        result = result or WindowInsetsCompat.Type.ime()
    }
    if (it or FLAG_TYPE_MANDATORY_SYSTEM_GESTURES == it) {
        result = result or WindowInsetsCompat.Type.mandatorySystemGestures()
    }
    if (it or FLAG_TYPE_NAVIGATION_BARS == it) {
        result = result or WindowInsetsCompat.Type.navigationBars()
    }
    if (it or FLAG_TYPE_STATUS_BARS == it) {
        result = result or WindowInsetsCompat.Type.statusBars()
    }
    if (it or FLAG_TYPE_SYSTEM_BARS == it) {
        result = result or WindowInsetsCompat.Type.systemBars()
    }
    if (it or FLAG_TYPE_SYSTEM_GESTURES == it) {
        result = result or WindowInsetsCompat.Type.systemGestures()
    }
    if (it or FLAG_TYPE_TAPPABLE_ELEMENT == it) {
        result = result or WindowInsetsCompat.Type.tappableElement()
    }
    result
}