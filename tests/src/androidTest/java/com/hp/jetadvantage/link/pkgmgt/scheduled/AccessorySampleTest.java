package com.hp.jetadvantage.link.pkgmgt.scheduled;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.hp.jetadvantage.link.pkgmgt.helper.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AccessorySampleTest {

    private static final String TAG = "AccessorySampleTest";

    private static class Info {
        static final String packageName = "com.hp.workpath.sample.accessorysample";
        static final String solutionId = "11111111-1111-1111-9991-111111111111";
        static final String solutionName = "Accessory Sample";
        static final String applicationAgentId = "11111111-1111-1111-0002-111111111111";
        static final String hpk2Version = "1.6.3";
        static final String vendorName = "HP";
        static final String functionType = "com.hp.ext.service.usbAccessories.version.1.type.usbAccessoriesAgentRegistrationRecord";
        static final String param1_1 = "{\"productId\":\"69\",\"registrationType\":\"OWNED\",\"serialNumber\":null,\"vendorId\":\"1008\"}";
    }

    @Test
    public void verifyAccessorySamplePackage() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        Log.i(TAG, "Starting verification for package: " + Info.packageName);
        
        Cursor cursor = Utils.queryPackage(context, Info.packageName);
        assertNotNull("Failed to query package: " + Info.packageName, cursor);
        
        try {
            if (cursor.moveToFirst()) {
                // Verify common columns
                verifyColumnValue(cursor, "packageName", Info.packageName);
                
                // Specific verification for Accessory Sample using Info constants
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
    public void verifyAccessorySampleProvider() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Log.i(TAG, "Starting provider verification for package: " + Info.packageName);

        Cursor cursor = Utils.queryProvider(context, Info.packageName);
        assertNotNull("Failed to query provider for package: " + Info.packageName, cursor);

        try {
            boolean found1 = false;
            boolean found2 = false;
            
            while (cursor.moveToNext()) {
                int functionTypeIndex = cursor.getColumnIndex("functionType");
                if (functionTypeIndex >= 0) {
                    String functionType = cursor.getString(functionTypeIndex);
                    if (Info.functionType.equals(functionType)) {
                        Log.i(TAG, "Found matching functionType: " + functionType);
                        
                        // Verify other columns for this provider record
                        verifyColumnValue(cursor, "packageName", Info.packageName);
                        verifyColumnValue(cursor, "solutionId", Info.solutionId);

                        // Check extData1
                        int extData1Index = cursor.getColumnIndex("extData1");
                        if (extData1Index >= 0) {
                            String extData1 = cursor.getString(extData1Index);
                            if (Info.param1_1.equals(extData1)) {
                                found1 = true;
                                Log.i(TAG, "Found matching param1_1");
                            }
                        }
                    }
                }
            }
            
            assertTrue("Param1 value 1 not found: " + Info.param1_1, found1);
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
