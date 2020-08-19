package com.yullg.android.scaffold.ui.chart

enum class ReverseMode {
    NONE, HORIZONTAL, VERTICAL
}

fun Int.toReverseMode() = let {
    when (it) {
        1 -> ReverseMode.HORIZONTAL
        2 -> ReverseMode.VERTICAL
        else -> ReverseMode.NONE
    }
}