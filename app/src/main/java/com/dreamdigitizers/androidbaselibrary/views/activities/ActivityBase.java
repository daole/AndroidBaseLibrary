package com.dreamdigitizers.androidbaselibrary.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.dreamdigitizers.androidbaselibrary.ApplicationBase;
import com.dreamdigitizers.androidbaselibrary.views.fragments.screens.ScreenBase;
import com.dreamdigitizers.androidbaselibrary.views.fragments.FragmentBase;

public abstract class ActivityBase extends AppCompatActivity implements FragmentBase.IStateChecker, ScreenBase.IOnScreenActionsListener {
    protected ScreenBase mCurrentScreen;
    protected boolean mIsRecreated;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        this.setLayout();
        this.retrieveItems();
        this.mapInformationToItems();

        if (pSavedInstanceState != null) {
            this.recoverInstanceState(pSavedInstanceState);
        }

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.handleExtras(extras);
        }

        if (!this.mIsRecreated) {
            this.changeScreen(this.getStartScreen());
        }
    }

    @Override
    public void onBackPressed() {
        if (this.mCurrentScreen != null) {
            boolean isHandled = this.mCurrentScreen.onBackPressed();
            if(isHandled) {
                return;
            }
        }

        this.back();
    }

    @Override
    public boolean isRecreated() {
        return this.mIsRecreated;
    }

    @Override
    public boolean isBeingCovered(FragmentBase pFragment) {
        return false;
    }

    @Override
    public void onSetCurrentScreen(ScreenBase pCurrentScreen) {
        this.mCurrentScreen = pCurrentScreen;
    }

    @Override
    public void onChangeScreen(ScreenBase pScreen) {
        this.changeScreen(pScreen);
    }

    @Override
    public void onReturnActivityResult(int pResultCode, Intent pData) {
        this.setResult(pResultCode, pData);
        this.finish();
    }

    @Override
    public void onChangeLanguage(String pLanguage) {
        ApplicationBase applicationBase = (ApplicationBase) this.getApplication();
        applicationBase.setLocale(pLanguage);
    }

    @Override
    public void onBack() {
        this.back();
    }

    public void changeScreen(ScreenBase pScreen) {
        this.changeScreen(pScreen, true, true);
    }

    public void changeScreen(ScreenBase pScreen, boolean pIsUseAnimation) {
        this.changeScreen(pScreen, true, pIsUseAnimation);
    }

    public void changeScreen(ScreenBase pScreen, boolean pIsUseAnimation, boolean pIsAddToTransaction) {
        try {
            if (pScreen != null) {
                String className = pScreen.getClass().getName();

                if (pScreen.shouldPopBackStack()) {
                    boolean result = this.getSupportFragmentManager().popBackStackImmediate(className, 0);
                    if (result) {
                        return;
                    }
                }

                FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();

                if (pIsUseAnimation) {
                    //fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left, R.animator.slide_in_right, R.animator.slide_out_right);
                }

                fragmentTransaction.replace(this.getScreenContainerViewId(), pScreen);

                if (pIsAddToTransaction) {
                    fragmentTransaction.addToBackStack(className);
                }

                fragmentTransaction.commit();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            this.finish();
        }
    }

    public void back() {
        int backStackEntryCount = this.getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount <= 1) {
            this.finish();
            return;
        }

        boolean result = this.getSupportFragmentManager().popBackStackImmediate();
        if (!result) {
            this.finish();
        }
    }

    protected void recoverInstanceState(Bundle pSavedInstanceState) {
        this.mIsRecreated = true;
    }

    protected void handleExtras(Bundle pExtras) {
    }

    protected abstract void setLayout();
    protected abstract void retrieveItems();
    protected abstract void mapInformationToItems();
    protected abstract ScreenBase getStartScreen();
    protected abstract int getScreenContainerViewId();
}
