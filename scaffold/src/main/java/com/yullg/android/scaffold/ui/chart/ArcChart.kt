package com.yullg.android.scaffold.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.*
import androidx.core.graphics.withScale
import com.yullg.android.scaffold.R

open class ArcChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.yg_chartArcStyle,
    defStyleRes: Int = R.style.yg_ChartArcDefaultStyle
) : View(context, attrs, defStyleAttr, defStyleRes) {

    protected val arcUnderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val arcUpperPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected var _arcUnderStartAngle: Float = 0f
    protected var _arcUnderSweepAngle: Float = 360f
    protected var _arcUnderUseCenter: Boolean = true
    protected var _arcUnderReverseMode: ReverseMode = ReverseMode.NONE
    protected var _arcUpperStartAngle: Float = 0f
    protected var _arcUpperSweepAngle: Float = 0f
    protected var _arcUpperUseCenter: Boolean = true
    protected var _arcUpperReverseMode: ReverseMode = ReverseMode.NONE

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ArcChart, defStyleAttr, defStyleRes)
            .apply {
                try {
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderWidth)) {
                        arcUnderPaint.strokeWidth =
                            getDimensionOrThrow(R.styleable.ArcChart_yg_arcUnderWidth)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderStyle)) {
                        arcUnderPaint.style =
                            getIntegerOrThrow(R.styleable.ArcChart_yg_arcUnderStyle)
                                .toPaintStyle()
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderCap)) {
                        arcUnderPaint.strokeCap =
                            getIntegerOrThrow(R.styleable.ArcChart_yg_arcUnderCap)
                                .toPaintCap()
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderColor)) {
                        arcUnderPaint.color =
                            getColorOrThrow(R.styleable.ArcChart_yg_arcUnderColor)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderStartAngle)) {
                        _arcUnderStartAngle =
                            getFloatOrThrow(R.styleable.ArcChart_yg_arcUnderStartAngle)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderSweepAngle)) {
                        _arcUnderSweepAngle =
                            getFloatOrThrow(R.styleable.ArcChart_yg_arcUnderSweepAngle)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderUseCenter)) {
                        _arcUnderUseCenter =
                            getBooleanOrThrow(R.styleable.ArcChart_yg_arcUnderUseCenter)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUnderReverse)) {
                        _arcUnderReverseMode =
                            getIntegerOrThrow(R.styleable.ArcChart_yg_arcUnderReverse)
                                .toReverseMode()
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperWidth)) {
                        arcUpperPaint.strokeWidth =
                            getDimensionOrThrow(R.styleable.ArcChart_yg_arcUpperWidth)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperStyle)) {
                        arcUpperPaint.style =
                            getIntegerOrThrow(R.styleable.ArcChart_yg_arcUpperStyle)
                                .toPaintStyle()
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperCap)) {
                        arcUpperPaint.strokeCap =
                            getIntegerOrThrow(R.styleable.ArcChart_yg_arcUpperCap)
                                .toPaintCap()
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperColor)) {
                        arcUpperPaint.color =
                            getColorOrThrow(R.styleable.ArcChart_yg_arcUpperColor)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperStartAngle)) {
                        _arcUpperStartAngle =
                            getFloatOrThrow(R.styleable.ArcChart_yg_arcUpperStartAngle)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperSweepAngle)) {
                        _arcUpperSweepAngle =
                            getFloatOrThrow(R.styleable.ArcChart_yg_arcUpperSweepAngle)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperUseCenter)) {
                        _arcUpperUseCenter =
                            getBooleanOrThrow(R.styleable.ArcChart_yg_arcUpperUseCenter)
                    }
                    if (hasValue(R.styleable.ArcChart_yg_arcUpperReverse)) {
                        _arcUpperReverseMode =
                            getIntegerOrThrow(R.styleable.ArcChart_yg_arcUpperReverse)
                                .toReverseMode()
                    }
                } finally {
                    recycle()
                }
            }
    }

    var arcUnderWidth: Float
        get() = arcUnderPaint.strokeWidth
        set(value) {
            arcUnderPaint.strokeWidth = value
            invalidate()
        }

    var arcUnderStyle: Paint.Style
        get() = arcUnderPaint.style
        set(value) {
            arcUnderPaint.style = value
            invalidate()
        }

    var arcUnderCap: Paint.Cap
        get() = arcUnderPaint.strokeCap
        set(value) {
            arcUnderPaint.strokeCap = value
            invalidate()
        }

    var arcUnderColor: Int
        get() = arcUnderPaint.color
        set(value) {
            arcUnderPaint.color = value
            invalidate()
        }

    var arcUnderShader: Shader?
        get() = arcUnderPaint.shader
        set(value) {
            arcUnderPaint.shader = value
            invalidate()
        }

    var arcUnderStartAngle: Float
        get() = _arcUnderStartAngle
        set(value) {
            _arcUnderStartAngle = value
            invalidate()
        }

    var arcUnderSweepAngle: Float
        get() = _arcUnderSweepAngle
        set(value) {
            _arcUnderSweepAngle = value
            invalidate()
        }

    var arcUnderUseCenter: Boolean
        get() = _arcUnderUseCenter
        set(value) {
            _arcUnderUseCenter = value
            invalidate()
        }

    var arcUnderReverseMode: ReverseMode
        get() = _arcUnderReverseMode
        set(value) {
            _arcUnderReverseMode = value
            invalidate()
        }

    var arcUpperWidth: Float
        get() = arcUpperPaint.strokeWidth
        set(value) {
            arcUpperPaint.strokeWidth = value
            invalidate()
        }

    var arcUpperStyle: Paint.Style
        get() = arcUpperPaint.style
        set(value) {
            arcUpperPaint.style = value
            invalidate()
        }

    var arcUpperCap: Paint.Cap
        get() = arcUpperPaint.strokeCap
        set(value) {
            arcUpperPaint.strokeCap = value
            invalidate()
        }

    var arcUpperColor: Int
        get() = arcUpperPaint.color
        set(value) {
            arcUpperPaint.color = value
            invalidate()
        }

    var arcUpperShader: Shader?
        get() = arcUpperPaint.shader
        set(value) {
            arcUpperPaint.shader = value
            invalidate()
        }

    var arcUpperStartAngle: Float
        get() = _arcUpperStartAngle
        set(value) {
            _arcUpperStartAngle = value
            invalidate()
        }

    var arcUpperSweepAngle: Float
        get() = _arcUpperSweepAngle
        set(value) {
            _arcUpperSweepAngle = value
            invalidate()
        }

    var arcUpperUseCenter: Boolean
        get() = _arcUpperUseCenter
        set(value) {
            _arcUpperUseCenter = value
            invalidate()
        }

    var arcUpperReverseMode: ReverseMode
        get() = _arcUpperReverseMode
        set(value) {
            _arcUpperReverseMode = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        if (_arcUnderSweepAngle > 0) {
            canvas.withScale(
                if (ReverseMode.HORIZONTAL == _arcUnderReverseMode) -1f else 1f,
                if (ReverseMode.VERTICAL == _arcUnderReverseMode) -1f else 1f,
                width / 2f,
                height / 2f
            ) {
                canvas.drawArc(
                    paddingLeft + arcUnderPaint.strokeWidth / 2,
                    paddingTop + arcUnderPaint.strokeWidth / 2,
                    width - paddingRight - arcUnderPaint.strokeWidth / 2,
                    height - paddingBottom - arcUnderPaint.strokeWidth / 2,
                    _arcUnderStartAngle,
                    _arcUnderSweepAngle,
                    _arcUnderUseCenter,
                    arcUnderPaint
                )
            }
        }
        if (_arcUpperSweepAngle > 0) {
            canvas.withScale(
                if (ReverseMode.HORIZONTAL == _arcUpperReverseMode) -1f else 1f,
                if (ReverseMode.VERTICAL == _arcUpperReverseMode) -1f else 1f,
                width / 2f,
                height / 2f
            ) {
                canvas.drawArc(
                    paddingLeft + arcUpperPaint.strokeWidth / 2,
                    paddingTop + arcUpperPaint.strokeWidth / 2,
                    width - paddingRight - arcUpperPaint.strokeWidth / 2,
                    height - paddingBottom - arcUpperPaint.strokeWidth / 2,
                    _arcUpperStartAngle,
                    _arcUpperSweepAngle,
                    _arcUpperUseCenter,
                    arcUpperPaint
                )
            }
        }
    }

    private fun Int.toPaintStyle() = let {
        when (it) {
            1 -> Paint.Style.FILL
            2 -> Paint.Style.STROKE
            3 -> Paint.Style.FILL_AND_STROKE
            else -> Paint.Style.FILL_AND_STROKE
        }
    }

    private fun Int.toPaintCap() = let {
        when (it) {
            1 -> Paint.Cap.BUTT
            2 -> Paint.Cap.ROUND
            3 -> Paint.Cap.SQUARE
            else -> Paint.Cap.BUTT
        }
    }

}