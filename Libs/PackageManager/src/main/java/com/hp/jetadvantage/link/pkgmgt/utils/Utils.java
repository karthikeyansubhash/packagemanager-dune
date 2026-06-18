package com.hp.jetadvantage.link.pkgmgt.utils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.ext.types.solutionManager.SolutionManifest;
import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.exception.OutOfStorageException;
import com.hp.jetadvantage.link.pkgmgt.helper.SecurityHelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static final String TAG = Constants.TAG + "Utils";

    public static String readFile(File file) {
        if (file != null) {
            Log.d(TAG, "readFile " + file.getName());
            FileInputStream fis = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try {
                fis = new FileInputStream(file);
                copyStream(fis, bos, false);
                String data = bos.toString("UTF-8");
                Log.d(TAG, "data: " + data.length());

                return data;
            } catch (Exception e) {
                Log.w(TAG, "Failed to readFile: " + file.getName());
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to close file", e);
                    }
                }
            }
        }
        return "";
    }

    public static void copyStream(final InputStream input, final OutputStream output, Boolean isSizeLimit) throws IOException {
        byte[] buffer = new byte[512 * 1024]; // or other buffer size
        int read;

        int total = 0;
        try {
            if (isSizeLimit) {
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    total += read;
                }
            } else {
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    total += read;
                }
            }
        } catch (IOException e) {
            // check if this is "no space left on device" error
            OutOfStorageException.accept(e);
            throw e;
        } finally {
            input.close();
        }

        Log.i(TAG, "copyStream: " + total + " bytes copied");
    }

    public static void sendPackageBroadcast(final Context context, final String action, final Bundle extras, final String permission) {
        sendBroadcastToSystem(context, action, extras, permission, SecurityHelper.SYSTEM_SDK_PACKAGE_NAME);
        sendBroadcastToSystem(context, action, extras, permission, SecurityHelper.SYSTEM_SVC_PACKAGE_NAME);
        sendBroadcastToSystem(context, action, extras, permission, SecurityHelper.SYSTEM_SERVICE_PACKAGE_NAME);
        sendBroadcastToSystem(context, action, extras, permission, SecurityHelper.SYSTEM_LOG_PACKAGE_NAME);
    }

    private static void sendBroadcastToSystem(Context context, String action, Bundle extras, String permission, String packageName) {
        Log.d(TAG, "sendBroadcastToSystem package: " + packageName);
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        intent.putExtras(extras);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent, permission);
    }

    public static SolutionManifest getSolutionManifest(String solutionData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(solutionData, SolutionManifest.class);
    }

    public static SolutionManifest getSolutionManifest(File solutionFile) throws IOException {
        String solutionData = Utils.readFile(solutionFile);
        return getSolutionManifest(solutionData);
    }

    public static boolean isDataPartitionHasEnoughSpaceForFileSize(final Context context, long fileSize) {
        File dataPartitionFolder = context.getApplicationContext().getFilesDir();
        long approxFreeSizeAfterInstall = dataPartitionFolder.getFreeSpace() - (long) (Constants.APK_INSTALL_SIZE_MULTIPLIER * fileSize);
        long lowSpaceThresholdSize = (long) (Constants.LOW_SPACE_WARNING_THRESHOLD * dataPartitionFolder.getTotalSpace());

        Log.d(TAG, "Space after install: " + approxFreeSizeAfterInstall + " (limit: " + lowSpaceThresholdSize + ")");
        return (approxFreeSizeAfterInstall > lowSpaceThresholdSize);
    }

    public static String getApplicationHash(final String path) {
        final String encrypt = "SHA-256";

        if (TextUtils.isEmpty(path) || !(new File(path)).exists()) {
            Log.e(TAG, "package path is null or invalid path");
            return null;
        }

        String hashString = null;
        BufferedInputStream bis = null;

        try {
            MessageDigest digest = MessageDigest.getInstance(encrypt);
            byte[] buffer = new byte[512 * 1024];
            int count;
            bis = new BufferedInputStream(new FileInputStream(path));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();

            byte[] hash = digest.digest();
            hashString = Base64.encodeToString(hash, Base64.NO_WRAP).trim();
        } catch (final NoSuchAlgorithmException nse) {
            Log.e(TAG, "algorithm is not supported", nse);
        } catch (final IOException ioe) {
            Log.e(TAG, "package read error", ioe);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                }
            }
        }

        return hashString;
    }

    public static void clearCache(Context context) {
        File cacheDirectory = context.getCacheDir();
        deleteFilesInDir(cacheDirectory);
    }

    private static void deleteFilesInDir(File directory) {
        if (directory != null && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    public static Intent createLaunchIntent(ComponentName componentName) {
        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.setComponent(componentName);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        return launchIntent;
    }
}
