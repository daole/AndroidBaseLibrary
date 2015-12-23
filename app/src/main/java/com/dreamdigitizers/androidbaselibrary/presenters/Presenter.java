package com.dreamdigitizers.androidbaselibrary.presenters;

import com.dreamdigitizers.androidbaselibrary.views.IView;

import java.lang.ref.WeakReference;

public abstract class Presenter<V extends IView> {
    private WeakReference<V> mView;

    public Presenter(V pView) {
        this.mView = new WeakReference(pView);
    }

    protected V getView() {
        return this.mView.get();
    }
}
