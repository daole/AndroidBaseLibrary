package com.dreamdigitizers.androidbaselibrary.presenters.classes;

import com.dreamdigitizers.androidbaselibrary.presenters.interfaces.IPresenter;
import com.dreamdigitizers.androidbaselibrary.views.interfaces.IView;

import java.lang.ref.WeakReference;

public abstract class Presenter<V extends IView> implements IPresenter {
    private WeakReference<V> mView;

    public Presenter(V pView) {
        this.mView = new WeakReference(pView);
    }

    protected V getView() {
        return this.mView.get();
    }
}
