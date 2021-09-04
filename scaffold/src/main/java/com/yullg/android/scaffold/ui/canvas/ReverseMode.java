package com.yullg.android.scaffold.ui.canvas;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;

@Documented
@IntDef(value = {ReverseMode.NONE, ReverseMode.HORIZONTAL, ReverseMode.VERTICAL}, flag = true)
public @interface ReverseMode {
    int NONE = 0;
    int HORIZONTAL = 1;
    int VERTICAL = 2;
}