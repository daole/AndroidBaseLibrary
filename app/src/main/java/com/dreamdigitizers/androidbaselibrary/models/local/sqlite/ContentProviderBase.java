package com.dreamdigitizers.androidbaselibrary.models.local.sqlite;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteOpenHelper;

import com.dreamdigitizers.androidbaselibrary.models.local.sqlite.helpers.HelperSQLiteDatabase;

import java.util.ArrayList;

public abstract class ContentProviderBase extends ContentProvider {
    protected HelperSQLiteDatabase mHelperSQLiteDatabase;

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> pOperations) throws OperationApplicationException {
        this.mHelperSQLiteDatabase.open();
        this.mHelperSQLiteDatabase.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(pOperations);
            this.mHelperSQLiteDatabase.commitTransaction();
            return results;
        } catch (OperationApplicationException e){
            this.mHelperSQLiteDatabase.rollbackTransaction();
            throw e;
        } finally {
            this.mHelperSQLiteDatabase.close();
        }
    }

    @Override
    public boolean onCreate() {
        this.mHelperSQLiteDatabase = new HelperSQLiteDatabase(this.createSQLiteOpenHelper());
        return true;
    }

    protected abstract SQLiteOpenHelper createSQLiteOpenHelper();
}
