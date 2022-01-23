package com.yullg.android.scaffold.ui.widget;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义如何调整布局来适配窗口inserts
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@IntDef(value = {SafeAreaApplyMode.MARGIN, SafeAreaApplyMode.PADDING})
public @interface SafeAreaApplyMode {
    /**
     * 通过调整margin来适配窗口inserts
     */
    int MARGIN = 1;
    /**
     * 通过调整padding来适配窗口inserts
     */
    int PADDING = 2;
}