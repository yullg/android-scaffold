package com.yullg.android.scaffold.ui.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.content.res.getBooleanOrThrow
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getIntegerOrThrow
import com.yullg.android.scaffold.R

abstract class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    protected var _cvPaint = Paint()
    protected var _cvPaintAlpha: Int? = null
    protected var _cvPaintAntiAlias: Boolean? = null
    protected var _cvPaintColor: Int? = null
    protected var _cvPaintColorFilter: ColorFilter? = null
    protected var _cvPaintDither: Boolean? = null
    protected var _cvPaintFilterBitmap: Boolean? = null
    protected var _cvPaintHinting: Int? = null
    protected var _cvPaintMaskFilter: MaskFilter? = null
    protected var _cvPaintPathEffect: PathEffect? = null
    protected var _cvPaintShader: Shader? = null
    protected var _cvPaintStrokeCap: Paint.Cap? = null
    protected var _cvPaintStrokeJoin: Paint.Join? = null
    protected var _cvPaintStrokeMiter: Float? = null
    protected var _cvPaintStrokeWidth: Float? = null
    protected var _cvPaintStyle: Paint.Style? = null
    protected var _cvPaintXfermode: Xfermode? = null
    protected var _cvReverseMode: Int = ReverseMode.NONE

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CanvasView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                if (hasValue(R.styleable.CanvasView_yg_cvPaintAlpha)) {
                    getIntegerOrThrow(R.styleable.CanvasView_yg_cvPaintAlpha).let {
                        _cvPaint.alpha = it
                        _cvPaintAlpha = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintAntiAlias)) {
                    getBooleanOrThrow(R.styleable.CanvasView_yg_cvPaintAntiAlias).let {
                        _cvPaint.isAntiAlias = it
                        _cvPaintAntiAlias = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintColor)) {
                    getColorOrThrow(R.styleable.CanvasView_yg_cvPaintColor).let {
                        _cvPaint.color = it
                        _cvPaintColor = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintDither)) {
                    getBooleanOrThrow(R.styleable.CanvasView_yg_cvPaintDither).let {
                        _cvPaint.isDither = it
                        _cvPaintDither = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintFilterBitmap)) {
                    getBooleanOrThrow(R.styleable.CanvasView_yg_cvPaintFilterBitmap).let {
                        _cvPaint.isFilterBitmap = it
                        _cvPaintFilterBitmap = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintHinting)) {
                    getIntegerOrThrow(R.styleable.CanvasView_yg_cvPaintHinting).let {
                        _cvPaint.hinting = it
                        _cvPaintHinting = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintStrokeCap)) {
                    getIntegerOrThrow(R.styleable.CanvasView_yg_cvPaintStrokeCap).toPaintCap().let {
                        _cvPaint.strokeCap = it
                        _cvPaintStrokeCap = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintStrokeJoin)) {
                    getIntegerOrThrow(R.styleable.CanvasView_yg_cvPaintStrokeJoin).toPaintJoin()
                        .let {
                            _cvPaint.strokeJoin = it
                            _cvPaintStrokeJoin = it
                        }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintStrokeMiter)) {
                    getDimensionOrThrow(R.styleable.CanvasView_yg_cvPaintStrokeMiter).let {
                        _cvPaint.strokeMiter = it
                        _cvPaintStrokeMiter = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintStrokeWidth)) {
                    getDimensionOrThrow(R.styleable.CanvasView_yg_cvPaintStrokeWidth).let {
                        _cvPaint.strokeWidth = it
                        _cvPaintStrokeWidth = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvPaintStyle)) {
                    getIntegerOrThrow(R.styleable.CanvasView_yg_cvPaintStyle).toPaintStyle().let {
                        _cvPaint.style = it
                        _cvPaintStyle = it
                    }
                }
                if (hasValue(R.styleable.CanvasView_yg_cvReverseMode)) {
                    _cvReverseMode = getIntegerOrThrow(R.styleable.CanvasView_yg_cvReverseMode)
                }
            } finally {
                recycle()
            }
        }
    }

    var cvPaint: Paint
        get() = _cvPaint
        set(value) {
            _cvPaint = Paint(value).apply {
                _cvPaintAlpha?.let { alpha = it }
                _cvPaintAntiAlias?.let { isAntiAlias = it }
                _cvPaintColor?.let { color = it }
                _cvPaintColorFilter?.let { colorFilter = it }
                _cvPaintDither?.let { isDither = it }
                _cvPaintFilterBitmap?.let { isFilterBitmap = it }
                _cvPaintHinting?.let { hinting = it }
                _cvPaintMaskFilter?.let { maskFilter = it }
                _cvPaintPathEffect?.let { pathEffect = it }
                _cvPaintShader?.let { shader = it }
                _cvPaintStrokeCap?.let { strokeCap = it }
                _cvPaintStrokeJoin?.let { strokeJoin = it }
                _cvPaintStrokeMiter?.let { strokeMiter = it }
                _cvPaintStrokeWidth?.let { strokeWidth = it }
                _cvPaintStyle?.let { style = it }
                _cvPaintXfermode?.let { xfermode = it }
            }
        }

    var cvPaintAlpha: Int
        @IntRange(from = 0, to = 255)
        get() = _cvPaintAlpha ?: _cvPaint.alpha
        set(@IntRange(from = 0, to = 255) value) {
            _cvPaint.alpha = value
            _cvPaintAlpha = value
            invalidate()
        }

    var cvPaintAntiAlias: Boolean
        get() = _cvPaintAntiAlias ?: _cvPaint.isAntiAlias
        set(value) {
            _cvPaint.isAntiAlias = value
            _cvPaintAntiAlias = value
            invalidate()
        }

    var cvPaintColor: Int
        @ColorInt
        get() = _cvPaintColor ?: _cvPaint.color
        set(@ColorInt value) {
            _cvPaint.color = value
            _cvPaintColor = value
            invalidate()
        }

    var cvPaintColorFilter: ColorFilter?
        get() = _cvPaintColorFilter ?: _cvPaint.colorFilter
        set(value) {
            _cvPaint.colorFilter = value
            _cvPaintColorFilter = value
            invalidate()
        }

    var cvPaintDither: Boolean
        get() = _cvPaintDither ?: _cvPaint.isDither
        set(value) {
            _cvPaint.isDither = value
            _cvPaintDither = value
            invalidate()
        }

    var cvPaintFilterBitmap: Boolean
        get() = _cvPaintFilterBitmap ?: _cvPaint.isFilterBitmap
        set(value) {
            _cvPaint.isFilterBitmap = value
            _cvPaintFilterBitmap = value
            invalidate()
        }

    var cvPaintHinting: Int
        get() = _cvPaintHinting ?: _cvPaint.hinting
        set(value) {
            _cvPaint.hinting = value
            _cvPaintHinting = value
            invalidate()
        }

    var cvPaintMaskFilter: MaskFilter?
        get() = _cvPaintMaskFilter ?: _cvPaint.maskFilter
        set(value) {
            _cvPaint.maskFilter = value
            _cvPaintMaskFilter = value
            invalidate()
        }

    var cvPaintPathEffect: PathEffect?
        get() = _cvPaintPathEffect ?: _cvPaint.pathEffect
        set(value) {
            _cvPaint.pathEffect = value
            _cvPaintPathEffect = value
            invalidate()
        }

    var cvPaintShader: Shader?
        get() = _cvPaintShader ?: _cvPaint.shader
        set(value) {
            _cvPaint.shader = value
            _cvPaintShader = value
            invalidate()
        }

    var cvPaintStrokeCap: Paint.Cap
        get() = _cvPaintStrokeCap ?: _cvPaint.strokeCap
        set(value) {
            _cvPaint.strokeCap = value
            _cvPaintStrokeCap = value
            invalidate()
        }

    var cvPaintStrokeJoin: Paint.Join
        get() = _cvPaintStrokeJoin ?: _cvPaint.strokeJoin
        set(value) {
            _cvPaint.strokeJoin = value
            _cvPaintStrokeJoin = value
            invalidate()
        }

    var cvPaintStrokeMiter: Float
        get() = _cvPaintStrokeMiter ?: _cvPaint.strokeMiter
        set(value) {
            _cvPaint.strokeMiter = value
            _cvPaintStrokeMiter = value
            invalidate()
        }

    var cvPaintStrokeWidth: Float
        get() = _cvPaintStrokeWidth ?: _cvPaint.strokeWidth
        set(value) {
            _cvPaint.strokeWidth = value
            _cvPaintStrokeWidth = value
            invalidate()
        }

    var cvPaintStyle: Paint.Style
        get() = _cvPaintStyle ?: _cvPaint.style
        set(value) {
            _cvPaint.style = value
            _cvPaintStyle = value
            invalidate()
        }

    var cvPaintXfermode: Xfermode?
        get() = _cvPaintXfermode ?: _cvPaint.xfermode
        set(value) {
            _cvPaint.xfermode = value
            _cvPaintXfermode = value
            invalidate()
        }

    var cvReverseMode: Int
        @ReverseMode
        get() = _cvReverseMode
        set(@ReverseMode value) {
            _cvReverseMode = value
            invalidate()
        }

    protected fun containsReverseModeFlag(flag: Int): Boolean {
        return (_cvReverseMode or flag) == _cvReverseMode
    }

}

private fun Int.toPaintCap() = let {
    when (it) {
        1 -> Paint.Cap.BUTT
        2 -> Paint.Cap.ROUND
        3 -> Paint.Cap.SQUARE
        else -> throw IllegalArgumentException("Unable to convert the number [$it] to Paint.Cap")
    }
}

private fun Int.toPaintJoin() = let {
    when (it) {
        1 -> Paint.Join.BEVEL
        2 -> Paint.Join.MITER
        3 -> Paint.Join.ROUND
        else -> throw IllegalArgumentException("Unable to convert the number [$it] to Paint.Join")
    }
}

private fun Int.toPaintStyle() = let {
    when (it) {
        1 -> Paint.Style.FILL
        2 -> Paint.Style.STROKE
        3 -> Paint.Style.FILL_AND_STROKE
        else -> throw IllegalArgumentException("Unable to convert the number [$it] to Paint.Style")
    }
}