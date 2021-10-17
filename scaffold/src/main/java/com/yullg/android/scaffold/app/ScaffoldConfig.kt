package com.yullg.android.scaffold.app

import com.yullg.android.scaffold.support.logger.LoggerConfig
import com.yullg.android.scaffold.support.logger.MutableLoggerConfig
import com.yullg.android.scaffold.support.media.MediaConfig
import com.yullg.android.scaffold.support.media.MutableMediaConfig
import com.yullg.android.scaffold.ui.MutableUIConfig
import com.yullg.android.scaffold.ui.UIConfig

class ScaffoldConfig internal constructor() {

    fun logger(block: MutableLoggerConfig.() -> Unit): ScaffoldConfig {
        block(MutableLoggerConfig)
        return this
    }

    fun media(block: MutableMediaConfig.() -> Unit): ScaffoldConfig {
        block(MutableMediaConfig)
        return this
    }

    fun ui(block: MutableUIConfig.() -> Unit): ScaffoldConfig {
        block(MutableUIConfig)
        return this
    }

    companion object {

        val Logger: LoggerConfig get() = MutableLoggerConfig

        val Media: MediaConfig get() = MutableMediaConfig

        val UI: UIConfig get() = MutableUIConfig

    }

}

/*
 * --- 配置类的设计思路 ---
 * 本库仅允许在初始化过程中进行配置修改，但可以在任何时候访问配置。通过有意的设计来保证这一点：
 * ScaffoldConfig组合各模块的配置，提供唯一的出入口。internal修饰的构造器将实例创建能力限制在库内部，通过实例方法
 * 可访问各模块配置对象的可修改版本，公开的伴生对象仅提供对各模块配置对象的只读版本。
 * 各模块配置类由只读接口和可修改版本实现类两部分组成，实现类的构造器使用private修饰，internal修饰的伴生对象是其唯一实例。
 * 这样做在公开修改版本接口的同时也保证无法在库的外部创建实例。
 */