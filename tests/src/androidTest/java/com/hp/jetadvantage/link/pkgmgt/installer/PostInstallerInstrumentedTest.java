package com.hp.jetadvantage.link.pkgmgt.installer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.ext.service.application.MessageCenterAgentRegistrationRecord;
import com.hp.ext.service.authentication.AuthenticationAgentRegistrationRecord;
import com.hp.ext.service.copy.CopyAgentRegistrationRecord;
import com.hp.ext.service.jobStatistics.JobStatisticsAgentRegistrationRecord;
import com.hp.ext.service.printJob.PrintJobAgentRegistrationRecord;
import com.hp.ext.service.scanJob.ScanJobAgentRegistrationRecord;
import com.hp.ext.service.security.SecurityAgentRegistrationRecord;
import com.hp.ext.service.solutionManager.SolutionNotificationAgentRegistrationRecord;
import com.hp.ext.service.supplies.SuppliesAgentRegistrationRecord;
import com.hp.ext.service.usbAccessories.UsbAccessoriesAgentRegistrationRecord;
import com.hp.ext.types.solutionManager.SolutionContent;
import com.hp.jetadvantage.link.pkgmgt.PackageContract;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;
import com.hp.jetadvantage.link.pkgmgt.helper.FakeContentProvider;
import com.hp.jetadvantage.link.pkgmgt.helper.Utils;
import com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.solutions.Solutions;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class PostInstallerInstrumentedTest {

    private static final String TAG = "PostInstallerTest";

    // Agent constants
    private static final String AUTH_PACKAGE_NAME = "com.hp.workpath.sample.authentication";
    private static final String AUTH_SOLUTION_UUID = "11111111-1111-1111-9993-111111111111";
    private static final String AUTH_AGENT_ID = "13a0ee28-6c80-4fec-9260-01de3a4c15ef";
    private static final String AUTH_AGENT_LAUNCH_URI = "intent:#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "launchFlags=0x10008000;component=com.hp.workpath.sample.authentication/.AuthenticationActivity;end";

    private static final String ACCESSORY_PACKAGE_NAME = "com.hp.workpath.sample.accessorysample";
    private static final String ACCESSORY_SOLUTION_UUID = "11111111-1111-1111-9991-111111111111";
    private static final String ACCESSORY_AGENT_ID = "5ba4db8b-e4ef-4bac-984c-319920fff9d6";
    private static final String ACCESSORY_REGISTRATIONS_1 = "{\"productId\":\"69\",\"registrationType\":\"OWNED\",\"serialNumber\":\"NULL\"," +
            "\"vendorId\":\"1008\"}";
    private static final String ACCESSORY_REGISTRATIONS_2 = "{\"productId\":\"70\",\"registrationType\":\"SHARED\",\"serialNumber\":null," +
            "\"vendorId\":\"1009\"}";

    private static final String SCAN_PACKAGE_NAME = "com.hp.workpath.sample.scansample";
    private static final String SCAN_SOLUTION_UUID = "11111111-1111-1111-9999-111111111111";
    private static final String SCAN_AGENT_ID = "26c78c4c-b81b-4812-9e56-c8f8830589dd";

    private static final String COPY_PACKAGE_NAME = "com.hp.workpath.sample.copysample";
    private static final String COPY_SOLUTION_UUID = "11111111-1111-1111-9996-111111111111";
    private static final String COPY_AGENT_ID = "ff66f4f6-dae5-4d5f-9380-42287cb751e4";

    private static final String PRINT_PACKAGE_NAME = "com.hp.workpath.sample.printsample";
    private static final String PRINT_SOLUTION_UUID = "11111111-1111-1111-9998-111111111111";
    private static final String PRINT_AGENT_ID = "f882ce50-51c3-4a33-9c7b-c1c3872b565a";

    private static final String SUPPLIES_PACKAGE_NAME = "com.hp.workpath.sample.suppliessample";
    private static final String SUPPLIES_SOLUTION_UUID = "11111111-1111-1111-8889-111111111111";
    private static final String SUPPLIES_AGENT_ID = "d50ec0d8-b16b-434b-abe2-5d2202aad652";

    private static final String ACCESS_PACKAGE_NAME = "com.hp.workpath.sample.accesssample";
    private static final String ACCESS_SOLUTION_UUID = "11111111-1111-1111-9992-111111111111";
    private static final String SECURITY_AGENT_ID = "79686f62-5a7e-4fc7-95bc-81de0e6673e0";

    private static final String STATISTICS_PACKAGE_NAME = "com.hp.workpath.sample.statisticsample";
    private static final String STATISTICS_SOLUTION_UUID = "11111111-1111-1111-8888-111111111111";
    private static final String STATISTICS_AGENT_ID = "c5b76ad9-dfba-4111-9245-832494bf5c89";

    private static final String SOLUTION_NOTIFICATION_AGENT_ID = "4d152755-5212-45da-8aa2-e34f92aef907";

    private static final String MESSAGE_CENTER_PACKAGE_NAME = "com.hp.workpath.sample.messagecentersample";
    private static final String MESSAGE_CENTER_SOLUTION_UUID = "11111111-1111-1111-8887-111111111111";
    private static final String MESSAGE_CENTER_AGENT_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Mock
    Context mockContext;
    @Mock
    Cursor mockCursor;

    private FakeContentProvider fakeContentProvider;
    private Context appContext;
    private PostInstaller postInstaller;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        postInstaller = new TestPostInstaller(0, null, AUTH_PACKAGE_NAME, "Test Message", mockContext);
        fakeContentProvider = new FakeContentProvider(mockCursor);
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForAuthn_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("authentication_solution.json", false, AUTH_SOLUTION_UUID, AUTH_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), AUTH_AGENT_ID, AUTH_PACKAGE_NAME, AUTH_SOLUTION_UUID, AUTH_AGENT_LAUNCH_URI,
                new AuthenticationAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForAuthn_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("authentication_solution.json", true, AUTH_SOLUTION_UUID, AUTH_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), AUTH_AGENT_ID, AUTH_PACKAGE_NAME, AUTH_SOLUTION_UUID, AUTH_AGENT_LAUNCH_URI,
                new AuthenticationAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForAccessory_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("usbAccessory_solution.json", false, ACCESSORY_SOLUTION_UUID, ACCESSORY_PACKAGE_NAME);

        assertEquals("updated elements", 2, updateValues.size());
        validateContentValues(updateValues.get(0), ACCESSORY_AGENT_ID, ACCESSORY_PACKAGE_NAME, ACCESSORY_SOLUTION_UUID, null,
                new UsbAccessoriesAgentRegistrationRecord().getTypeGUN(), ACCESSORY_REGISTRATIONS_1);
        validateContentValues(updateValues.get(1), ACCESSORY_AGENT_ID, ACCESSORY_PACKAGE_NAME, ACCESSORY_SOLUTION_UUID, null,
                new UsbAccessoriesAgentRegistrationRecord().getTypeGUN(), ACCESSORY_REGISTRATIONS_2);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForAccessory_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("usbAccessory_solution.json", true, ACCESSORY_SOLUTION_UUID, ACCESSORY_PACKAGE_NAME);

        assertEquals("updated elements", 2, updateValues.size());
        validateContentValues(updateValues.get(0), ACCESSORY_AGENT_ID, ACCESSORY_PACKAGE_NAME, ACCESSORY_SOLUTION_UUID, null,
                new UsbAccessoriesAgentRegistrationRecord().getTypeGUN(), ACCESSORY_REGISTRATIONS_1);
        validateContentValues(updateValues.get(1), ACCESSORY_AGENT_ID, ACCESSORY_PACKAGE_NAME, ACCESSORY_SOLUTION_UUID, null,
                new UsbAccessoriesAgentRegistrationRecord().getTypeGUN(), ACCESSORY_REGISTRATIONS_2);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForScanJob_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("scanJob_solution.json", false, SCAN_SOLUTION_UUID, SCAN_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SCAN_AGENT_ID, SCAN_PACKAGE_NAME, SCAN_SOLUTION_UUID, null,
                new ScanJobAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForScanJob_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("scanJob_solution.json", true, SCAN_SOLUTION_UUID, SCAN_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SCAN_AGENT_ID, SCAN_PACKAGE_NAME, SCAN_SOLUTION_UUID, null,
                new ScanJobAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForCopyJob_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("copyJob_solution.json", false, COPY_SOLUTION_UUID, COPY_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), COPY_AGENT_ID, COPY_PACKAGE_NAME, COPY_SOLUTION_UUID, null,
                new CopyAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForCopyJob_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("copyJob_solution.json", true, COPY_SOLUTION_UUID, COPY_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), COPY_AGENT_ID, COPY_PACKAGE_NAME, COPY_SOLUTION_UUID, null,
                new CopyAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForPrintJob_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("printJob_solution.json", false, PRINT_SOLUTION_UUID, PRINT_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), PRINT_AGENT_ID, PRINT_PACKAGE_NAME, PRINT_SOLUTION_UUID, null,
                new PrintJobAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForPrintJob_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("printJob_solution.json", true, PRINT_SOLUTION_UUID, PRINT_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), PRINT_AGENT_ID, PRINT_PACKAGE_NAME, PRINT_SOLUTION_UUID, null,
                new PrintJobAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSupplies_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("supplies_solution.json", false, SUPPLIES_SOLUTION_UUID, SUPPLIES_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SUPPLIES_AGENT_ID, SUPPLIES_PACKAGE_NAME, SUPPLIES_SOLUTION_UUID, null,
                new SuppliesAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSupplies_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("supplies_solution.json", true, SUPPLIES_SOLUTION_UUID, SUPPLIES_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SUPPLIES_AGENT_ID, SUPPLIES_PACKAGE_NAME, SUPPLIES_SOLUTION_UUID, null,
                new SuppliesAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSecurityAgentRecord_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("security_solution.json", false, ACCESS_SOLUTION_UUID, ACCESS_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SECURITY_AGENT_ID, ACCESS_PACKAGE_NAME, ACCESS_SOLUTION_UUID, null,
                new SecurityAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSecurityAgentRecord_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("security_solution.json", true, ACCESS_SOLUTION_UUID, ACCESS_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SECURITY_AGENT_ID, ACCESS_PACKAGE_NAME, ACCESS_SOLUTION_UUID, null,
                new SecurityAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSolutionNotificationAgentRecord_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("solution_notification_solution.json", false, COPY_SOLUTION_UUID, COPY_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SOLUTION_NOTIFICATION_AGENT_ID, COPY_PACKAGE_NAME, COPY_SOLUTION_UUID, null,
                new SolutionNotificationAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSolutionNotificationAgentRecord_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("solution_notification_solution.json", true, COPY_SOLUTION_UUID, COPY_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SOLUTION_NOTIFICATION_AGENT_ID, COPY_PACKAGE_NAME, COPY_SOLUTION_UUID, null,
                new SolutionNotificationAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForSolutionNotificationAgentRecordWithLocalizationEmpty_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("solution_notification_solution_localization_empty.json", true, COPY_SOLUTION_UUID, COPY_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), SOLUTION_NOTIFICATION_AGENT_ID, COPY_PACKAGE_NAME, COPY_SOLUTION_UUID, null,
                new SolutionNotificationAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenInsertProvidersCalledForSolutionNotificationAgentRecord_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("statistics_solution.json", false, STATISTICS_SOLUTION_UUID, STATISTICS_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), STATISTICS_AGENT_ID, STATISTICS_PACKAGE_NAME, STATISTICS_SOLUTION_UUID, null,
                new JobStatisticsAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForJobStatisticsAgentRecord_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("statistics_solution.json", true, STATISTICS_SOLUTION_UUID, STATISTICS_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), STATISTICS_AGENT_ID, STATISTICS_PACKAGE_NAME, STATISTICS_SOLUTION_UUID, null,
                new JobStatisticsAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForMessageCenterAgentRecord_thenInsertDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("message_center_solution.json", false, MESSAGE_CENTER_SOLUTION_UUID, MESSAGE_CENTER_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), MESSAGE_CENTER_AGENT_ID, MESSAGE_CENTER_PACKAGE_NAME, MESSAGE_CENTER_SOLUTION_UUID, null,
                new MessageCenterAgentRegistrationRecord().getTypeGUN(), null);
    }

    @Test
    public void givenPostInstaller_whenUpdateProvidersCalledForMessageCenterAgentRecord_thenUpdateDB() throws Exception {
        List<ContentValues> updateValues = testUpdateProviders("message_center_solution.json", true, MESSAGE_CENTER_SOLUTION_UUID, MESSAGE_CENTER_PACKAGE_NAME);

        assertEquals("updated elements", 1, updateValues.size());
        validateContentValues(updateValues.get(0), MESSAGE_CENTER_AGENT_ID, MESSAGE_CENTER_PACKAGE_NAME, MESSAGE_CENTER_SOLUTION_UUID, null,
                new MessageCenterAgentRegistrationRecord().getTypeGUN(), null);
    }

    private List<ContentValues> testUpdateProviders(String fileName, boolean entryExists, String solutionUuid, String packageName) throws Exception {
        setUpMocksForDatabaseOperation(entryExists);

        Pair<String, SolutionContent> pairs = createSolutionContent(fileName);
        SolutionContent solutionContent = pairs.second;
        String json = pairs.first;

        postInstaller.updateProviders(solutionContent, packageName, json);
        List<ContentValues> resultValueList = entryExists ? fakeContentProvider.getUpdateValues() : fakeContentProvider.getInsertValues();

        return resultValueList;
    }

    private void setUpMocksForDatabaseOperation(boolean entryExists) throws Exception {
        when(mockContext.getPackageManager()).thenReturn(appContext.getPackageManager());
        when(mockContext.getContentResolver()).thenReturn(ContentResolver.wrap(fakeContentProvider));
        when(mockCursor.moveToNext()).thenReturn(entryExists);
        injectAppContext(mockContext);
    }

    private void injectAppContext(Context context) throws Exception {
        Field contextRefField = PackageManagerApplication.class.getDeclaredField("contextRef");
        contextRefField.setAccessible(true);
        contextRefField.set(null, new WeakReference<>(context));
    }

    private void validateContentValues(ContentValues resultValues, String agentId, String packageName, String solutionId, String expectedLaunchUri,
                                       String functionType, String param1) {
        assertEquals("AGENT_ID", agentId, resultValues.get(PackageContract.PackageProviderEntry.AGENT_ID));
        assertEquals("PACKAGE_NAME", packageName, resultValues.get(PackageContract.PackageProviderEntry.PACKAGE_NAME));
        assertEquals("SOLUTION_ID", solutionId, resultValues.get(PackageContract.PackageProviderEntry.SOLUTION_ID));
        assertEquals("FUNCTION_TYPE", functionType, resultValues.get(PackageContract.PackageProviderEntry.FUNCTION_TYPE));
        if (expectedLaunchUri != null) {
            assertEquals("LAUNCH_INTENT", expectedLaunchUri, resultValues.get(PackageContract.PackageProviderEntry.LAUNCH_INTENT));
        }
        if (param1 != null) {
            assertEquals("EXT_DATA1", param1, resultValues.get(PackageContract.PackageProviderEntry.EXT_DATA1));
        }
    }

    private Pair<String, SolutionContent> createSolutionContent(String filepath) {
        try {
            String json = Utils.loadTestJsonResource(Objects.requireNonNull(getClass().getClassLoader()), filepath);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return new Pair<>(json, objectMapper.readValue(json, SolutionContent.class));
        } catch (Exception e) {
            Log.e(TAG, "Error creating SolutionContent from JSON", e);
            throw new RuntimeException("Failed to create SolutionContent", e);
        }
    }

    public class TestPostInstaller extends PostInstaller {
        private final Context context;

        public TestPostInstaller(int statusCode, Solutions solutions, String packageName, String message, Context mockContext) {
            super(statusCode, solutions, packageName, message, "");
            this.context = mockContext;
        }

        @Override
        protected int uninstallProviders(String solutionId) {
            return 0; // Mocked behavior
        }

        @Override
        protected Context getContext() {
            return context;
        }
    }
}