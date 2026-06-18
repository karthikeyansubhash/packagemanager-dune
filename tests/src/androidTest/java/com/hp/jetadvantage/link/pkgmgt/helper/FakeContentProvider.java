package com.hp.jetadvantage.link.pkgmgt.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FakeContentProvider extends ContentProvider {
    Cursor cursor = null;
    List<ContentValues> insertValueList = new ArrayList<>();
    List<ContentValues> updateValueList = new ArrayList<>();

    public FakeContentProvider(Cursor mockCursor) {
        this.cursor = mockCursor;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        insertValueList.add(values);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        updateValueList.add(values);
        return 0;
    }

    public List<ContentValues> getInsertValues() {
        return insertValueList;
    }

    public List<ContentValues> getUpdateValues() {
        return updateValueList;
    }

}
