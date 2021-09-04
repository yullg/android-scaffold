package com.yullg.android.scaffold.core

import kotlin.math.max
import kotlin.math.min

interface NumberSupplier {

    fun get(): Long

    fun reset() {}

}

/**
 * Example : 1,1,1,1,1,1
 */
class FixedNumberSupplier(
    private val value: Long
) : NumberSupplier {

    override fun get(): Long = value

}

/**
 * Example : array element
 */
class ArrayNumberSupplier(
    private val values: Array<Long>,
    @ArrayNumberSupplierMode private val mode: Int = ArrayNumberSupplierMode.CLAMP
) : NumberSupplier {

    private var nextIndex: Int = 0
    private var inverse: Boolean = false

    override fun get(): Long = synchronized(this) {
        val result = values[nextIndex]
        when (mode) {
            ArrayNumberSupplierMode.CLAMP -> {
                if (nextIndex < values.size - 1) {
                    nextIndex += 1
                }
            }
            ArrayNumberSupplierMode.MIRROR -> {
                if (inverse) {
                    if (nextIndex > 0) {
                        nextIndex -= 1
                    } else {
                        nextIndex = min(1, values.size - 1)
                        inverse = false
                    }
                } else {
                    if (nextIndex < values.size - 1) {
                        nextIndex += 1
                    } else {
                        nextIndex = max(0, values.size - 2)
                        inverse = true
                    }
                }
            }
            ArrayNumberSupplierMode.REPEAT -> {
                if (nextIndex < values.size - 1) {
                    nextIndex += 1
                } else {
                    nextIndex = 0
                }
            }
        }
        return result
    }

    override fun reset() = synchronized(this) {
        nextIndex = 0
    }

}

/**
 * Example : 1,2,3,4,5,6
 */
class LinearIncreaseNumberSupplier(
    private val value: Long,
    private val maxValue: Long = Long.MAX_VALUE,
) : NumberSupplier {

    private var nextValue: Long = min(value, maxValue)

    override fun get(): Long = synchronized(this) {
        val result = nextValue
        nextValue = if (maxValue - value > nextValue) {
            nextValue + value
        } else {
            maxValue
        }
        return result
    }

    override fun reset() = synchronized(this) {
        nextValue = min(value, maxValue)
    }

}

/**
 * Example : 1,2,4,8,16,32
 */
class ExponentialIncreaseNumberSupplier(
    private val value: Long,
    private val maxValue: Long = Long.MAX_VALUE,
) : NumberSupplier {

    private var nextValue: Long = min(value, maxValue)

    override fun get(): Long = synchronized(this) {
        val result = nextValue
        nextValue = if (maxValue / 2 > nextValue) {
            nextValue * 2
        } else {
            maxValue
        }
        result
    }

    override fun reset() = synchronized(this) {
        nextValue = min(value, maxValue)
    }

}