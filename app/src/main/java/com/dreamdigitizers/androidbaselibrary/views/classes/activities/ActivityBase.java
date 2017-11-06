package com.dreamdigitizers.androidbaselibrary.views.classes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dreamdigitizers.androidbaselibrary.ApplicationBase;
import com.dreamdigitizers.androidbaselibrary.R;
import com.dreamdigitizers.androidbaselibrary.utilities.UtilsString;
import com.dreamdigitizers.androidbaselibrary.views.classes.fragments.FragmentBase;
import com.dreamdigitizers.androidbaselibrary.views.classes.fragments.screens.ScreenBase;

public abstract class ActivityBase extends AppCompatActivity implements FragmentBase.IStateChecker, ScreenBase.IOnScreenActionsListener {
    protected ScreenBase mCurrentScreen;
    protected boolean mIsRecreated;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        Intent intent = this.getIntent();
        if (intent != null) {
            this.handleIntent(intent);
        }

        if (pSavedInstanceState != null) {
            this.recoverInstanceState(pSavedInstanceState);
        }

        this.setLayout();
        this.retrieveItems();
        this.mapInformationToItems();

        if (!this.mIsRecreated) {
            this.changeScreen(this.getStartScreen());
        }
    }

    @Override
    public void onBackPressed() {
        if (this.handleNeededBackProcess()) {
            return;
        }

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
    public void onShowSnackbar(int pLength, int pMessageResourceIdId, int pActionResourceId, View.OnClickListener pActionListener) {
        this.onShowSnackbar(pLength, this.getString(pMessageResourceIdId), this.getString(pActionResourceId), pActionListener);
    }

    @Override
    public void onShowSnackbar(int pLength, String pMessage, String pAction, View.OnClickListener pActionListener) {
        CoordinatorLayout coordinatorLayout = this.getCoordinatorLayout();
        if (coordinatorLayout != null) {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, pMessage, pLength);
            if (!UtilsString.isEmpty(pAction) && pActionListener != null) {
                snackbar.setAction(pAction, pActionListener);
            }
            snackbar.show();
        }
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

    public void changeActivityWithoutAnimation(Intent pIntent) {
        this.changeActivity(pIntent, false, 0, 0);
    }

    public void changeActivityWithoutAnimation(Intent pIntent, boolean pIsFinish) {
        this.changeActivity(pIntent, pIsFinish, 0, 0);
    }

    public void changeActivity(Intent pIntent) {
        this.changeActivity(pIntent, false, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    public void changeActivity(Intent pIntent, boolean pIsFinish) {
        this.changeActivity(pIntent, pIsFinish, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    public void changeActivity(Intent pIntent, boolean pIsFinish, int pEnterAnimationResourceId, int pExitAnimationResourceId) {
        this.startActivity(pIntent);
        if(pEnterAnimationResourceId != 0 && pExitAnimationResourceId != 0) {
            this.overridePendingTransition(pEnterAnimationResourceId, pExitAnimationResourceId);
        }
        if (pIsFinish) {
            this.finish();
        }
    }

    public void changeScreen(ScreenBase pScreen) {
        this.changeScreen(pScreen, true, true);
    }

    public void changeScreen(ScreenBase pScreen, boolean pIsAddToTransaction) {
        this.changeScreen(pScreen, true, pIsAddToTransaction);
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
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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

    protected boolean handleNeededBackProcess() {
        return false;
    }

    protected void recoverInstanceState(Bundle pSavedInstanceState) {
        this.mIsRecreated = true;
    }

    protected void handleIntent(Intent pIntent) {
    }

    protected CoordinatorLayout getCoordinatorLayout() {
        return null;
    }

    protected abstract void setLayout();
    protected abstract void retrieveItems();
    protected abstract void mapInformationToItems();
    protected abstract ScreenBase getStartScreen();
    protected abstract int getScreenContainerViewId();
}
