package com.yullg.android.scaffold.core

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