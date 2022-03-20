package com.yullg.android.scaffold.support.location;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义在定位过程中缓存数据的使用方式
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@IntDef({CacheUseMode.IGNORE, CacheUseMode.FIRST, CacheUseMode.ONLY})
public @interface CacheUseMode {

    /**
     * 忽略缓存数据
     */
    int IGNORE = 0;
    /**
     * 优先使用缓存数据
     */
    int FIRST = 1;
    /**
     * 仅使用缓存数据
     */
    int ONLY = 2;

}