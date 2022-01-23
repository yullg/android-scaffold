package com.yullg.android.scaffold.ui.canvas

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.graphics.withScale
import com.yullg.android.scaffold.R

/**
 * 一个执行单次[Canvas.drawRoundRect]来绘制自身的View
 */
open class RectCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : CanvasView(context, attrs, defStyleAttr, defStyleRes) {

    protected var _rectXRadius: Float = 0f
    protected var _rectYRadius: Float = 0f

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RectCanvasView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                if (hasValue(R.styleable.RectCanvasView_yg_rectXRadius)) {
                    _rectXRadius = getDimensionOrThrow(R.styleable.RectCanvasView_yg_rectXRadius)
                }
                if (hasValue(R.styleable.RectCanvasView_yg_rectYRadius)) {
                    _rectYRadius = getDimensionOrThrow(R.styleable.RectCanvasView_yg_rectYRadius)
                }
            } finally {
                recycle()
            }
        }
    }

    var rectXRadius: Float
        get() = _rectXRadius
        set(value) {
            _rectXRadius = value
            invalidate()
        }

    var rectYRadius: Float
        get() = _rectYRadius
        set(value) {
            _rectYRadius = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        canvas.withScale(
            if (containsReverseModeFlag(ReverseMode.HORIZONTAL)) -1f else 1f,
            if (containsReverseModeFlag(ReverseMode.VERTICAL)) -1f else 1f,
            width / 2f,
            height / 2f
        ) {
            canvas.drawRoundRect(
                paddingLeft + _cvPaint.strokeWidth / 2,
                paddingTop + _cvPaint.strokeWidth / 2,
                width - paddingRight - _cvPaint.strokeWidth / 2,
                height - paddingBottom - _cvPaint.strokeWidth / 2,
                _rectXRadius,
                _rectYRadius,
                _cvPaint
            )
        }
    }

}