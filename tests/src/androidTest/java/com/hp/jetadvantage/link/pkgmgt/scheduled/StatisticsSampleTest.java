package com.hp.jetadvantage.link.pkgmgt.scheduled;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.hp.jetadvantage.link.pkgmgt.helper.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StatisticsSampleTest {

    private static final String TAG = "StatisticsSampleTest";

    private static class Info {
        static final String packageName = "com.hp.workpath.sample.statisticsample";
        static final String solutionId = "11111111-1111-1111-8888-111111111111";
        static final String solutionName = "Statistics Sample";
        static final String applicationAgentId = "c5b76ad9-dfba-4111-9245-832494bf5c89";
        static final String hpk2Version = "1";
        static final String vendorName = "HP";
        static final String functionType = "com.hp.ext.service.jobStatistics.version.1.type.jobStatisticsAgentRegistrationRecord";
    }

    @Test
    public void verifySamplePackage() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        Log.i(TAG, "Starting verification for package: " + Info.packageName);
        
        Cursor cursor = Utils.queryPackage(context, Info.packageName);
        assertNotNull("Failed to query package: " + Info.packageName, cursor);
        
        try {
            if (cursor.moveToFirst()) {
                // Verify common columns
                verifyColumnValue(cursor, "packageName", Info.packageName);
                
                // Specific verification for Scan Sample using Info constants
                verifyColumnValue(cursor, "applicationAgentId", Info.applicationAgentId);
                verifyColumnValue(cursor, "solutionName", Info.solutionName);
                verifyColumnValue(cursor, "hpk2Version", Info.hpk2Version);
                verifyColumnValue(cursor, "vendorName", Info.vendorName);
                verifyColumnValue(cursor, "solutionId", Info.solutionId);
                
                Log.i(TAG, "Verification successful for " + Info.packageName);
            } else {
                fail("Package not found in ContentProvider: " + Info.packageName);
            }
        } finally {
            cursor.close();
        }
    }

    @Test
    public void verifySampleProvider() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Log.i(TAG, "Starting provider verification for package: " + Info.packageName);

        Cursor cursor = Utils.queryProvider(context, Info.packageName);
        assertNotNull("Failed to query provider for package: " + Info.packageName, cursor);

        try {
            boolean found = false;
            
            while (cursor.moveToNext()) {
                int functionTypeIndex = cursor.getColumnIndex("functionType");
                if (functionTypeIndex >= 0) {
                    String functionType = cursor.getString(functionTypeIndex);
                    if (Info.functionType.equals(functionType)) {
                        found = true;
                        Log.i(TAG, "Found matching functionType: " + functionType);
                        
                        // Verify other columns for this provider record
                        verifyColumnValue(cursor, "packageName", Info.packageName);
                        verifyColumnValue(cursor, "solutionId", Info.solutionId);
                        verifyColumnValue(cursor, "agentId", Info.applicationAgentId);
                        break;
                    }
                }
            }
            
            assertTrue("Target functionType not found: " + Info.functionType, found);
            Log.i(TAG, "Provider verification successful for " + Info.packageName);
            
        } finally {
            cursor.close();
        }
    }

    private void verifyColumnValue(Cursor cursor, String columnName, String expectedValue) {
        int index = cursor.getColumnIndex(columnName);
        assertTrue("Column " + columnName + " not found", index >= 0);
        String actualValue = cursor.getString(index);
        assertEquals("Value mismatch for column " + columnName, expectedValue, actualValue);
        Log.d(TAG, "Verified " + columnName + ": " + actualValue);
    }
}
