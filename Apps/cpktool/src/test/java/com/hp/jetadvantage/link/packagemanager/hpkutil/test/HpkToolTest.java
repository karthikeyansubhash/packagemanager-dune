package com.hp.jetadvantage.link.packagemanager.hpkutil.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.HpkTool;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration.ConfigManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.HPKInstaller;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.HPKUninstaller;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.InstallListener;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Configuration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.packagemanager.hpkutil.test.etc.Utils;
import com.hp.jetadvantage.link.packagemanager.hpkutil.test.value.ConfigSampleTest;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskStatus;
import com.hp.jetadvantage.link.pkgmgt.lib.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.UUID;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HpkToolTest {

    String jsonData;
    private static final String EOL = System.getProperty("line.separator");
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        Constants.DEFAULT_PLATFORM_TYPE = PlatformType.LinkForDevice;
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void test01_01LoadHPKFile() throws Exception {
        File installFile = Utils.getLinkForDeviceHPKFile();
        Connector connector = Utils.getConnector(installFile);
        assertEquals(ConfigSampleTest.APP_UUID.toString(), connector.getUuid().toString());
        assertEquals(ConfigSampleTest.APP_NAME, connector.getName());
        assertEquals(ConfigSampleTest.VENDOR_NAME, connector.getVendorName());
        assertEquals(ConfigSampleTest.APP_DATE, connector.getDate());
        assertEquals(PlatformType.LinkForDevice, connector.getPlatformType());
        assertEquals(ConfigSampleTest.INSTALL_FILE, connector.getInstallFile());
        assertEquals(ConfigSampleTest.SCHEMA_LOCATION, connector.getSchemaLocation());
    }

    @Test
    public void test01_02InstallerNormalCase() throws Exception {
        System.out.println("host: " + Utils.HOST);
        testInstaller(Utils.HOST, Utils.ADMIN, Utils.PASSWORD, taskInterface);
    }

    @Test
    public void test01_03InstallerWrongHostCase() throws Exception {
        testInstaller(Utils.HOST + Utils.WRONG_WORD, Utils.ADMIN, Utils.PASSWORD, wrongTaskInterface);
    }

    @Test
    public void test01_04InstallerWrongPasswordCase() throws Exception {
        testInstaller(Utils.HOST, Utils.ADMIN, Utils.PASSWORD + Utils.WRONG_WORD, wrongTaskInterface);
    }

    @Test
    public void test02_01GetConfigurationNormalCase() throws Exception {
        jsonData = "{\"url\":\"https:\\/\\/developer.hp.com\",\"colorMode\":\"MONO\",\"paperSize\":\"LETTER\",\"copies\":3,\"desc\":\"option=empty\"}";
        testGetConfiguration(Utils.HOST, ConfigSampleTest.APP_UUID, Utils.PASSWORD, configTaskInterface);
    }

    @Test
    public void test02_02GetConfigurationWrongPasswordCase() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Please check password.");
        testGetConfiguration(Utils.HOST, ConfigSampleTest.APP_UUID, Utils.PASSWORD + Utils.WRONG_WORD, wrongConfigTaskInterface);
    }

    @Test
    public void test02_03GetConfigurationWrongUUIDCase() throws Exception {
        testGetConfiguration(Utils.HOST, ConfigSampleTest.WRONG_APP_UUID, Utils.PASSWORD, wrongConfigTaskInterface);
    }

    @Test
    public void test03_01UpdateConfigurationNormalCase() throws Exception {
        jsonData = "{\"test\":\"This is for test\",\"number\":162534,\"author\":\"Hyoeun\"}";
        testUpdateConfiguration(Utils.HOST, ConfigSampleTest.APP_UUID, Utils.PASSWORD, jsonData, configTaskInterface);
    }

    @Test
    public void test03_02UpdateConfigurationWrongPasswordCase() throws Exception {
        jsonData = "{\"test\":\"This is for test\",\"number\":162534,\"author\":\"Hyoeun\"}";
        testUpdateConfiguration(Utils.HOST, ConfigSampleTest.APP_UUID, Utils.PASSWORD + Utils.WRONG_WORD, jsonData, wrongConfigTaskInterface);
    }

    @Test
    public void test03_03UpdateConfigurationWrongUUIDCase() throws Exception {
        jsonData = "{\"test\":\"This is for test\",\"number\":162534,\"author\":\"Hyoeun\"}";
        testUpdateConfiguration(Utils.HOST, ConfigSampleTest.WRONG_APP_UUID, Utils.PASSWORD, jsonData, wrongConfigTaskInterface);
    }

    @Test
    public void test03_04UpdateConfigurationNullJsonCase() throws Exception {
        jsonData = "";
        testUpdateConfiguration(Utils.HOST, ConfigSampleTest.APP_UUID, Utils.PASSWORD, jsonData, wrongConfigTaskInterface);
    }

    @Test
    public void test04_01CommandHelpCase() throws Exception {
        System.setProperty("test", "true");
        String cmd = "--help";
        String[] args = cmd.split(" ");
        HpkTool.main(args);

        String helpFirstLine = "HPK Tool";
        assertTrue(outContent.toString().contains(helpFirstLine));
    }

    @Test
    public void test05_01CommandCreateHPKUsingAPKCase() throws Exception {
        File apkFile = Utils.getAPKFile();
        String testHPKPath = apkFile.getParent() + "\\test.hpk";

        String cmd = "--create --output " + testHPKPath + " --installfile " + apkFile.getAbsolutePath() + " --uuid 1234abcd-1234-1234-123a-12345678abcd --name Test_Application --vendor My_Company";
        String[] args = cmd.split(" ");
        HpkTool.main(args);

        HpkFile hpkFile = new HpkFile(testHPKPath);
        Connector connector = hpkFile.getConnector();
        assertEquals("1234abcd-1234-1234-123a-12345678abcd", connector.getUuid().toString());
        assertEquals("Test_Application", connector.getName());
        assertEquals("My_Company", connector.getVendorName());
        assertEquals("LinkForDevice", connector.getPlatformType().toString());
    }

    @Test
    public void test05_02CommandCreateHPKWithSubActivityUsingAPKCase() throws Exception {
        File apkFile = Utils.getAPKFile();
        String testHPKPath = apkFile.getParent() + "\\test.hpk";

        String cmd = "--create --output " + testHPKPath + " --installfile " + apkFile.getAbsolutePath() + " --uuid 1234abcd-1234-1234-123a-12345678abcd --name Test_Application --vendor My_Company --subactivity1 1234abcd-1234-1234-0000-12345678abce,com.test.app/com.test.app.MyActivity --subactivity2 1234abcd-1234-1234-0000-12345678abcf,com.test.app/com.test.app.SecondActivity,Configuration";
        String[] args = cmd.split(" ");
        HpkTool.main(args);

        HpkFile hpkFile = new HpkFile(testHPKPath);
        Connector connector = hpkFile.getConnector();
        assertEquals("1234abcd-1234-1234-123a-12345678abcd", connector.getUuid().toString());
        assertEquals("Test_Application", connector.getName());
        assertEquals("My_Company", connector.getVendorName());
        assertEquals("LinkForDevice", connector.getPlatformType().toString());
        ArrayList<SubApp> subList = connector.getSubAppList();
        assertEquals("1234abcd-1234-1234-0000-12345678abce", subList.get(0).getUuid().toString());
        assertEquals("com.test.app/com.test.app.MyActivity", subList.get(0).getPlatformId());
        assertEquals("1234abcd-1234-1234-0000-12345678abcf", subList.get(1).getUuid().toString());
        assertEquals("com.test.app/com.test.app.SecondActivity", subList.get(1).getPlatformId());
    }

    @Test
    public void test05_03CommandXMLCreateCase() throws Exception {
        File apkFile = Utils.getAPKFile();
        File appFile = Utils.getAppFile("/xml/hpk.xml");
        String testHPKPath = apkFile.getParent() + "\\ConfigSample.hpk";

        String cmd = "--xml --output " + testHPKPath + " --installfile " + apkFile.getAbsolutePath() + " --manifest " + appFile;
        String[] args = cmd.split(" ");
        HpkTool.main(args);

        HpkFile hpkFile = new HpkFile(testHPKPath);
        Connector connector = hpkFile.getConnector();
        assertEquals("11111111-1111-1111-9995-111111111111", connector.getUuid().toString());
        assertEquals("Config Sample", connector.getName());
        assertEquals("HP", connector.getVendorName());
        assertEquals("LinkForDevice", connector.getPlatformType().toString());
        assertEquals("ConfigSample.apk", connector.getInstallFile());
    }

    @Test
    public void test05_04CommandCreateHPKWithAuthAgentUsingAPKCase() throws Exception {
        File apkFile = Utils.getAPKFile();
        String testHPKPath = apkFile.getParent() + "\\test.hpk";

        String cmd = "--create --output " + testHPKPath + " --installfile " + apkFile.getAbsolutePath() + " --uuid 1234abcd-1234-1234-123a-12345678abcd --name Test_Application --vendor My_Company --linkauthagent -p:name={\"en-US\":\"usName\",\"ko-KR\":\"krName\"} -p:description={\"en-US\":\"usDes\",\"ko-KR\":\"krDes\"} -p:url=package/activity -p:preprompt=false";
        String[] args = cmd.split(" ");
        HpkTool.main(args);

        HpkFile hpkFile = new HpkFile(testHPKPath);
        Connector connector = hpkFile.getConnector();
        assertEquals("1234abcd-1234-1234-123a-12345678abcd", connector.getUuid().toString());
        assertEquals("Test_Application", connector.getName());
        assertEquals("My_Company", connector.getVendorName());
        assertEquals("LinkForDevice", connector.getPlatformType().toString());
        ArrayList<Provider> providerList = connector.getProviders();
        assertEquals("package/activity", providerList.get(0).getAuthenticationUrl());
        assertEquals("false", providerList.get(0).getEnablePrePromptCheck());

        ArrayList<LocalizedString> titleList = providerList.get(0).getTitle();
        assertEquals("en-US", titleList.get(0).getCode());
        assertEquals("usName", titleList.get(0).getValue());
        assertEquals("ko-KR", titleList.get(1).getCode());
        assertEquals("krName", titleList.get(1).getValue());

        ArrayList<LocalizedString> descList = providerList.get(0).getDescription();
        assertEquals("en-US", descList.get(0).getCode());
        assertEquals("usDes", descList.get(0).getValue());
        assertEquals("ko-KR", descList.get(1).getCode());
        assertEquals("krDes", descList.get(1).getValue());
    }

    @Test
    public void test05_05CommandCreateHPKWithStatisticsAgentUsingAPKCase() throws Exception {
        File apkFile = Utils.getAPKFile();
        String testHPKPath = apkFile.getParent() + "\\test.hpk";

        String cmd = "--create --output " + testHPKPath + " --installfile " + apkFile.getAbsolutePath() + " --uuid 1234abcd-1234-1234-123a-12345678abcd --name Test_Application --vendor My_Company --statisticsagent -s:name={\"en-US\":\"usName\",\"ko-KR\":\"krName\"} -s:description={\"en-US\":\"usDes\",\"ko-KR\":\"krDes\"} -s:ackrequiredfordelete=false";
        String[] args = cmd.split(" ");
        HpkTool.main(args);

        HpkFile hpkFile = new HpkFile(testHPKPath);
        Connector connector = hpkFile.getConnector();
        assertEquals("1234abcd-1234-1234-123a-12345678abcd", connector.getUuid().toString());
        assertEquals("Test_Application", connector.getName());
        assertEquals("My_Company", connector.getVendorName());
        assertEquals("LinkForDevice", connector.getPlatformType().toString());

        ArrayList<Provider> providerList = connector.getProviders();
        assertEquals("false", providerList.get(0).getAckRequiredForDelete());

        ArrayList<LocalizedString> titleList = providerList.get(0).getTitle();
        assertEquals("en-US", titleList.get(0).getLanguageTag());
        assertEquals("usName", titleList.get(0).getValue());
        assertEquals("ko-KR", titleList.get(1).getLanguageTag());
        assertEquals("krName", titleList.get(1).getValue());

        ArrayList<LocalizedString> descList = providerList.get(0).getDescription();
        assertEquals("en-US", descList.get(0).getLanguageTag());
        assertEquals("usDes", descList.get(0).getValue());
        assertEquals("ko-KR", descList.get(1).getLanguageTag());
        assertEquals("krDes", descList.get(1).getValue());

    }

    @Test
    public void test06_01UnInstallerWrongHostCase() throws Exception {
        testUnInstaller(Utils.HOST + Utils.WRONG_WORD, Utils.ADMIN, Utils.PASSWORD, wrongTaskInterface);
    }

    @Test
    public void test06_02UnInstallerWrongPasswordCase() throws Exception {
        testUnInstaller(Utils.HOST, Utils.ADMIN, Utils.PASSWORD + Utils.WRONG_WORD, wrongTaskInterface);
    }

    @Test
    public void test06_03UnInstallerNormalCase() throws Exception {
        testUnInstaller(Utils.HOST, Utils.ADMIN, Utils.PASSWORD, taskInterface);
    }

    @Test
    public void test06_04UnInstallerAppNotInstallCase() throws Exception {
        testUnInstaller(Utils.HOST, Utils.ADMIN, Utils.PASSWORD, wrongTaskInterface);
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void testInstaller(String host, String admin, String password, final TaskInterface taskInterface) {
        sleep();
        HPKInstaller installer = new HPKInstaller(host);
        installer.setUsername(admin);
        installer.setUserPassword(password);
        installer.install(Utils.getLinkForDeviceHPKFile(), new InstallListener() {
            @Override
            public void status(PackageInstallerState status, String cause) {
                TaskStatus taskStatus = new TaskStatus(status, cause);
                taskInterface.updateMessage(taskStatus);
                if (status.equals(PackageInstallerState.psFailed)) {
                    this.finished();
                }
            }

            @Override
            public void finished() {
                taskInterface.onSucceed(null);
            }
        });
    }

    private void testUnInstaller(String host, String admin, String password, final TaskInterface taskInterface) {
        sleep();
        HPKUninstaller uninstaller = new HPKUninstaller(host);
        uninstaller.setUsername(admin);
        uninstaller.setUserPassword(password);
        uninstaller.uninstall(ConfigSampleTest.APP_UUID.toString(), new InstallListener() {
            @Override
            public void status(PackageInstallerState status, String cause) {
                TaskStatus taskStatus = new TaskStatus(status, cause);
                taskInterface.updateMessage(taskStatus);
                if (status.equals(PackageInstallerState.psFailed)) {
                    this.finished();
                }
            }

            @Override
            public void finished() {
                taskInterface.onSucceed(null);
            }
        });
    }

    private void testGetConfiguration(String host, UUID uuid, String password, TaskInterface taskInterface) throws Exception {
        sleep();
        Constants.DEFAULT_USER_PASSWORD = password;
        ConfigManager configManager = new ConfigManager(host, uuid);
        configManager.getConfiguration(taskInterface);
    }

    private void testUpdateConfiguration(String host, UUID uuid, String password, String jsonData, TaskInterface taskInterface) throws Exception {
        sleep();
        Constants.DEFAULT_USER_PASSWORD = password;
        Configuration configuration = new Configuration();
        if (!jsonData.isEmpty())
            configuration.setData(new JsonParser().parse(jsonData).getAsJsonObject());
        configuration.setUuid(uuid.toString());

        ConfigManager configManager = new ConfigManager(host, uuid);
        configManager.putConfiguration(configuration, taskInterface);
    }


    TaskInterface taskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            System.out.println("msg: " + msg.getState().toString());
            if (msg.getState().equals(PackageInstallerState.psCompleted)) {
                assertTrue(true);
            } else if (msg.getState().equals(PackageInstallerState.psFailed)) {
                System.out.println("msg: " + msg.getCause().toString());
                assertTrue(false);
            }
            return null;
        }

        @Override
        public void onSucceed(Object obj) {

        }

        @Override
        public void onFailed(Exception e) {

        }
    };

    TaskInterface wrongTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            System.out.println("msg: " + msg.getState().toString());
            if (msg.getState().equals(PackageInstallerState.psCompleted)) {
                System.out.println("msg: " + msg.getCause().toString());
                assertTrue(false);
            } else if (msg.getState().equals(PackageInstallerState.psFailed)) {
                assertTrue(true);
            }
            return null;
        }

        @Override
        public void onSucceed(Object obj) {

        }

        @Override
        public void onFailed(Exception e) {

        }
    };

    TaskInterface configTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            Configuration configuration = (Configuration) obj;
            if (configuration != null) {
                JsonObject jsonObject = new JsonParser().parse(jsonData).getAsJsonObject();
                if (jsonObject.toString().equals(configuration.getData().toString())) {
                    System.out.println("same");
                    assertTrue(true);
                } else {
                    System.out.println("wrong");
                    assertTrue(false);
                }
            }
        }

        @Override
        public void onFailed(Exception e) {
            System.out.println(e.getMessage());
            assertTrue(false);
        }
    };

    TaskInterface wrongConfigTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            Configuration configuration = (Configuration) obj;
            if (configuration != null) {
                assertTrue(false);
            }
        }

        @Override
        public void onFailed(Exception e) {
            assertTrue(true);
        }
    };
}
