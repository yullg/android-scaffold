package com.yullg.android.scaffold.helper

import android.content.Context
import android.content.SharedPreferences
import com.yullg.android.scaffold.app.Scaffold
import com.yullg.android.scaffold.app.ScaffoldConstants

/**
 * 提供键值存储相关的辅助功能
 */
class SPHelper(name: String) : ISharedPreferences {

    private val sp: SharedPreferences by lazy {
        Scaffold.context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    override fun contains(key: String): Boolean = sp.contains(key)

    override fun getAll(): Map<String, *> = sp.all

    override fun getBoolean(key: String, defValue: Boolean): Boolean = sp.getBoolean(key, defValue)

    override fun getFloat(key: String, defValue: Float): Float = sp.getFloat(key, defValue)

    override fun getInt(key: String, defValue: Int): Int = sp.getInt(key, defValue)

    override fun getLong(key: String, defValue: Long): Long = sp.getLong(key, defValue)

    override fun getString(key: String, defValue: String?): String? = sp.getString(key, defValue)

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? =
        sp.getStringSet(key, defValue)

    override fun clear() = sp.edit().clear().apply()

    override fun putBoolean(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()

    override fun putFloat(key: String, value: Float) = sp.edit().putFloat(key, value).apply()

    override fun putInt(key: String, value: Int) = sp.edit().putInt(key, value).apply()

    override fun putLong(key: String, value: Long) = sp.edit().putLong(key, value).apply()

    override fun putString(key: String, value: String?) = sp.edit().putString(key, value).apply()

    override fun putStringSet(key: String, value: Set<String>?) =
        sp.edit().putStringSet(key, value).apply()

    override fun remove(key: String) = sp.edit().remove(key).apply()

    companion object : ISharedPreferences by SPHelper(ScaffoldConstants.SP.NAME_DEFAULT)

}

interface ISharedPreferences {

    fun contains(key: String): Boolean

    fun getAll(): Map<String, *>

    fun getBoolean(key: String, defValue: Boolean): Boolean

    fun getFloat(key: String, defValue: Float): Float

    fun getInt(key: String, defValue: Int): Int

    fun getLong(key: String, defValue: Long): Long

    fun getString(key: String, defValue: String?): String?

    fun getStringSet(key: String, defValue: Set<String>?): Set<String>?

    fun clear()

    fun putBoolean(key: String, value: Boolean)

    fun putFloat(key: String, value: Float)

    fun putInt(key: String, value: Int)

    fun putLong(key: String, value: Long)

    fun putString(key: String, value: String?)

    fun putStringSet(key: String, value: Set<String>?)

    fun remove(key: String)

}