package com.yullg.android.scaffold.core;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.CLASS)
@IntDef({ArrayNumberSupplierMode.CLAMP, ArrayNumberSupplierMode.MIRROR, ArrayNumberSupplierMode.REPEAT})
public @interface ArrayNumberSupplierMode {
    /**
     * Replicate the last value
     */
    int CLAMP = 1;
    /**
     * Reverse traversal
     */
    int MIRROR = 2;
    /**
     * Back to square one
     */
    int REPEAT = 3;
}