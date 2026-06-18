package com.hp.jetadvantage.link.pkgmgt.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hp.jetadvantage.link.pkgmgt.PackageContract;

public class PermissionActivity extends Activity {
    private static final String TAG = "PacM/PERM";
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        grantPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void grantPermission(Context context, String... permission) {
        Log.i(TAG, "Called grant");
        try {
            final boolean hasWritePermission = (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_SETTINGS) == android.content.pm.PackageManager.PERMISSION_GRANTED);

            if (!hasWritePermission) {
                Log.i(TAG, "No per for storage");
            }

            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.INSTALL_PACKAGES,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.DELETE_PACKAGES,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    PackageContract.Permission.GRANT_RUNTIME_PERMISSIONS,
                    Manifest.permission.SYSTEM_ALERT_WINDOW
            };

            ActivityCompat.requestPermissions(this, permissions,
                    PERMISSION_REQUEST_CODE);

            final boolean hasWritePermissionAfter = (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED);
            if (hasWritePermissionAfter) {
                Log.i(TAG, "Permission is granted.");
            } else {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "(Retry) Permission is granted.");
                } else {
                    Log.i(TAG, "Permission is not granted.");
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Error in perm:" + e.getMessage());
        } finally {
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}