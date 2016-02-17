package com.dreamdigitizers.androidbaselibrary.views.interfaces;

import android.content.Context;
import android.view.View;

import com.dreamdigitizers.androidbaselibrary.utilities.UtilsDialog;

public interface IViewBase {
    Context getViewContext();
    Object getViewSystemService(String pName);
    void showMessage(int pMessageResourceId);
    void showMessage(String pMessage);
    void showMessage(int pMessageResourceId, int pActionResourceId, View.OnClickListener pActionListener);
    void showMessage(String pMessage, String pAction, View.OnClickListener pActionListener);
    void showConfirmation(int pMessageResourceId, UtilsDialog.IOnDialogButtonClickListener pListener);
    void showConfirmation(String pMessage, UtilsDialog.IOnDialogButtonClickListener pListener);
    void showError(int pMessageResourceId);
    void showError(String pMessage);
    void showRetryableError(int pMessageResourceId, boolean pIsEndActivity, UtilsDialog.IRetryAction pRetryAction);
    void showRetryableError(String pMessage, boolean pIsEndActivity, UtilsDialog.IRetryAction pRetryAction);
    void showNetworkProgress();
    void hideNetworkProgress();
}
