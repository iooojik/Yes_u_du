package com.yes_u_du.zuyger.sharedprefs

import android.content.SharedPreferences

class MyPreferences {

    companion object {
        @JvmStatic
        var userPreferences: SharedPreferences? = null

        @JvmStatic
        var applicationPreferences: SharedPreferences? = null

        @JvmStatic
        fun saveToPreferences(preferences: SharedPreferences, key: String, value: String) {
            preferences.edit().putString(key, value).apply()
        }

        @JvmStatic
        fun saveToPreferences(preferences: SharedPreferences, key: String, value: Int) {
            preferences.edit().putInt(key, value).apply()
        }

        @JvmStatic
        fun saveToPreferences(preferences: SharedPreferences, key: String, value: Float) {
            preferences.edit().putFloat(key, value).apply()
        }

        @JvmStatic
        fun saveToPreferences(preferences: SharedPreferences, key: String, value: Boolean) {
            preferences.edit().putBoolean(key, value).apply()
        }

        @JvmStatic
        fun saveToPreferences(preferences: SharedPreferences, key: String, value: Long) {
            preferences.edit().putLong(key, value).apply()
        }

        @JvmStatic
        fun saveToPreferences(preferences: SharedPreferences, key: String, value: Set<String>) {
            preferences.edit().putStringSet(key, value).apply()
        }

        @JvmStatic
        fun clearAll() {
            userPreferences?.edit()?.clear()?.apply()
            applicationPreferences?.edit()?.clear()?.apply()
        }
    }

}