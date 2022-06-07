package com.appsol.advancedvoicechangeapp.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.appsol.advancedvoicechangeapp.MyApp
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.databinding.ActivitySplashBinding
import com.appsol.advancedvoicechangeapp.utils.AdMobInterstitial
import com.appsol.advancedvoicechangeapp.utils.FacebookAdsInterstitialAd
import com.appsol.advancedvoicechangeapp.utils.SharedPrefs
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.lang.Boolean
import kotlin.Exception
import kotlin.Long
import kotlin.String
import kotlin.also

class SplashActivity : AppCompatActivity() {
    private var appOpenAd: AppOpenAd? = null

    lateinit var splashBinding: ActivitySplashBinding

    private var handler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        splashBinding.animationView.also {
            it.imageAssetsFolder = "images"
            it.playAnimation()
        }
        FacebookAdsInterstitialAd.getDefaultInstance(this).mLaodFacebookIntersitional()
        AdMobInterstitial.getDefaultInstance(this).loadNewAdMobInterstitialAds()
        if (!AdMobInterstitial.checkConnection(this)) SPLASH_DISPLAY_LENGTH = 4000
        handler = Handler()
        handler!!.postDelayed({
            isHandler = true
            if (appOpenAd != null) {
                showAppOpenAds()
                splashBinding.progresBar.visibility = View.INVISIBLE
            } else {
                splashBinding.btnStart.also {
                    it.alpha = 1.0f
                    it.isEnabled=true
                }
                splashBinding.progresBar.visibility = View.INVISIBLE
            }
        }, SPLASH_DISPLAY_LENGTH)
        splashBinding.btnStart.setOnClickListener {
            if (appOpenAd != null) {
                showAppOpenAds()
            } else {
                launchActivity()
                if (!AdMobInterstitial.getDefaultInstance(this@SplashActivity)
                        .showInterstitialAd(this@SplashActivity)
                ) FacebookAdsInterstitialAd.getDefaultInstance(this).showFbInterstistial()
            }
        }
        val sharedPrefs = SharedPrefs(this)
        if (!sharedPrefs.premium!!) initAppOpenAds()
    }

    private fun launchActivity() {
        var intent = Intent(this@SplashActivity, MainActivity::class.java)
        var str: String? = null
        if (getIntent() != null) str = getIntent().getStringExtra("i")
        if (getIntent() != null && str != null && Boolean.parseBoolean(str)) {
            try {
                intent = Intent(this@SplashActivity, NotificationViewActivity::class.java)
                intent.putExtra("title", getIntent().getStringExtra("t"))
                intent.putExtra("dec", getIntent().getStringExtra("d"))
                intent.putExtra("link", getIntent().getStringExtra("l"))
                Log.d("TAG_", "launchActivity: Enter")
            } catch (e: Exception) {
                intent = Intent(this@SplashActivity, MainActivity::class.java)
                Log.d("TAG_", "launchActivity: " + e.message)
            }
        }
        startActivity(intent)
        val hasPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            (applicationContext as MyApp).initAppOpenAds()
        }
        finish()
    }

    private fun showAppOpenAds() {
        appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                launchActivity()
            }
        }
        appOpenAd!!.show(this@SplashActivity)
    }

    private var loadCallback: AppOpenAdLoadCallback? = null
    private fun initAppOpenAds() {
        MobileAds.initialize(this) { }
        Log.e("TAG", "initAppOpenAds: ")
        loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                Log.e("TAG_", "onAppOpenAdLoaded: ")
                appOpenAd = ad
                if (handler != null && !isHandler) {
                    handler!!.removeCallbacksAndMessages(null)
                    showAppOpenAds()
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("TAG", "onAppOpenAdFailedToLoad: " + loadAdError.message)
                if (handler != null) {
                    handler!!.removeCallbacksAndMessages(null)

                    splashBinding.btnStart.also {
                        it.alpha = 1.0f
                        it.isEnabled=true
                    }
                    splashBinding.progresBar.visibility = View.INVISIBLE
                }
            }
        }
        AppOpenAd.load(
            this@SplashActivity,
            getString(R.string.admob_app_open_ads),
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback!!
        )
    }

    companion object {
        private var SPLASH_DISPLAY_LENGTH: Long = 7500
        private var isHandler = false
    }
}