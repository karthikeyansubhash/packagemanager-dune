package com.hp.jetadvantage.link.pkgmgt.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;

public class AttestationChangeBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = Constants.TAG + "ATT";

    private final String ACTION_ATTESTATION = "com.hp.jetadvantage.link.intent.action.ATTESTATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received Attestation broadcast: " + intent.getAction());

        try {
            if (context != null && intent != null && intent.getAction() != null) {
                if (ACTION_ATTESTATION.equals(intent.getAction()) && (intent.getExtras() != null)) {
                    SecurityHelper.checkCallingPackageForLink(context);

                    String uuid = intent.getExtras().getString("UUID");
                    String data = intent.getExtras().getString("EXTRA_DATA");
                    String user = intent.getExtras().getString("EXTRA_USER");
                    String ldbKey = intent.getExtras().getString("EXTRA_LDBKEY");

                    if (data == null || data.length() < 1) {
                        Log.e(TAG, "Client information is empty");
                        return;
                    }

                    try {
                        Gson gson = new Gson();
                        if (data.startsWith("[")) {
                            gson.fromJson(data, JsonArray.class);
                        } else {
                            gson.fromJson(data, JsonObject.class);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Invalid client information", e);
                        return;
                    }

                    Uri packageUri = Uri.withAppendedPath(PackageContract.PACKAGES_ATTESTATION_CONTENT_URI, uuid);
                    Cursor cursor = context.getContentResolver().query(
                            packageUri, null, null, null, null);

                    long currentTime = System.currentTimeMillis();
                    ContentValues pcv = new ContentValues();
                    pcv.put(PackageContract.PackageAttestationEntry.SOLUTION_ID, uuid);
                    pcv.put(PackageContract.PackageAttestationEntry.AUTH, data);
                    pcv.put(PackageContract.PackageAttestationEntry.USER, user);
                    pcv.put(PackageContract.PackageAttestationEntry.KEY, ldbKey);
                    pcv.put(PackageContract.PackageAttestationEntry.MODIFY_DATE, currentTime);

                    String querySelection = PackageContract.PackageAttestationEntry.SOLUTION_ID + " = ?";
                    String[] querySelectionArgs = new String[]{uuid};
                    try {
                        if (cursor != null && cursor.moveToNext()) {
                            //String uuid = "UUID: " + cursor.getString(cursor.getColumnIndex(PackageContract.PackageAttestationEntry.SOLUTION_ID));
                            int providerUpdated = context.getContentResolver().update(
                                    packageUri, pcv, querySelection, querySelectionArgs);
                            Log.i(TAG, "provider is updated " + providerUpdated);
                        } else {
                            pcv.put(PackageContract.PackageAttestationEntry.INSTALL_DATE, currentTime);
                            Uri inserted = context.getContentResolver().insert(
                                    PackageContract.PACKAGES_ATTESTATION_CONTENT_URI, pcv);
                            Log.i(TAG, "provider is inserted " + inserted);
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } else {
                    Log.w(TAG, "No extras");
                }
            } else {
                Log.e(TAG, "Invalid access");
            }
        } catch (Exception e) {
            Log.w(TAG, " Received event 3, but no permission " + e.getMessage());
        }
    }
}
