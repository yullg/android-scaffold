package com.yullg.android.scaffold.helper

import java.io.PrintWriter
import java.io.StringWriter

object ExceptionHelper {

    fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        PrintWriter(sw).let {
            throwable.printStackTrace(it)
            it.flush()
        }
        return sw.toString()
    }

    fun getRootCause(throwable: Throwable): Throwable = getThrowableList(throwable).last()

    fun <T> extractLeniency(throwable: Throwable, type: Class<T>): T? =
        extract(throwable, type, true)

    fun <T> extractStrictly(throwable: Throwable, clazz: Class<T>): T? =
        extract(throwable, clazz, false)

    private fun <T> extract(throwable: Throwable, type: Class<T>, subclass: Boolean): T? {
        val throwableList: List<Any> = getThrowableList(throwable)
        if (subclass) {
            for (item in throwableList) {
                if (type.isAssignableFrom(item.javaClass)) {
                    return type.cast(item)
                }
            }
        } else {
            for (item in throwableList) {
                if (type == item.javaClass) {
                    return type.cast(item)
                }
            }
        }
        return null
    }

    private fun getThrowableList(throwable: Throwable): List<Throwable> {
        val list = ArrayList<Throwable>()
        var tempThrowable: Throwable? = throwable
        while (tempThrowable != null && !list.contains(tempThrowable)) {
            list.add(tempThrowable)
            tempThrowable = tempThrowable.cause
        }
        return list
    }

}