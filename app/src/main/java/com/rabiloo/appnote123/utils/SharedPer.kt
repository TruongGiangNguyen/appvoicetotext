package com.rabiloo.appnote123.utils

import android.content.Context
import android.content.SharedPreferences
import com.rabiloo.appnote123.key.KEY
import java.security.Key

object SharedPer {
    fun saveEmail(e: String, c: Context){
        val sharedPreferences: SharedPreferences = c.getSharedPreferences(KEY.EMAIL, Context.MODE_PRIVATE)
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putString(KEY.EMAIL, e)
        edit.apply()
    }
    fun getEmail(c: Context): String? {
        val sharedPreferences: SharedPreferences = c.getSharedPreferences(KEY.EMAIL, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY.EMAIL, "");
    }
}