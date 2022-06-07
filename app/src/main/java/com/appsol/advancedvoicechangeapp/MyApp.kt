package com.appsol.advancedvoicechangeapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.firebase.database.*
import java.lang.Boolean
import kotlin.let
import kotlin.toString

class MyApp : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {
    var isBannerAds = false
    private var mDatabase: DatabaseReference? = null
    var isInterstialShown = false
    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase!!.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("isBanner").value != null) {
                    val user = dataSnapshot.child("isBanner").value.toString()
                    isBannerAds = Boolean.parseBoolean(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Log.d("TAG_C", "onActivityCreated: ")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d("TAG_C", "onActivityStarted: ")
    }

    var act: Activity? = null
    override fun onActivityResumed(activity: Activity) {
        act = activity
    }

//    private fun showAppOpenAds(activity: Activity?) {
//        Log.d("TAG_S", "showAppOpenAds: " + activity!!.javaClass.name)
//        if (activity.javaClass.name == "com.google.android.gms.ads.AdActivity" || activity.javaClass.name == "com.appsol.advancedvoicechangeapp.activity.MyAudioFileActivity") return
//        Log.d("TAG_S", "showAppOpenAds: MYAPP ")
//        if (appOpenAd != null) {
//            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
//                override fun onAdShowedFullScreenContent() {
//                    super.onAdShowedFullScreenContent()
//                }
//
//                override fun onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent()
//                    initAppOpenAds()
//                }
//            }
//            appOpenAd!!.show(activity)
//        }
//    }


    override fun onActivityPaused(activity: Activity) {
        Log.d("TAG_C", "onActivityPaused: ")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("TAG_C", "onActivityStopped: ")
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        Log.d("TAG_C", "onActivitySaveInstanceState: ")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("TAG_C", "onActivityDestroyed: ")
    }

    private var loadCallback: AppOpenAdLoadCallback? = null
    var appOpenAd: AppOpenAd? = null
    fun initAppOpenAds() {
        MobileAds.initialize(this) { initializationStatus: InitializationStatus? -> }
        Log.e("TAG", "initAppOpenAds: ")
        loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.e("TAG", "onAppOpenAdLoaded: ")
                appOpenAd = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("TAG", "onAppOpenAdFailedToLoad: " + loadAdError.message)
            }
        }
        appContext?.let {
            loadCallback?.let { it1 ->
                AppOpenAd.load(
                    it,
                    getString(R.string.admob_app_open_ads),
                    AdRequest.Builder().build(),
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    it1
                )
            }
        }
    }

    companion object {
        var appContext: Context? = null


        /**
         * Calculate density pixels per second for record duration.
         * Used for visualisation waveform in view.
         *
         * @param Duration Seconds record duration in seconds
         */


    }
}