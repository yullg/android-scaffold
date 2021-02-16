package com.yullg.android.scaffold.ui.canvas

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.core.graphics.withScale

open class OvalCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : CanvasView(context, attrs, defStyleAttr, defStyleRes) {

    override fun onDraw(canvas: Canvas) {
        canvas.withScale(
            if (containsReverseModeFlag(ReverseMode.HORIZONTAL)) -1f else 1f,
            if (containsReverseModeFlag(ReverseMode.VERTICAL)) -1f else 1f,
            width / 2f,
            height / 2f
        ) {
            canvas.drawOval(
                paddingLeft + _cvPaint.strokeWidth / 2,
                paddingTop + _cvPaint.strokeWidth / 2,
                width - paddingRight - _cvPaint.strokeWidth / 2,
                height - paddingBottom - _cvPaint.strokeWidth / 2,
                _cvPaint
            )
        }
    }

}