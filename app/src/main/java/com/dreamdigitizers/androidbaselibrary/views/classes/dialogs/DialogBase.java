package com.dreamdigitizers.androidbaselibrary.views.classes.dialogs;

import android.support.v7.app.AppCompatDialog;

import com.dreamdigitizers.androidbaselibrary.views.classes.activities.ActivityBase;

import java.lang.ref.WeakReference;

public abstract class DialogBase extends AppCompatDialog {
    protected WeakReference<ActivityBase> mActivityBase;

    public DialogBase(ActivityBase pActivityBase) {
        super(pActivityBase);
        this.mActivityBase = new WeakReference<>(pActivityBase);

        int titleStringId = this.getTitle();
        if (titleStringId > 0) {
            this.setTitle(titleStringId);
        } else {
            this.setTitle(this.getTitleString());
        }

        this.setContentView(this.getContentView());
        this.retrieveItems();
        this.mapInformationToItems();
    }

    protected String getTitleString() {
        return null;
    }

    protected abstract int getTitle();
    protected abstract int getContentView();
    protected abstract void retrieveItems();
    protected abstract void mapInformationToItems();
}
