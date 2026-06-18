package com.hp.jetadvantage.link.pkgmgt.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manager for handling Event Receiver registration.
 * Parses AndroidManifest.xml to extract broadcast receiver declarations for recognized Workpath event actions.
 * Persists extracted event receivers in the eventreceivers database table.
 */
public class EventReceiverManager {
    private static final String TAG = Constants.TAG + "EventReceiverManager";
    private static final String LOG_PREFIX = "[DUNE-314377]";
    private static final String EVENT_TABLE_PREFIX = "[EVENT-TABLE]";

    private static final String RECEIVE_SIGN_IN_OUT_EVENT = "com.hp.workpath.permission.RECEIVE_SIGN_IN_OUT_EVENT";
    private static final String RECEIVE_CONFIG_CHANGED_EVENT = "com.hp.workpath.permission.RECEIVE_CONFIG_CHANGED_EVENT";
    private static final String RECEIVE_JOB_COMPLETED_EVENT = "com.hp.workpath.permission.RECEIVE_JOB_COMPLETED_EVENT";
    private static final String RECEIVE_SLEEP_WAKEUP_EVENT = "com.hp.workpath.permission.RECEIVE_SLEEP_WAKEUP_EVENT";

    // Keep action and expected permission in a single declaration for simpler maintenance.
    private static final class EventRule {
        final String action;
        final String permission;

        EventRule(String action, String permission) {
            this.action = action;
            this.permission = permission;
        }
    }

    private static final List<EventRule> EVENT_RULES = Arrays.asList(
            new EventRule(PackageContract.WorkpathEventAction.SIGN_IN, RECEIVE_SIGN_IN_OUT_EVENT),
            new EventRule(PackageContract.WorkpathEventAction.SIGN_OUT, RECEIVE_SIGN_IN_OUT_EVENT),
            new EventRule(PackageContract.WorkpathEventAction.JOB_COMPLETED, RECEIVE_JOB_COMPLETED_EVENT),
            new EventRule(PackageContract.WorkpathEventAction.CONFIG_CHANGED, RECEIVE_CONFIG_CHANGED_EVENT),
            new EventRule(PackageContract.WorkpathEventAction.WAKE_UP, RECEIVE_SLEEP_WAKEUP_EVENT),
            new EventRule(PackageContract.WorkpathEventAction.SLEEP, RECEIVE_SLEEP_WAKEUP_EVENT)
    );

    private final Context context;

    public EventReceiverManager(Context context) {
        this.context = context;
    }

    /**
     * Registers event receivers for an application by parsing its AndroidManifest.xml
     *
     * @param packageName The package name of the installed application
     * @return true if registration was successful, false otherwise
     */
    public boolean registerEventReceivers(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.w(TAG, "Invalid package name for event receiver registration");
            return false;
        }

        try {
            List<String> eventActions = extractEventActions(packageName);
            if (!eventActions.isEmpty()) {
                int insertedCount = 0;
                for (String eventAction : eventActions) {
                    if (insertEventReceiver(eventAction, packageName)) {
                        insertedCount++;
                    }
                }
                Log.i(TAG, LOG_PREFIX + " Install flow completed, table rows created=" + insertedCount
                        + " package=" + packageName + " actions=" + eventActions);
                return true;
            } else {
                Log.i(TAG, LOG_PREFIX + " Install flow completed, table rows created=0 package=" + packageName + " actions=[]");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, LOG_PREFIX + " Install flow failed for package=" + packageName + ", error=" + e.getMessage());
            return false;
        }
    }

    /**
     * Removes all event receiver registrations for an application
     *
     * @param packageName The package name of the application being uninstalled
     * @return true if cleanup was successful, false otherwise
     */
    public boolean unregisterEventReceivers(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.w(TAG, "Invalid package name for event receiver unregistration");
            return false;
        }

        try {
            String where = PackageContract.EventReceiverEntry.PACKAGE_NAME + " = ?";
            String[] whereArgs = {packageName};
            int deletedRows = context.getContentResolver().delete(
                    PackageContract.EVENTRECEIVERS_CONTENT_URI,
                    where,
                    whereArgs
            );
            Log.i(TAG, LOG_PREFIX + " Uninstall flow completed, table rows removed=" + deletedRows
                    + " package=" + packageName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, LOG_PREFIX + " Uninstall flow failed for package=" + packageName + ", error=" + e.getMessage());
            return false;
        }
    }

    /**
     * Removes all event receiver rows from the table.
     *
     * @return true if operation succeeded.
     */
    public boolean clearAllEventReceivers() {
        try {
            int deletedRows = context.getContentResolver().delete(
                    PackageContract.EVENTRECEIVERS_CONTENT_URI,
                    null,
                    null
            );
            Log.i(TAG, EVENT_TABLE_PREFIX + " Deleted all rows from eventreceivers table: " + deletedRows);
            return true;
        } catch (Exception e) {
            Log.e(TAG, LOG_PREFIX + " Failed to clear all event receivers: " + e.getMessage());
            return false;
        }
    }

    /**
     * Rebuilds the eventreceivers table by scanning installed packages.
     *
     * @return Number of registered rows in eventreceivers after scan.
     */
    public int scanInstalledPackagesAndRebuildTable() {
        int totalRegistered = 0;
        try {
            // Start with a clean table so boot scan reflects current device state.
            context.getContentResolver().delete(PackageContract.EVENTRECEIVERS_CONTENT_URI, null, null);

            PackageManager pm = context.getPackageManager();
            List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_RECEIVERS);

            for (PackageInfo info : installedPackages) {
                String packageName = info.packageName;
                if (packageName == null || packageName.isEmpty()) {
                    continue;
                }

                List<String> eventActions = extractEventActions(packageName);
                if (eventActions.isEmpty()) {
                    continue;
                }

                for (String action : eventActions) {
                    if (insertEventReceiver(action, packageName)) {
                        totalRegistered++;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, EVENT_TABLE_PREFIX + " Boot scan failed: " + e.getMessage());
        }

        return totalRegistered;
    }

    // Resolve receiver intent-filters through PackageManager (manifest-derived) and keep unique actions.
    private List<String> extractEventActions(String packageName) {
        List<String> eventActions = new ArrayList<>();

        try {
            PackageManager pm = context.getPackageManager();
            for (EventRule rule : EVENT_RULES) {
                Intent intent = new Intent(rule.action);
                intent.setPackage(packageName);
                List<ResolveInfo> receivers = pm.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER);
                if (receivers.isEmpty()) {
                    continue;
                }
                for (ResolveInfo receiverInfo : receivers) {
                    IntentFilter filter = receiverInfo.filter;
                    String receiverPermission = receiverInfo.activityInfo != null
                            ? receiverInfo.activityInfo.permission : null;
                    if (filter != null && permissionMatches(rule.permission, receiverPermission)) {
                        if (!eventActions.contains(rule.action)) {
                            eventActions.add(rule.action);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, LOG_PREFIX + " Receiver resolution failed package=" + packageName + ", error=" + e.getMessage());
        }

        return eventActions;
    }

    private boolean permissionMatches(String expected, String actual) {
        return expected == null || expected.equals(actual);
    }

    /**
     * Inserts an event receiver into the database
     *
     * @param eventType The event action type
     * @param packageName The package name of the receiver
     */
    private boolean insertEventReceiver(String eventType, String packageName) {
        try {
            ContentValues values = new ContentValues();
            values.put(PackageContract.EventReceiverEntry.EVENT_TYPE, eventType);
            values.put(PackageContract.EventReceiverEntry.PACKAGE_NAME, packageName);

            Uri inserted = context.getContentResolver().insert(
                    PackageContract.EVENTRECEIVERS_CONTENT_URI,
                    values
            );
            return inserted != null;
        } catch (Exception e) {
            Log.e(TAG, LOG_PREFIX + " Row insert failed eventType=" + eventType + " package=" + packageName
                    + " error=" + e.getMessage());
            return false;
        }
    }
}
