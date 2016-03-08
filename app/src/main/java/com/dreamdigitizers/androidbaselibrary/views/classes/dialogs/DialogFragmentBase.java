package com.dreamdigitizers.androidbaselibrary.views.classes.dialogs;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class DialogFragmentBase extends AppCompatDialogFragment {
    private View mView;

    @Override
    public void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        Bundle arguments = this.getArguments();
        if (arguments != null) {
            this.handleArguments(arguments);
        }

        if (pSavedInstanceState != null) {
            this.recoverInstanceState(pSavedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        if (this.mView == null || !this.isCacheView()) {
            this.mView = this.loadView(pInflater, pContainer);
            this.retrieveItems(this.mView);
            this.mapInformationToItems(this.mView);
            this.setHasOptionsMenu(true);
        }
        return this.mView;
    }

    protected boolean isCacheView() {
        return true;
    }

    protected void handleArguments(Bundle pArguments) {
    }

    protected void recoverInstanceState(Bundle pSavedInstanceState) {
    }

    protected abstract View loadView(LayoutInflater pInflater, ViewGroup pContainer);
    protected abstract void retrieveItems(View pView);
    protected abstract void mapInformationToItems(View pView);
}
