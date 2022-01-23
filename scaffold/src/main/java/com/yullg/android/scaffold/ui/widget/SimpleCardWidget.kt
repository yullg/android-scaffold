package com.yullg.android.scaffold.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getIntegerOrThrow
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.yullg.android.scaffold.R

/**
 * 一个[FrameLayout]子类，使用[MaterialShapeDrawable]作为背景来提供类似卡片的显示效果。
 *
 * @see MaterialShapeDrawable
 */
open class SimpleCardWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    protected val cardBackgroundDrawable = CardBackgroundDrawable()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SimpleCardWidget,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                if (hasValue(R.styleable.SimpleCardWidget_yg_cardAlpha)) {
                    cardBackgroundDrawable.alpha =
                        getIntegerOrThrow(R.styleable.SimpleCardWidget_yg_cardAlpha)
                }
                if (hasValue(R.styleable.SimpleCardWidget_yg_cardElevation)) {
                    cardBackgroundDrawable.elevation =
                        getDimensionOrThrow(R.styleable.SimpleCardWidget_yg_cardElevation)
                }
                if (hasValue(R.styleable.SimpleCardWidget_yg_cardFillColor)) {
                    cardBackgroundDrawable.fillColor =
                        getColorStateList(R.styleable.SimpleCardWidget_yg_cardFillColor)
                }
                if (hasValue(R.styleable.SimpleCardWidget_yg_cardShadowColor)) {
                    cardBackgroundDrawable.setShadowColor(getColorOrThrow(R.styleable.SimpleCardWidget_yg_cardShadowColor))
                }
                if (hasValue(R.styleable.SimpleCardWidget_yg_cardStrokeColor)) {
                    cardBackgroundDrawable.strokeColor =
                        getColorStateList(R.styleable.SimpleCardWidget_yg_cardStrokeColor)
                }
                if (hasValue(R.styleable.SimpleCardWidget_yg_cardStrokeWidth)) {
                    cardBackgroundDrawable.strokeWidth =
                        getDimensionOrThrow(R.styleable.SimpleCardWidget_yg_cardStrokeWidth)
                }
                cardBackgroundDrawable.shapeAppearanceModel = ShapeAppearanceModel.builder().also {
                    val cornerFamily: Int? =
                        if (hasValue(R.styleable.SimpleCardWidget_yg_cardCornerFamily))
                            getIntOrThrow(R.styleable.SimpleCardWidget_yg_cardCornerFamily).toCornerFamily()
                        else null
                    val cornerSize: Float? =
                        if (hasValue(R.styleable.SimpleCardWidget_yg_cardCornerSize))
                            getDimensionOrThrow(R.styleable.SimpleCardWidget_yg_cardCornerSize)
                        else null
                    setOneCorner(
                        this,
                        R.styleable.SimpleCardWidget_yg_cardTopLeftCornerFamily,
                        R.styleable.SimpleCardWidget_yg_cardTopLeftCornerSize,
                        cornerFamily,
                        cornerSize
                    ) { family, size -> it.setTopLeftCorner(family, size) }
                    setOneCorner(
                        this,
                        R.styleable.SimpleCardWidget_yg_cardTopRightCornerFamily,
                        R.styleable.SimpleCardWidget_yg_cardTopRightCornerSize,
                        cornerFamily,
                        cornerSize
                    ) { family, size -> it.setTopRightCorner(family, size) }
                    setOneCorner(
                        this,
                        R.styleable.SimpleCardWidget_yg_cardBottomLeftCornerFamily,
                        R.styleable.SimpleCardWidget_yg_cardBottomLeftCornerSize,
                        cornerFamily,
                        cornerSize
                    ) { family, size -> it.setBottomLeftCorner(family, size) }
                    setOneCorner(
                        this,
                        R.styleable.SimpleCardWidget_yg_cardBottomRightCornerFamily,
                        R.styleable.SimpleCardWidget_yg_cardBottomRightCornerSize,
                        cornerFamily,
                        cornerSize
                    ) { family, size -> it.setBottomRightCorner(family, size) }
                }.build()
                cardBackgroundDrawable.shadowCompatibilityMode =
                    MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
            } finally {
                recycle()
            }
        }
        background = cardBackgroundDrawable
    }

    var cardAlpha: Int
        get() = cardBackgroundDrawable.alpha
        set(value) {
            cardBackgroundDrawable.alpha = value
        }

    var cardElevation: Float
        get() = cardBackgroundDrawable.elevation
        set(value) {
            cardBackgroundDrawable.elevation = value
        }

    var cardFillColor: ColorStateList?
        get() = cardBackgroundDrawable.fillColor
        set(value) {
            cardBackgroundDrawable.fillColor = value
        }

    var cardShadowColor: Int
        get() = cardBackgroundDrawable.getShadowColor()
        set(value) {
            cardBackgroundDrawable.setShadowColor(value)
        }

    var cardStrokeColor: ColorStateList?
        get() = cardBackgroundDrawable.strokeColor
        set(value) {
            cardBackgroundDrawable.strokeColor = value
        }

    var cardStrokeWidth: Float
        get() = cardBackgroundDrawable.strokeWidth
        set(value) {
            cardBackgroundDrawable.strokeWidth = value
        }

    var cardShapeAppearanceModel: ShapeAppearanceModel
        get() = cardBackgroundDrawable.shapeAppearanceModel
        set(value) {
            cardBackgroundDrawable.shapeAppearanceModel = value
        }

    fun setCardCornerSize(cornerSize: Float) = cardBackgroundDrawable.setCornerSize(cornerSize)

    private fun setOneCorner(
        typedArray: TypedArray,
        familyIndex: Int,
        sizeIndex: Int,
        defaultFamily: Int?,
        defaultSize: Float?,
        block: (cornerFamily: Int, cornerSize: Float) -> Unit
    ) {
        val family: Int? = if (typedArray.hasValue(familyIndex))
            typedArray.getIntegerOrThrow(familyIndex).toCornerFamily()
        else defaultFamily
        val size: Float? = if (typedArray.hasValue(sizeIndex))
            typedArray.getDimensionOrThrow(sizeIndex)
        else defaultSize
        if (size != null) {
            block(family ?: CornerFamily.ROUNDED, size)
        }
    }

    protected inner class CardBackgroundDrawable : MaterialShapeDrawable() {

        private var shadowColorBackup: Int = Color.BLACK

        override fun getShadowOffsetX(): Int = 0

        override fun getShadowOffsetY(): Int = 0

        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
            super.setBounds(
                left + cardBackgroundDrawable.shadowRadius,
                top + cardBackgroundDrawable.shadowRadius,
                right - cardBackgroundDrawable.shadowRadius,
                bottom - cardBackgroundDrawable.shadowRadius
            )
        }

        override fun setShadowColor(shadowColor: Int) {
            super.setShadowColor(shadowColor)
            shadowColorBackup = shadowColor
        }

        fun getShadowColor() = shadowColorBackup

    }

}

private fun Int.toCornerFamily() = let {
    when (it) {
        1 -> CornerFamily.ROUNDED
        2 -> CornerFamily.CUT
        else -> throw IllegalArgumentException("Unable to convert the number [$it] to CornerFamily")
    }
}