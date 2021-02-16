package com.yullg.android.scaffold.ui.canvas;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(CLASS)
@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE, ANNOTATION_TYPE})
@IntDef(value = {ReverseMode.NONE, ReverseMode.HORIZONTAL, ReverseMode.VERTICAL}, flag = true)
public @interface ReverseMode {
    int NONE = 0;
    int HORIZONTAL = 1;
    int VERTICAL = 2;
}