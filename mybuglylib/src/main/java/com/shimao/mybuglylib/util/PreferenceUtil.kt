package com.shimao.mybuglylib.util

import android.content.Context

/**
 * Created by sreay on 14-8-19. reBuild by jian on 19/10/10
 */
object PreferenceUtil {
    private val PreferenceName = "jjBugly"

    fun setStringValue(context: Context, key: String, value: String) {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringValue(context: Context, key: String, defaultValue: String): String {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        return preferences.getString(key, defaultValue)?:defaultValue
    }

    fun getIntValue(context: Context, key: String, defaultValue: Int): Int {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        return preferences.getInt(key, defaultValue)
    }

    fun setIntValue(context: Context, key: String, value: Int) {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getBooleanValue(context: Context, key: String, defaultValue: Boolean): Boolean {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        return preferences.getBoolean(key, defaultValue)
    }

    fun setBooleanValue(context: Context, key: String, value: Boolean) {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
}
