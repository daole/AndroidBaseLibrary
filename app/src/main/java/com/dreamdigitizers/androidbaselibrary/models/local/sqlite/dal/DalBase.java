package com.dreamdigitizers.androidbaselibrary.models.local.sqlite.dal;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import com.dreamdigitizers.androidbaselibrary.models.local.sqlite.helpers.HelperSQLiteDatabase;

public abstract class DalBase {
    protected HelperSQLiteDatabase mHelperSQLiteDatabase;
    protected SQLiteQueryBuilder mSQLiteQueryBuilder;

    public DalBase(HelperSQLiteDatabase pHelperSQLiteDatabase) {
        this.mHelperSQLiteDatabase = pHelperSQLiteDatabase;
        this.mSQLiteQueryBuilder =  new SQLiteQueryBuilder();
    }

    public long insert(ContentValues pContentValues) {
        return this.insert(pContentValues, false);
    }

    public long insert(ContentValues pContentValues, boolean pIsCloseOnEnd) {
        long newId = this.mHelperSQLiteDatabase.insert(this.getTableName(), pContentValues, false, pIsCloseOnEnd);
        return newId;
    }

    public int update(ContentValues pContentValues, String pWhereClause, String[] pWhereArgs) {
        return this.update(pContentValues, pWhereClause, pWhereArgs, false);
    }

    public int update(ContentValues pContentValues, String pWhereClause, String[] pWhereArgs, boolean pIsCloseOnEnd) {
        int affectedRows = this.mHelperSQLiteDatabase.update(this.getTableName(), pContentValues, pWhereClause, pWhereArgs, false, pIsCloseOnEnd);
        return affectedRows;
    }

    public int delete(String pWhereClause, String[] pWhereArgs) {
        return this.delete(pWhereClause, pWhereArgs, false);
    }

    public int delete(String pWhereClause, String[] pWhereArgs, boolean pIsCloseOnEnd) {
        int affectedRows = this.mHelperSQLiteDatabase.delete(this.getTableName(), pWhereClause, pWhereArgs, false, pIsCloseOnEnd);
        return affectedRows;
    }

    public Cursor select(String[] pProjection, String pWhereClause, String[] pWhereArgs, String pGroupBy, String pHaving, String pSortOrder) {
        return this.select(pProjection, pWhereClause, pWhereArgs, pGroupBy, pHaving, pSortOrder, false);
    }

    public Cursor select(String[] pProjection, String pWhereClause, String[] pWhereArgs, String pGroupBy, String pHaving, String pSortOrder, boolean pIsCloseOnEnd) {
        this.mSQLiteQueryBuilder.setTables(this.getTableName());
        Cursor cursor = this.mHelperSQLiteDatabase.select(this.mSQLiteQueryBuilder, pProjection, pWhereClause, pWhereArgs, pGroupBy, pHaving, pSortOrder, null, false, pIsCloseOnEnd);
        return  cursor;
    }

    public abstract String getTableName();
    public abstract boolean checkColumns(String[] pProjection);
}
