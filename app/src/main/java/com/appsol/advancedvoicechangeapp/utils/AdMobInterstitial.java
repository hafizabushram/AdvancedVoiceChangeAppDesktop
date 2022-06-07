package com.appsol.advancedvoicechangeapp.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.appsol.advancedvoicechangeapp.MyApp;
import com.appsol.advancedvoicechangeapp.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Objects;


public class AdMobInterstitial extends FullScreenContentCallback {
    private static int mAds_request = 0;
    private static long mLastClickTime;

    private InterstitialAd ad;
    public static int count = 1;

    private String tag = "ad_test";
    private static AdMobInterstitial adClass;


    private static Context context;

    private static SharedPrefs sharedPrefs;


    private AdMobInterstitial(Context ctx) {
        context = ctx;
    }

    public static AdMobInterstitial getDefaultInstance(Context context) {
        sharedPrefs = new SharedPrefs(context);
        if (adClass == null)
            adClass = new AdMobInterstitial(context);
        return adClass;
    }

    public boolean showInterstitialAd(Activity activity) {
        if (sharedPrefs.getPremium())
            return false;
        if (ad != null) {
            Log.d(tag, "showInterstitialAd(): Ad Loaded");
            ad.show(activity);
            ((MyApp) activity.getApplicationContext()).setInterstialShown(true);
            return true;
        } else {
            if (AdMobInterstitial.mAds_request < 2 && checkConnection(activity)) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 4000) {
                    return false;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                AdMobInterstitial.mAds_request++;
                loadNewAdMobInterstitialAds();
                Log.d("TAG_Ads", "getShowAds: " + AdMobInterstitial.mAds_request);
            }
        }
        return false;
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent();
        loadNewAdMobInterstitialAds();
        if (context != null)
            ((MyApp) context.getApplicationContext()).setInterstialShown(false);
    }

    @Override
    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
        super.onAdFailedToShowFullScreenContent(adError);
    }

    @Override
    public void onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent();
    }

    public void loadNewAdMobInterstitialAds() {
        if (sharedPrefs.getPremium())
            return;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getString(R.string.admob_interstitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        ad = interstitialAd;
                        ad.setFullScreenContentCallback(AdMobInterstitial.this);
                        Log.i("TAG_Ads", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("TAG_Ads", "onAdFailedToLoad: " + loadAdError.getMessage());

                        ad = null;
                    }
                });


    }

    public static void getAdmobAds(Context context, FrameLayout adContainerView) {
        sharedPrefs = new SharedPrefs(context);
        if (sharedPrefs.getPremium()) {
            adContainerView.setVisibility(View.GONE);
            return;
        }
        if (!((MyApp) context.getApplicationContext()).isBannerAds()) {
            refreshAdSmall(context, adContainerView);
            return;
        }
        AdView adView;
        adView = new AdView(context);
        adView.setAdUnitId(context.getString(R.string.admob_banner_id));
        adContainerView.addView(adView);
        AdRequest adRequest =
                new AdRequest.Builder()
                        .build();
        AdSize adSize = getAdSize(context);
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);
        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);

    }

    private static AdSize getAdSize(Context context) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    private static void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
        if (nativeAd.getMediaContent().getVideoController().hasVideoContent()) {
            nativeAd.getMediaContent().getVideoController().setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {

                    super.onVideoEnd();
                }
            });
        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     */
    public static void refreshAd(final Context context, final FrameLayout frameLayout) {
        SharedPrefs sharedPrefs = new SharedPrefs(context);
        if (sharedPrefs.getPremium() || !checkConnection(context)) {
            frameLayout.setVisibility(View.GONE);
            return;
        }

        final NativeAd[] nativeAd = new NativeAd[1];
        AdLoader.Builder builder = new AdLoader.Builder(context, context.getResources().getString(R.string.admob_native_id));

        // OnUnifiedNativeAdLoadedListener implementation.
        builder.forNativeAd(unifiedNativeAd -> {
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            if (nativeAd[0] != null) {
                nativeAd[0].destroy();
            }
            nativeAd[0] = unifiedNativeAd;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) inflater
                    .inflate(R.layout.ad_unified, null);
            populateUnifiedNativeAdView(unifiedNativeAd, adView);
            frameLayout.setVisibility(View.VISIBLE);
            frameLayout.removeAllViews();
            frameLayout.addView(adView);
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.d("TAG", "onAdFailedToLoad: Native " + adError.getMessage());
                    }

                    @Override
                    public void onAdClicked() {
                        // Log the click event or other custom behavior.

                    }

                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private static void populateUnifiedNativeAdViewSmall(NativeAd nativeAd, NativeAdView adView) {

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        CardView cardview = adView.findViewById(R.id.cardview);
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());


        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
            cardview.setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
            cardview.setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     */
    public static void refreshAdSmall(final Context context, final FrameLayout frameLayout) {
        sharedPrefs = new SharedPrefs(context);
        if (sharedPrefs.getPremium()) {
            frameLayout.setVisibility(View.GONE);
            return;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (120 * scale + 0.5f);
        frameLayout.setMinimumHeight(pixels);

        final NativeAd[] nativeAd = {null};
        AdLoader.Builder builder = new AdLoader.Builder(context, context.getResources().getString(R.string.admob_native_id));

        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (nativeAd[0] != null) {
                    nativeAd[0].destroy();
                }
                nativeAd[0] = unifiedNativeAd;
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                int layout_id = R.layout.ad_unified_small_height;
                NativeAdView adView = (NativeAdView) inflater
                        .inflate(layout_id, null);
                populateUnifiedNativeAdViewSmall(unifiedNativeAd, adView);
                if (frameLayout != null) {
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            }

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Handle the failure by logging, altering the UI, and so on.
            }

            @Override
            public void onAdClicked() {
                // Log the click event or other custom behavior.
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public static boolean checkConnection(Context context) {
        try {
            ConnectivityManager conMgr;
            conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Objects.requireNonNull(conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() == NetworkInfo.State.CONNECTED
                    || Objects.requireNonNull(conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState() == NetworkInfo.State.CONNECTED) {
                return true;
            } else {
                if (Objects.requireNonNull(conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)).getState() != NetworkInfo.State.DISCONNECTED) {
                    Objects.requireNonNull(conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).getState();
                }
            }
            return false;

        } catch (Exception ignored) {
            return true;
        }
    }
}
