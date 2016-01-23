package com.dreamdigitizers.androidbaselibrary.views.interfaces;

import android.content.Context;

import com.dreamdigitizers.androidbaselibrary.utilities.UtilsDialog;

public interface IViewBase {
    Context getViewContext();
    Object getViewSystemService(String pName);
    void showMessage(final int pStringResourceId);
    void showConfirmation(final int pStringResourceId, final UtilsDialog.IOnDialogButtonClickListener pListener);
    void showError(final int pStringResourceId);
    public void showRetryableError(final int pStringResourceId, final boolean pIsEndActivity, final UtilsDialog.IRetryAction pRetryAction);
    void showNetworkProgress();
    void hideNetworkProgress();
}
