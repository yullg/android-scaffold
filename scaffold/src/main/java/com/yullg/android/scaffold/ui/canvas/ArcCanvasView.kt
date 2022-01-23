package com.yullg.android.scaffold.ui.canvas

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.core.content.res.getBooleanOrThrow
import androidx.core.content.res.getFloatOrThrow
import androidx.core.graphics.withScale
import com.yullg.android.scaffold.R

/**
 * 一个执行单次[Canvas.drawArc]来绘制自身的View
 */
open class ArcCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : CanvasView(context, attrs, defStyleAttr, defStyleRes) {

    protected var _arcStartAngle: Float = 0f
    protected var _arcSweepAngle: Float = 0f
    protected var _arcUseCenter: Boolean = true

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ArcCanvasView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                if (hasValue(R.styleable.ArcCanvasView_yg_arcStartAngle)) {
                    _arcStartAngle = getFloatOrThrow(R.styleable.ArcCanvasView_yg_arcStartAngle)
                }
                if (hasValue(R.styleable.ArcCanvasView_yg_arcSweepAngle)) {
                    _arcSweepAngle = getFloatOrThrow(R.styleable.ArcCanvasView_yg_arcSweepAngle)
                }
                if (hasValue(R.styleable.ArcCanvasView_yg_arcUseCenter)) {
                    _arcUseCenter = getBooleanOrThrow(R.styleable.ArcCanvasView_yg_arcUseCenter)
                }
            } finally {
                recycle()
            }
        }
    }

    var arcStartAngle: Float
        get() = _arcStartAngle
        set(value) {
            _arcStartAngle = value
            invalidate()
        }

    var arcSweepAngle: Float
        get() = _arcSweepAngle
        set(value) {
            _arcSweepAngle = value
            invalidate()
        }

    var arcUseCenter: Boolean
        get() = _arcUseCenter
        set(value) {
            _arcUseCenter = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        if (_arcSweepAngle <= 0) return
        canvas.withScale(
            if (containsReverseModeFlag(ReverseMode.HORIZONTAL)) -1f else 1f,
            if (containsReverseModeFlag(ReverseMode.VERTICAL)) -1f else 1f,
            width / 2f,
            height / 2f
        ) {
            canvas.drawArc(
                paddingLeft + _cvPaint.strokeWidth / 2,
                paddingTop + _cvPaint.strokeWidth / 2,
                width - paddingRight - _cvPaint.strokeWidth / 2,
                height - paddingBottom - _cvPaint.strokeWidth / 2,
                _arcStartAngle,
                _arcSweepAngle,
                _arcUseCenter,
                _cvPaint
            )
        }
    }

}