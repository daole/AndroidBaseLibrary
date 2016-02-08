package com.dreamdigitizers.androidbaselibrary.views.classes.activities;

import android.app.Activity;
import android.os.Bundle;

public class ActivityDummy extends Activity {
    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        this.finish();
    }
}
