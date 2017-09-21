package com.fourthstatelab.trackr.Utils

import android.content.Context
import android.content.SharedPreferences
import com.fourthstatelab.trackr.Models.Credential


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Created by sid on 2/13/17.
 */

object Preference {
    private val prefName = "com.fourthstatelab.trackr"
    val MY_DEVICES = "my_devices"
    val CREDS = "my_creds"
    val LOST_DEVICES ="lost_devices"

    private val gson : Gson = Gson()


    //SHARED PREFERENCES INSTANCE OF THE APP
    private fun getPrefsInstance(context: Context): SharedPreferences {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    //SHARED PREFERENCES EDITOR INSTANCE OF THE APP
    private fun getPrefEditor(context: Context): SharedPreferences.Editor {
        return getPrefsInstance(context).edit()
    }

    operator fun get(context: Context, name: String, defaultValue: Boolean): Boolean {
        return getPrefsInstance(context).getBoolean(name, defaultValue)
    }

    operator fun get(context: Context, name: String, defaultValue: Int): Int {
        return getPrefsInstance(context).getInt(name, defaultValue)
    }

    operator fun get(context: Context, key: String,defaultValue: String) :String{
        return getPrefsInstance(context).getString(key,defaultValue)
    }

    //GENERICS
    fun <E> put(context: Context, key: String, value: E?) {
        val editor : SharedPreferences.Editor  = getPrefEditor(context)
        when(value){
            is Int->{editor.putInt(key,value) }
            is String->{ editor.putString(key,value)}
            is Boolean->{ editor.putBoolean(key,value)}
            else->{
                val toSave = gson.toJson(value)
                editor.putString(key,toSave)
            }
        }
        editor.apply()
    }

    operator fun <E> get(context: Context, key: String): E? {
        val json : String? = getPrefsInstance(context).getString(key, null)
        return if (json == null) {
            null
        } else {
            gson.fromJson<E>(json, object : TypeToken<E>(){}.type)
        }
    }
}
