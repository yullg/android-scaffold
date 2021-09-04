package com.yullg.android.scaffold.ui.widget;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;

@Documented
@IntDef(value = {SafeAreaApplyMode.MARGIN, SafeAreaApplyMode.PADDING})
public @interface SafeAreaApplyMode {
    int MARGIN = 1;
    int PADDING = 2;
}