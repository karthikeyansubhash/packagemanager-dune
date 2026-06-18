package com.hp.smartux.lib.cpkutil.test;

import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import com.hp.jetadvantage.link.pkgmgt.lib.SubApp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.ElementException;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.core.ValueRequiredException;
import org.simpleframework.xml.stream.Format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HpkFileTest {
    private static final UUID APP_UUID = java.util.UUID.randomUUID();
    private static final UUID APP_SUB_UUID = java.util.UUID.randomUUID();
    private static final UUID APP_CFG_UUID = java.util.UUID.randomUUID();
    private static final String APP_NAME = "Test App";
    private static final String VENDOR_NAME = "HP";
    private static final String APP_DATE = "20181210";
    private static final PlatformType PLATFORM_TYPE = PlatformType.LinkForDevice;
    private static final String PLATFORM_VERSION = "29.5";
    private static final String XML_PROLOG = "<?xml version=\"1.0\" encoding= \"UTF-8\" standalone=\"yes\"?>";
    private static final Serializer SERIALIZER = new Persister(new AnnotationStrategy(), new Format(XML_PROLOG));
//    private static final String SCHEMA_LOCATION = "http://www.hp.com/schemas/imaging/OXPm/service/connectors/2017/01/31connector.xsd";
    private static final String SCHEMA_LOCATION = "http://www.hp.com/schemas/jetadvantage/link/hpk/v2.1 hpk.xsd";

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void connectorGettersSetters() throws Exception {
        File installFile = createApk();
        Connector connector = getConnector(installFile);

        assertEquals(APP_UUID, connector.getUuid());
        assertEquals(APP_NAME, connector.getName());
        assertEquals(VENDOR_NAME, connector.getVendorName());
        assertEquals(APP_DATE, connector.getDate());
        assertEquals(PLATFORM_TYPE, connector.getPlatformType());
        assertEquals(installFile.getName(), connector.getInstallFile());
        assertEquals(SCHEMA_LOCATION, connector.getSchemaLocation());
    }

    @Test
    public void connectorHashcodeEquals() throws Exception {
        File installFile = createApk();
        Connector connector1 = getConnector(installFile);

        Connector connector2 = getConnector(installFile);

        Map<Connector, String> map = new HashMap<>();
        map.put(connector1, APP_NAME);
        assertEquals(1, map.size());

        map.put(connector2, APP_NAME);
        assertEquals(1, map.size());

        assertEquals(connector1, connector2);

        connector2.setSchemaLocation("");
        assertNotEquals(connector1, connector2);
    }

    @Test
    public void connectorAsString() throws Exception {
        File installFile = createApk();
        Connector connector1 = getConnector(installFile);
        String stringXml = connector1.asString();

        assertNotNull(stringXml);
        Connector connector2 = SERIALIZER.read(Connector.class, stringXml);
        assertEquals(connector1, connector2);
    }

    @Test
    public void connectorWrongUUIDRead() throws Exception {
        thrown.expect(ValueRequiredException.class);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        File installFile = createApk();
        Connector connector = getConnector(installFile);

        connector.writeTo(bos);

        String xml = bos.toString("UTF-8");
        SERIALIZER.read(Connector.class, xml.replace(APP_UUID.toString(), "dummy"));
    }

    @Test
    public void connectorWriteTo() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        File installFile = createApk();
        Connector connector = getConnector(installFile);

        connector.writeTo(bos);

        String xml = bos.toString("UTF-8");
        Connector connectorNew = SERIALIZER.read(Connector.class, xml);

        assertEquals(connector, connectorNew);
    }

    @Test
    public void connectorWriteToProlog() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        File installFile = createApk();
        Connector connector = getConnector(installFile);

        connector.writeTo(bos);

        String xml = bos.toString("UTF-8");
        assertTrue(xml.startsWith(XML_PROLOG));
    }

    @Test
    public void cpkFileCreate() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");
        File installFile = createApk();

        Connector connector = getConnector(installFile);
        createCpk(outputFile, connector, installFile);

        HpkFile hpkFile = new HpkFile(outputFile);

        assertNotNull(hpkFile.getConnector());
        assertNotNull(hpkFile.getConnector().getInstallFile());
        InputStream fileStream = hpkFile.getInstallFile();
        try {
            assertNotNull(fileStream);
        } finally {
            fileStream.close();
        }

        assertEquals(connector, hpkFile.getConnector());
        assertEquals(installFile.getName(), hpkFile.getConnector().getInstallFile());
    }

    @Test
    public void cpkFileCreateWithoutInstalledFileInXml() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");
        File installFile = createApk();

        Connector connector = getConnector(null);

        thrown.expect(ElementException.class);
        createCpk(outputFile, connector, installFile);
    }

    @Test
    public void cpkFileCreateWithoutInstalledFileInArchive() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");
        File installFile = createApk();

        Connector connector = getConnector(installFile);

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));

        ZipEntry connectorEntry = new ZipEntry(Connector.XML_FILENAME);
        zos.putNextEntry(connectorEntry);
        SERIALIZER.write(connector, zos);
        zos.closeEntry();
        zos.close();

        thrown.expect(IOException.class);

        HpkFile hpkFile = new HpkFile(outputFile);

        InputStream fileStream = hpkFile.getInstallFile();
        try {
            assertNotNull(fileStream);
        } finally {
            fileStream.close();
        }
    }

    @Test
    public void cpkFileCreateWithoutConnectorInArchive() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));
        zos.close();

        thrown.expect(IOException.class);

        new HpkFile(outputFile);
    }

    @Test
    public void cpkFileCreateWithInvalidConnectorInArchive() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));

        ZipEntry connectorEntry = new ZipEntry(Connector.XML_FILENAME);
        zos.putNextEntry(connectorEntry);
        zos.write(XML_PROLOG.getBytes("UTF-8"));
        zos.closeEntry();
        zos.close();

        thrown.expect(IOException.class);

        HpkFile hpkFile = new HpkFile(outputFile);

        InputStream fileStream = hpkFile.getInstallFile();
        try {
            assertNotNull(fileStream);
        } finally {
            fileStream.close();
        }
    }

    @Test
    public void cpkFileCreateWithoutFile() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");
        File installFile = createApk();

        Connector connector = getConnector(installFile);

        thrown.expect(NullPointerException.class);
        createCpk(outputFile, connector, null);
    }

    @Test
    public void cpkFileCreateWithWrongFile() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");
        File installFile = createApk();

        Connector connector = getConnector(installFile);
        connector.setInstallFile("wrong.apk");

        createCpk(outputFile, connector, installFile);
    }

    @Test
    public void cpkFileReadFile() throws Exception {
        File outputFile = testFolder.newFile("testHpk" + System.currentTimeMillis() + ".hpk");

        File installFile = createApk();
        Connector connector = getConnector(installFile);
        createCpk(outputFile, connector, installFile);

        HpkFile loadedHpkFile = new HpkFile(outputFile);
        assertEquals(connector, loadedHpkFile.getConnector());

        InputStream fileStream = loadedHpkFile.getInstallFile();
        try {
            assertNotNull(fileStream);
        } finally {
            fileStream.close();
        }
    }

    @Test
    public void cpkFileReadFilepath() throws Exception {
        String fileName = "testHpk" + System.currentTimeMillis() + ".hpk";
        File outputFile = testFolder.newFile(fileName);

        File installFile = createApk();
        Connector connector = getConnector(installFile);
        createCpk(outputFile, connector, installFile);

        HpkFile loadedHpkFile = new HpkFile(testFolder.getRoot().getPath() + File.separator + fileName);
        assertEquals(connector, loadedHpkFile.getConnector());
        assertEquals(installFile.getName(), loadedHpkFile.getConnector().getInstallFile());
    }

    @Test
    public void cpkFileReadWrongCpkPath() throws Exception {
        String fileName = "testHpk" + System.currentTimeMillis() + ".hpk";

        thrown.expect(IOException.class);

        new HpkFile(testFolder.getRoot().getPath() + File.separator + fileName);
    }

    @Test
    public void cpkFileReadCertificates() throws Exception {
        String fileName = "testHpk" + System.currentTimeMillis() + ".hpk";
        File outputFile = testFolder.newFile(fileName);

        System.out.println("[PACMAN]cpkFileReadCertificates(): outputFile location is "+outputFile.getAbsolutePath());

        File installFile = createApk();
        Connector connector = getConnector(installFile);
        createCpk(outputFile, connector, installFile);

        System.out.println("[PACMAN]cpkFileReadCertificates(): installFile location is "+installFile.getAbsolutePath());

        signCpk(outputFile);

        HpkFile loadedHpkFile = new HpkFile(testFolder.getRoot().getPath() + File.separator + fileName);
        assertEquals(connector, loadedHpkFile.getConnector());
        //assertNotNull(loadedHpkFile.getCertificates());
        //assertEquals(1, loadedHpkFile.getCertificates().length);
    }

    private static void copyStream(final InputStream input, final OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            input.close();
        }
    }

    private File createApk() throws IOException {
        File file = testFolder.newFile("testApk" + System.currentTimeMillis() + ".apk");
        FileWriter fw = new FileWriter(file);
        fw.write("This is a test file");
        fw.close();
        return file;
    }

    private void signCpk(File outputFile) throws Exception {
        URL classes = getClass().getClassLoader().getResource(".");
        assertNotNull(classes);

        File folder = new File(classes.toURI()).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        File keyStore = new File(new File(folder, "Platform"), "platform.keystore");

        Process jarSigner = Runtime.getRuntime().exec("jarsigner -keystore " + keyStore.getAbsolutePath()
                + " -storepass android -keypass android "
                + outputFile.getAbsolutePath() + " androiddebugkey");
        jarSigner.waitFor();
    }

    private Connector getConnector(File installFile) {
        Connector connector = new Connector();
        connector.setSchemaLocation(SCHEMA_LOCATION);
        connector.setUuid(APP_UUID);
        connector.setName(APP_NAME);
        connector.setVendorName(VENDOR_NAME);
        connector.setDate(APP_DATE);
        connector.setPlatformType(PlatformType.LinkForDevice);
        connector.setPlatformVersion(PLATFORM_VERSION);
        if (installFile != null) {
            connector.setInstallFile(installFile.getName());
        }
        connector.setSubAppList(new ArrayList<SubApp>());
        SubApp subApp = new SubApp();
        subApp.setUuid(APP_SUB_UUID);
        subApp.setPlatformId("com.acme.app/com.acme.app.util.SubActivity");
        connector.getSubAppList().add(subApp);

        subApp = new SubApp();
        subApp.setUuid(APP_CFG_UUID);
        subApp.setPlatformId("com.acme.app/com.acme.app.util.CfgActivity");
        connector.getSubAppList().add(subApp);

        return connector;
    }

    private void createCpk(final File outputFile, final Connector connector, final File installFile)
            throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));

        ZipEntry connectorEntry = new ZipEntry(Connector.XML_FILENAME);
        zos.putNextEntry(connectorEntry);
        SERIALIZER.write(connector, zos);
        zos.closeEntry();

        ZipEntry installedFileEntry = new ZipEntry(connector.getInstallFile());
        zos.putNextEntry(installedFileEntry);
        copyStream(new FileInputStream(installFile), zos);
        zos.closeEntry();

        zos.close();
    }
}
