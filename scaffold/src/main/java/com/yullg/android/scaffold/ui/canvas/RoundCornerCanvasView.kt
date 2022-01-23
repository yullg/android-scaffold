package com.yullg.android.scaffold.ui.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.core.graphics.withScale

/**
 * 圆角View
 */
open class RoundCornerCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : CanvasView(context, attrs, defStyleAttr, defStyleRes) {

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        canvas.withScale(
            if (containsReverseModeFlag(ReverseMode.HORIZONTAL)) -1f else 1f,
            if (containsReverseModeFlag(ReverseMode.VERTICAL)) -1f else 1f,
            width / 2f,
            height / 2f
        ) {
            path.reset()
            path.moveTo(paddingLeft.toFloat(), paddingTop.toFloat())
            path.lineTo(paddingLeft.toFloat(), (height - paddingBottom).toFloat())
            path.cubicTo(
                paddingLeft.toFloat(),
                ((height - paddingTop - paddingBottom) / 2 + paddingTop).toFloat(),
                ((width - paddingLeft - paddingRight) / 2 + paddingLeft).toFloat(),
                paddingTop.toFloat(),
                (width - paddingRight).toFloat(),
                paddingTop.toFloat()
            )
            path.close()
            canvas.drawPath(path, _cvPaint)
        }
    }

}