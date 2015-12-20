package com.dreamdigitizers.androidbaselibrary.views.abstracts;

import android.content.Context;

import com.dreamdigitizers.androidbaselibrary.utils.DialogUtils;

public interface IView {
    Context getViewContext();
    void showMessage(int pStringResourceId);
    void showConfirmation(int pStringResourceId, DialogUtils.IOnDialogButtonClickListener pListener);
    void showError(int pStringResourceId);
}
