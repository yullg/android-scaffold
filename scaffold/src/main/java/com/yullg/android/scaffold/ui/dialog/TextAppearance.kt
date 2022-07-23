package com.yullg.android.scaffold.ui.dialog

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.widget.TextView

/**
 * 定义文本外观
 */
data class TextAppearance(
    val textSize: Float? = null,
    val textColor: ColorStateList? = null,
    val typeface: Typeface? = null
) {

    fun apply(textView: TextView) {
        textSize?.let { textView.textSize = it }
        textColor?.let { textView.setTextColor(it) }
        typeface?.let { textView.typeface = it }
    }

}