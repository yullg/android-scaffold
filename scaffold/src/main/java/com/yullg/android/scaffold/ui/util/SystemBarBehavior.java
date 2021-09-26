package com.yullg.android.scaffold.ui.util;

import androidx.annotation.IntDef;
import androidx.core.view.WindowInsetsControllerCompat;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.CLASS)
@IntDef(value = {SystemBarBehavior.BEHAVIOR_SHOW_BARS_BY_TOUCH, SystemBarBehavior.BEHAVIOR_SHOW_BARS_BY_SWIPE, SystemBarBehavior.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE})
public @interface SystemBarBehavior {
    int BEHAVIOR_SHOW_BARS_BY_TOUCH = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH;
    int BEHAVIOR_SHOW_BARS_BY_SWIPE = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE;
    int BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;
}