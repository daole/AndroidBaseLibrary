package com.dreamdigitizers.androidbaselibrary.views;

import android.content.Context;

import com.dreamdigitizers.androidbaselibrary.utils.UtilsDialog;

public interface IView {
    Context getViewContext();
    void showMessage(int pStringResourceId);
    void showConfirmation(int pStringResourceId, UtilsDialog.IOnDialogButtonClickListener pListener);
    void showError(int pStringResourceId);
}
