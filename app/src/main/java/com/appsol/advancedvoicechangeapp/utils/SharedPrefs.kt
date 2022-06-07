package com.appsol.advancedvoicechangeapp.utils

import android.content.Context

class SharedPrefs(private val context: Context) {
    private val App_Name = "voice_prefs" //DB name
    private val USER_PREMIUM = "changer_premium"
    private val GET_PURCHASE_DAILOG = "dailog"
    var premium: Boolean?
        get() {
            val sp = context.getSharedPreferences(App_Name, Context.MODE_PRIVATE)
            return sp.getBoolean(USER_PREMIUM, false)
        }
        set(premium) {
            val sp = context.getSharedPreferences(App_Name, Context.MODE_PRIVATE)
            sp.edit().putBoolean(USER_PREMIUM, premium!!).apply()
        }

    fun setPurchaeDailogValue(context: Context, value: Int) {
        val sp = context.getSharedPreferences(App_Name, Context.MODE_PRIVATE)
        sp.edit().putInt(GET_PURCHASE_DAILOG, value).apply()
    }


    val purchaeDailogValue: Int
        get() {
            val sp = context.getSharedPreferences(App_Name, Context.MODE_PRIVATE)
            return sp.getInt(GET_PURCHASE_DAILOG, 2)
        }

    fun setRatingDailogValue(context: Context, value: Int) {
        val sp = context.getSharedPreferences(App_Name, Context.MODE_PRIVATE)
        sp.edit().putInt(GET_RATING_DAILOG, value).apply()
    }

    val ratingDailogValue: Int
        get() {
            val sp = context.getSharedPreferences(App_Name, Context.MODE_PRIVATE)
            return sp.getInt(GET_RATING_DAILOG, 5)
        }

    companion object {

//        @Volatile
//        private lateinit var instance: SharedPrefs

//        @JvmStatic
//        fun getInstance(context: Context): SharedPrefs {
//            if (::instance.isInitialized.not()) {
//                synchronized(SharedPrefs::class.java) {
//                    if (::instance.isInitialized.not()) {
//                        instance = SharedPrefs(context)
//                    }
//                }
//            }
//            return instance
//        }

        private const val GET_RATING_DAILOG = "rating_dialog"
    }
}