package com.simsinfotekno.maghribmengaji.model

import android.app.Activity
import android.content.Context

class MaghribMengajiPref {

    companion object {

        val USER_NAME_KEY = "UserNameKey"

        fun readString(activity: Activity, key: String?): String? {
            return activity.getPreferences(Context.MODE_PRIVATE).getString(key, null)
        }

        fun readString(activity: Activity, key: String?, defaultValue: String?): String? {
            return activity.getPreferences(Context.MODE_PRIVATE).getString(key, defaultValue)
        }

        fun saveString(activity: Activity, key: String?, value: String?) {
            val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(key, value)
            editor.apply()
        }

    }
}