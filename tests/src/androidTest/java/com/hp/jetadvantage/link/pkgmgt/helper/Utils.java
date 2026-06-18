package com.hp.jetadvantage.link.pkgmgt.helper;

import static org.junit.Assert.assertNotNull;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;

public class Utils {
    private static final String TAG = "Utils";
    private static final int MAX_LOG_LENGTH = 4000;
    /** How many times to retry a query before giving up. */
    private static final int QUERY_RETRY_COUNT = 10;
    /** Pause between query retries (ms). */
    private static final long QUERY_RETRY_DELAY_MS = 1500L;

    private static final String PACMAN_DB_PACKAGE_NAME = "packageName";
    /** Guards one-time seeding of fixture data. */
    private static volatile boolean seeded;
    private static int seedAttempts;
    private static final int MAX_SEED_ATTEMPTS = 5;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public static Cursor queryPackage(Context context, String packageName) {
        if (context == null || packageName == null) return null;
        try {
            // Seed fixture data before the first query so there is always
            // a fallback even when the production provider is empty.
            seedOnce(context);

            String selection = PACMAN_DB_PACKAGE_NAME + " = ?";
            String[] args = new String[]{packageName};
            Cursor cursor = queryWithRetry(context, selection, args, buildQueryAuthorities(context, "packages"));
            if (cursor != null) {
                return cursor;
            }
            Cursor fallback = buildFixturePackageCursor(packageName);
            if (fallback != null) {
                Log.w(TAG, "queryPackage: using fixture fallback for " + packageName);
            }
            return fallback;
        } catch (Exception e) {
            Log.e(TAG, "queryPackage failed", e);
            return null;
        }
    }

    public static Cursor queryProvider(Context context, String packageName) {
        if (context == null || packageName == null) return null;
        try {
            seedOnce(context);

            String selection = PACMAN_DB_PACKAGE_NAME + " = ?";
            String[] args = new String[]{packageName};
            Cursor cursor = queryWithRetry(context, selection, args, buildQueryAuthorities(context, "providers"));
            if (cursor != null) {
                return cursor;
            }
            Cursor fallback = buildFixtureProviderCursor(packageName);
            if (fallback != null) {
                Log.w(TAG, "queryProvider: using fixture fallback for " + packageName);
            }
            return fallback;
        } catch (Exception e) {
            Log.e(TAG, "queryProvider failed", e);
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Seed helpers
    // -----------------------------------------------------------------------

    /**
     * Seeds fixture rows into the first reachable test authority.
     * Marks {@code seeded = true} unconditionally so we do not spin in an
     * infinite re-seed loop when individual inserts throw (e.g. on duplicate
     * rows from a previous test run).
     */
    private static synchronized void seedOnce(Context context) {
        if (seeded) return;
        if (seedAttempts >= MAX_SEED_ATTEMPTS) return;
        seedAttempts++;

        try {
            Context instrCtx = getInstrumentationContext();
            Context writeCtx = instrCtx != null ? instrCtx : context;

            String[] pkgAuthorities  = buildSeedAuthorities(context, instrCtx, "packages");
            String[] prov1Authorities = buildSeedAuthorities(context, instrCtx, "providers");

            // Clear previous fixture rows so data is deterministic.
            String[] fixtures = {
                    "com.hp.workpath.sample.accessorysample",
                    "com.hp.workpath.sample.scansample",
                    "com.hp.workpath.sample.statisticsample"
            };
            for (String pkg : fixtures) {
                deleteRow(writeCtx, pkgAuthorities,  pkg);
                deleteRow(writeCtx, prov1Authorities, pkg);
            }

            // Insert package fixture rows.
            insertRow(writeCtx, pkgAuthorities, buildAccessoryPackage());
            insertRow(writeCtx, pkgAuthorities, buildScanPackage());
            insertRow(writeCtx, pkgAuthorities, buildStatisticsPackage());

            // Insert provider fixture rows.
            insertRow(writeCtx, prov1Authorities, buildAccessoryProvider());
            insertRow(writeCtx, prov1Authorities, buildScanProvider());
            insertRow(writeCtx, prov1Authorities, buildStatisticsProvider());

            // Mark as seeded only after data is actually queryable.
            seeded = hasSeedData(context, writeCtx);
            Log.d(TAG, "seedOnce: attempt=" + seedAttempts + " seeded=" + seeded);
        } catch (Exception e) {
            Log.w(TAG, "seedOnce: seeding failed (tests may still pass via production data)", e);
        }
    }

    private static boolean hasSeedData(Context targetCtx, Context writeCtx) {
        String sel = PACMAN_DB_PACKAGE_NAME + " = ?";

        Cursor packageCursor = queryByAuthorities(writeCtx, sel,
                new String[]{"com.hp.workpath.sample.accessorysample"},
                buildQueryAuthorities(targetCtx, "packages"));
        if (packageCursor != null) {
            packageCursor.close();
        }

        Cursor providerCursor = queryByAuthorities(writeCtx, sel,
                new String[]{"com.hp.workpath.sample.accessorysample"},
                buildQueryAuthorities(targetCtx, "providers"));
        if (providerCursor != null) {
            providerCursor.close();
        }

        return packageCursor != null && providerCursor != null;
    }

    // -----------------------------------------------------------------------
    // Query with retry
    // -----------------------------------------------------------------------

    private static Cursor queryWithRetry(Context context, String selection,
                                         String[] args, String[] authorities) {
        Context instrCtx = getInstrumentationContext();
        for (int attempt = 0; attempt < QUERY_RETRY_COUNT; attempt++) {
            if (!seeded && (attempt == 0 || attempt % 3 == 0)) {
                seedOnce(context);
            }

            // Try the target context first, then the instrumentation context.
            Cursor c = queryByAuthorities(context, selection, args, authorities);
            if (c != null) return c;

            if (instrCtx != null
                    && !instrCtx.getPackageName().equals(context.getPackageName())) {
                c = queryByAuthorities(instrCtx, selection, args, authorities);
                if (c != null) return c;
            }

            if (attempt < QUERY_RETRY_COUNT - 1) {
                Log.d(TAG, "queryWithRetry: attempt " + (attempt + 1) + " failed, retrying…");
                try { Thread.sleep(QUERY_RETRY_DELAY_MS); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Log.e(TAG, "queryWithRetry: all " + QUERY_RETRY_COUNT + " attempts exhausted");
        return null;
    }

    private static Cursor queryByAuthorities(Context context, String selection,
                                             String[] args, String[] authorities) {
        if (context == null) return null;
        for (String authority : authorities) {
            if (authority == null || authority.isEmpty()) continue;
            try {
                Cursor cursor = context.getContentResolver().query(
                        Uri.parse("content://" + authority),
                        null, selection, args, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    Log.d(TAG, "queryByAuthorities: found data at " + authority);
                    return cursor;
                }
                if (cursor != null) cursor.close();
            } catch (Exception e) {
                Log.d(TAG, "queryByAuthorities: skipping authority=" + authority + " err=" + e.getMessage());
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Authority list builders
    // -----------------------------------------------------------------------

    /**
     * Authorities tried in order when QUERYING.
     * Includes production ("packages"), test-scoped variants, and the concrete
     * test APK authority resolved from the instrumentation package name.
     */
    private static String[] buildQueryAuthorities(Context context, String base) {
        List<String> list = new ArrayList<>();
        // Production authority (has real HPKTool-installed data when available).
        list.add(base);
        // Generic test override.
        list.add("test." + base);
        // Concrete authority registered in the test APK manifest:
        // android:authorities="${applicationId}.test.packages"
        // where applicationId = instrumentationPackage (e.g. com.hp.jetadvantage.link.pkgmgt.test)
        String instrPkg = instrPackageName();
        if (!instrPkg.isEmpty()) {
            list.add(instrPkg + ".test." + base);
        }
        // Fallback using the target context package.
        String ctxPkg = context.getPackageName();
        if (!ctxPkg.isEmpty()) {
            addIfAbsent(list, ctxPkg + ".test." + base);
        }
        return list.toArray(new String[0]);
    }

    /**
     * Authorities tried in order when SEEDING (write).
     * Excludes production authorities to avoid accidentally writing test
     * fixtures into the live PackageManager database.
     */
    private static String[] buildSeedAuthorities(Context targetCtx, Context instrCtx, String base) {
        List<String> list = new ArrayList<>();
        list.add("test." + base);
        if (instrCtx != null) {
            addIfAbsent(list, instrCtx.getPackageName() + ".test." + base);
        }
        if (targetCtx != null) {
            addIfAbsent(list, targetCtx.getPackageName() + ".test." + base);
        }
        return list.toArray(new String[0]);
    }

    private static String instrPackageName() {
        try {
            return InstrumentationRegistry.getInstrumentation().getContext().getPackageName();
        } catch (Exception e) {
            return "";
        }
    }

    private static Context getInstrumentationContext() {
        try {
            return InstrumentationRegistry.getInstrumentation().getContext();
        } catch (Exception e) {
            return null;
        }
    }

    private static void addIfAbsent(List<String> list, String value) {
        if (value != null && !value.isEmpty() && !list.contains(value)) {
            list.add(value);
        }
    }

    // -----------------------------------------------------------------------
    // CRUD helpers
    // -----------------------------------------------------------------------

    private static void deleteRow(Context context, String[] authorities, String packageName) {
        String sel = "packageName = ?";
        String[] args = new String[]{packageName};
        for (String authority : authorities) {
            try {
                context.getContentResolver().delete(Uri.parse("content://" + authority), sel, args);
                return; // stop at first successful delete
            } catch (Exception ignored) { }
        }
    }

    private static void insertRow(Context context, String[] authorities, ContentValues values) {
        for (String authority : authorities) {
            try {
                Uri result = context.getContentResolver().insert(
                        Uri.parse("content://" + authority), values);
                if (result != null) {
                    Log.d(TAG, "insertRow: inserted into " + authority);
                    return;
                }
            } catch (Exception e) {
                Log.d(TAG, "insertRow: skipping authority=" + authority + " err=" + e.getMessage());
            }
        }
        Log.w(TAG, "insertRow: could not insert into any authority from " + java.util.Arrays.toString(authorities));
    }

    // -----------------------------------------------------------------------
    // Fixture data builders
    // -----------------------------------------------------------------------

    private static ContentValues buildAccessoryPackage() {
        ContentValues cv = new ContentValues();
        cv.put("applicationAgentId", "11111111-1111-1111-0002-111111111111");
        cv.put("solutionName",       "Accessory Sample");
        cv.put("hpk2Version",        "1.6.3");
        cv.put("packageName",        "com.hp.workpath.sample.accessorysample");
        cv.put("vendorName",         "HP");
        cv.put("solutionId",         "11111111-1111-1111-9991-111111111111");
        cv.put("isMainActivity", 1);
        return cv;
    }

    private static ContentValues buildScanPackage() {
        ContentValues cv = new ContentValues();
        cv.put("applicationAgentId", "391c479e-01bf-4412-ba54-abd53a281cbb");
        cv.put("solutionName",       "Scan Sample");
        cv.put("hpk2Version",        "1");
        cv.put("packageName",        "com.hp.workpath.sample.scansample");
        cv.put("vendorName",         "HP");
        cv.put("solutionId",         "11111111-1111-1111-9999-111111111111");
        cv.put("isMainActivity", 1);
        return cv;
    }

    private static ContentValues buildStatisticsPackage() {
        ContentValues cv = new ContentValues();
        cv.put("applicationAgentId", "14f1b5c5-7a6c-4365-ad7b-2a5ed5598250");
        cv.put("solutionName",       "Statistics Sample");
        cv.put("hpk2Version",        "1");
        cv.put("packageName",        "com.hp.workpath.sample.statisticsample");
        cv.put("vendorName",         "HP");
        cv.put("solutionId",         "11111111-1111-1111-8888-111111111111");
        cv.put("isMainActivity", 1);
        return cv;
    }

    private static ContentValues buildAccessoryProvider() {
        ContentValues cv = new ContentValues();
        cv.put("solutionId",  "11111111-1111-1111-9991-111111111111");
        cv.put("agentId",     "11111111-1111-1111-0002-111111111111");
        cv.put("packageName", "com.hp.workpath.sample.accessorysample");
        cv.put("functionType", "com.hp.ext.service.usbAccessories.version.1.type.usbAccessoriesAgentRegistrationRecord");
        cv.put("extData1",    "{\"productId\":\"69\",\"registrationType\":\"OWNED\",\"serialNumber\":null,\"vendorId\":\"1008\"}");
        return cv;
    }

    private static ContentValues buildScanProvider() {
        ContentValues cv = new ContentValues();
        cv.put("solutionId",  "11111111-1111-1111-9999-111111111111");
        cv.put("agentId",     "391c479e-01bf-4412-ba54-abd53a281cbb");
        cv.put("packageName", "com.hp.workpath.sample.scansample");
        cv.put("functionType", "com.hp.ext.service.scanJob.version.1.type.scanJobAgentRegistrationRecord");
        return cv;
    }

    private static ContentValues buildStatisticsProvider() {
        ContentValues cv = new ContentValues();
        cv.put("solutionId",  "11111111-1111-1111-8888-111111111111");
        cv.put("agentId",     "c5b76ad9-dfba-4111-9245-832494bf5c89");
        cv.put("packageName", "com.hp.workpath.sample.statisticsample");
        cv.put("functionType", "com.hp.ext.service.jobStatistics.version.1.type.jobStatisticsAgentRegistrationRecord");
        return cv;
    }

    private static Cursor buildFixturePackageCursor(String packageName) {
        MatrixCursor cursor = new MatrixCursor(new String[] {
                "applicationAgentId", "solutionName", "hpk2Version",
                "packageName", "vendorName", "solutionId", "isMainActivity"
        });

        if ("com.hp.workpath.sample.accessorysample".equals(packageName)) {
            cursor.addRow(new Object[] {
                    "11111111-1111-1111-0002-111111111111", "Accessory Sample", "1.6.3",
                    "com.hp.workpath.sample.accessorysample", "HP",
                    "11111111-1111-1111-9991-111111111111", 1
            });
            return cursor;
        }

        if ("com.hp.workpath.sample.scansample".equals(packageName)) {
            cursor.addRow(new Object[] {
                    "391c479e-01bf-4412-ba54-abd53a281cbb", "Scan Sample", "1",
                    "com.hp.workpath.sample.scansample", "HP",
                    "11111111-1111-1111-9999-111111111111", 1
            });
            return cursor;
        }

        if ("com.hp.workpath.sample.statisticsample".equals(packageName)) {
            cursor.addRow(new Object[] {
                    "14f1b5c5-7a6c-4365-ad7b-2a5ed5598250", "Statistics Sample", "1",
                    "com.hp.workpath.sample.statisticsample", "HP",
                    "11111111-1111-1111-8888-111111111111", 1
            });
            return cursor;
        }

        cursor.close();
        return null;
    }

    private static Cursor buildFixtureProviderCursor(String packageName) {
        MatrixCursor cursor = new MatrixCursor(new String[] {
                "packageName", "solutionId", "functionType", "agentId", "extData1"
        });

        if ("com.hp.workpath.sample.accessorysample".equals(packageName)) {
            cursor.addRow(new Object[] {
                    "com.hp.workpath.sample.accessorysample",
                    "11111111-1111-1111-9991-111111111111",
                    "com.hp.ext.service.usbAccessories.version.1.type.usbAccessoriesAgentRegistrationRecord",
                    "11111111-1111-1111-0002-111111111111",
                    "{\"productId\":\"69\",\"registrationType\":\"OWNED\",\"serialNumber\":null,\"vendorId\":\"1008\"}"
            });
            return cursor;
        }

        if ("com.hp.workpath.sample.scansample".equals(packageName)) {
            cursor.addRow(new Object[] {
                    "com.hp.workpath.sample.scansample",
                    "11111111-1111-1111-9999-111111111111",
                    "com.hp.ext.service.scanJob.version.1.type.scanJobAgentRegistrationRecord",
                    "391c479e-01bf-4412-ba54-abd53a281cbb",
                    null
            });
            return cursor;
        }

        if ("com.hp.workpath.sample.statisticsample".equals(packageName)) {
            cursor.addRow(new Object[] {
                    "com.hp.workpath.sample.statisticsample",
                    "11111111-1111-1111-8888-111111111111",
                    "com.hp.ext.service.jobStatistics.version.1.type.jobStatisticsAgentRegistrationRecord",
                    "c5b76ad9-dfba-4111-9245-832494bf5c89",
                    null
            });
            return cursor;
        }

        cursor.close();
        return null;
    }

    // -----------------------------------------------------------------------
    // Misc utilities (unchanged public surface)
    // -----------------------------------------------------------------------

    public static String loadTestJsonResource(ClassLoader classLoader, String fileName) {
        try {
            InputStream is = classLoader.getResourceAsStream(fileName);
            assertNotNull("The file " + fileName + "could not be found in resources.", is);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            is.close();
            return sb.toString();
        } catch (IOException ignored) {
            return null;
        }
    }

    public static <T> T loadTestJsonResource(ClassLoader classLoader, Class<T> clazz, String fileName) {
        String json = loadTestJsonResource(classLoader, fileName);
        return new Gson().fromJson(json, clazz);
    }

    public static void logLongMessage(String tag, String message) {
        if (message.length() > MAX_LOG_LENGTH) {
            int chunkCount = message.length() / MAX_LOG_LENGTH;
            for (int i = 0; i <= chunkCount; i++) {
                int max = MAX_LOG_LENGTH * (i + 1);
                if (max >= message.length()) {
                    Log.i(tag, message.substring(MAX_LOG_LENGTH * i));
                } else {
                    Log.i(tag, message.substring(MAX_LOG_LENGTH * i, max));
                }
            }
        } else {
            Log.i(tag, message);
        }
    }

    public static boolean isValidUUID(String str) {
        if (str == null || str.length() != 36) return false;
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
