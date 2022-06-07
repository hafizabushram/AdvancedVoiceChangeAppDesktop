package com.appsol.advancedvoicechangeapp.utils;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdkUtils;
import com.appsol.advancedvoicechangeapp.MyApp;
import com.appsol.advancedvoicechangeapp.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;


import java.util.concurrent.TimeUnit;


public class FacebookAdsInterstitialAd implements InterstitialAdListener {
    private static InterstitialAd ad;
    private Activity context;
    private static FacebookAdsInterstitialAd facebookAdsInterstitialAd;
    private String fbInterstitialAD_id;

    private static MaxInterstitialAd interstitialAd;
    private static int retryAttempt;
    private static SharedPrefs sharedPrefs;

    private FacebookAdsInterstitialAd(Activity ctx) {
        context = ctx;
//        AudienceNetworkInitializeHelper.initialize(ctx);
//        fbInterstitialAD_id = ctx.getResources().getString(R.string.facebook_Interstitial);
    }

    public static FacebookAdsInterstitialAd getDefaultInstance(Activity context) {
        sharedPrefs = new SharedPrefs(context);
        if (facebookAdsInterstitialAd == null || ad == null)
            facebookAdsInterstitialAd = new FacebookAdsInterstitialAd(context);
        return facebookAdsInterstitialAd;
    }

    public boolean showFbInterstistial() {
        if (sharedPrefs.getPremium())
            return false;
        if (interstitialAd != null && interstitialAd.isReady()) {
            interstitialAd.showAd();
            if (context != null)
                ((MyApp) context.getApplicationContext()).setInterstialShown(true);
            return true;
        } else return false;
//        if (ad == null || !ad.isAdLoaded()) {
//            return false;
//        }
//        // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
//        if (ad.isAdInvalidated()) {
//            return false;
//        }
//        // Show the ad
//        ad.show();
//        return true;
    }


    public void mLaodFacebookIntersitional() {
        if (sharedPrefs.getPremium())
            return;
        interstitialAd = new MaxInterstitialAd(context.getString(R.string.appLivin_Interstitial), context);
        MaxAdListener maxAdListener = new MaxAdListener() {
            @Override
            public void onAdLoaded(final MaxAd maxAd) {
                retryAttempt = 0;
            }

            @Override
            public void onAdLoadFailed(final String adUnitId, final MaxError error) {
                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        interstitialAd.loadAd();
                    }
                }, delayMillis);
            }

            @Override
            public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {
                interstitialAd.loadAd();
            }

            @Override
            public void onAdDisplayed(final MaxAd maxAd) {
            }

            @Override
            public void onAdClicked(final MaxAd maxAd) {
            }

            @Override
            public void onAdHidden(final MaxAd maxAd) {
                interstitialAd.loadAd();
                if (context != null)
                    ((MyApp) context.getApplicationContext()).setInterstialShown(false);
            }
        };
        interstitialAd.setListener(maxAdListener);
        interstitialAd.loadAd();
//        if (!fbInterstitialAD_id.equals(""))
//            ad = new InterstitialAd(
//                    context,
//                    fbInterstitialAD_id);
//
//        Log.d("FACEBOOK_ADS", "mLaodThemeIntersitional");
//        ad.setAdListener(FacebookAdsInterstitialAd.this);
//        ad.loadAd();

    }

    public boolean isAdLoaded() {
        if (ad != null) {
            if (ad.isAdLoaded())
                return true;
            else return false;
        }
        return false;
    }

    public static void getApplovinBanner(Activity context, FrameLayout frameLayout) {
        MaxAdView adView = new MaxAdView(context.getString(R.string.appLivin_Banner), context);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });

        // Stretch to the width of the screen for banners to be fully functional
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Get the adaptive banner height.
        int heightDp = MaxAdFormat.BANNER.getAdaptiveSize(context).getHeight();
        int heightPx = AppLovinSdkUtils.dpToPx(context, heightDp);

        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
        adView.setExtraParameter("adaptive_banner", "true");

        // Set background or background color for banners to be fully functional
        adView.setBackgroundColor(context.getResources().getColor(R.color.colorAdsbg));
        frameLayout.addView(adView);
        // Load the ad
        adView.loadAd();
    }


    @Override
    public void onInterstitialDisplayed(Ad ad) {
        Log.d("FACEBOOK_ADS", "mLaodThemeIntersitional");
    }

    @Override
    public void onInterstitialDismissed(Ad ad) {
//        ad.loadAd();

    }

    @Override
    public void onError(Ad ad, AdError adError) {

    }

    @Override
    public void onAdLoaded(Ad ad) {
        Log.d("FACEBOOK_ADS", "onAdLoaded");
    }

    @Override
    public void onAdClicked(Ad ad) {
        Log.d("FACEBOOK_ADS", "onAdClicked");
    }

    @Override
    public void onLoggingImpression(Ad ad) {
        Log.d("FACEBOOK_ADS", "onLoggingImpression");
    }
}
