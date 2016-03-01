package com.dreamdigitizers.androidbaselibrary.views.classes.support;

import android.os.Handler;
import android.support.v7.appcompat.BuildConfig;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AdListener extends com.google.android.gms.ads.AdListener {
    private static final int REFRESH_TIME = 10000;

    private static final List<String> mDeviceIds = new ArrayList<>();
    static {
        AdListener.mDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR);
        AdListener.mDeviceIds.add("BA60322C3D2E065E9975FB53C2D78F32");
    }

    private WeakReference<AdView> mAdView;
    private Handler mRefreshHandler;
    private Runnable mRefreshRunnable;
    private boolean mIsFirstAdReceived;

    public AdListener(AdView pAdView) {
        this.mAdView = new WeakReference<>(pAdView);
        this.mRefreshHandler = new Handler();
        this.mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                AdListener.this.requestAd();
            }
        };
        this.requestAd();
    }

    @Override
    public void onAdLoaded() {
        this.mIsFirstAdReceived = true;
        AdView adView = this.mAdView.get();
        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAdFailedToLoad(int pErrorCode) {
        if (!this.mIsFirstAdReceived) {
            AdView adView = this.mAdView.get();
            if (adView != null) {
                adView.setVisibility(View.GONE);
                this.mRefreshHandler.postDelayed(this.mRefreshRunnable, AdListener.REFRESH_TIME);
            }
        }
    }

    private void requestAd() {
        AdRequest adRequest;
        if (BuildConfig.DEBUG) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            for (String deviceId : AdListener.mDeviceIds) {
                adRequestBuilder.addTestDevice(deviceId);
            }
            adRequest = adRequestBuilder.build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }
        AdView adView = this.mAdView.get();
        if(adView != null) {
            adView.loadAd(adRequest);
        }
    }
}
