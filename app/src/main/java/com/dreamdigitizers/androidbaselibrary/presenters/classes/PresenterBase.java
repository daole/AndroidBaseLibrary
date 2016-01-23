package com.dreamdigitizers.androidbaselibrary.presenters.classes;

import com.dreamdigitizers.androidbaselibrary.presenters.interfaces.IPresenterBase;
import com.dreamdigitizers.androidbaselibrary.views.interfaces.IViewBase;

import java.lang.ref.WeakReference;

public abstract class PresenterBase<V extends IViewBase> implements IPresenterBase {
    private WeakReference<V> mView;

    @Override
    public void dispose() {
    }

    public PresenterBase(V pView) {
        this.mView = new WeakReference(pView);
    }

    protected V getView() {
        return this.mView.get();
    }
}
