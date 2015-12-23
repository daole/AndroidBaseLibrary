package com.dreamdigitizers.androidbaselibrary.presenters;

import com.dreamdigitizers.androidbaselibrary.views.IView;

import java.lang.reflect.InvocationTargetException;

public class PresenterFactory {
    public static <P extends Presenter, V extends IView> P createPresenter(
            Class<P> pPresenterClass,
            Class<V> pViewClass,
            V pView)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return pPresenterClass.getConstructor(pViewClass).newInstance(pView);
    }
}
