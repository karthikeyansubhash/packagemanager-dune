package com.hp.jetadvantage.link.packagemanager.hpkutil.test.etc;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static String HOST = System.getenv("TARGET_DEVICE");
    public static String PASSWORD = System.getenv("TARGET_DEVICE_PW");
    public static final String ADMIN = "admin";
    public static final String WRONG_WORD = "wrong_word";

    public static File getAPKFile(){
        return new File(getTestDataFolder() + "/apks/ConfigSample.apk");
    }

    public static File getLinkForDeviceHPKFile(){
        return new File(getTestDataFolder() + "/android/ConfigSample.hpk");
    }

    public static File getLinkForWebHPKFile(){
        return new File(getTestDataFolder() + "/omni/oxpd_sample10.hpk");
    }

    public static File getXMLFile(){
        return new File(getTestDataFolder() + "/xml/oxpd_sample_10.xml");
    }

    public static File getAppFile(String xmlPath){
        return new File(getTestDataFolder() + xmlPath);
    }

    public static Connector getConnector(File installFile) throws IOException{
        HpkFile hpkFile = new HpkFile(installFile);
        Connector connector = hpkFile.getConnector();
        return connector;
    }
	
	public static String getTestDataFolder(){
        File hpkToolFolder = new File(System.getProperty("user.dir"));
        File systemFolder = hpkToolFolder.getParentFile().getParentFile();
        File testDataFolder = new File(systemFolder.getAbsolutePath() + "/TestData");
        return testDataFolder.getAbsolutePath();
    }

    public static String encodeFileToBase64Binary(File file) {
        try {
            File icon = new File(file.getAbsolutePath());
            FileInputStream fileInputStream = new FileInputStream(icon);
            byte[] bytes = new byte[(int)icon.length()];
            fileInputStream.read(bytes);

            return DatatypeConverter.printBase64Binary(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ButtonInfo getButtonInfo(File buttonFile) {
        try {
            Serializer serializer = new Persister(new AnnotationStrategy(),
                    new Format("<?xml version=\"1.0\"?>"));

            InputStream inputStream = new FileInputStream(buttonFile);

            return serializer.read(ButtonInfo.class, inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
