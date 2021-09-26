package com.yullg.android.scaffold.ui.widget;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.CLASS)
@IntDef(value = {SafeAreaApplyMode.MARGIN, SafeAreaApplyMode.PADDING})
public @interface SafeAreaApplyMode {
    int MARGIN = 1;
    int PADDING = 2;
}