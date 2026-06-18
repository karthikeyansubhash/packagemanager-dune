package com.hp.jetadvantage.link.pkgmgt.hpkutil;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HpkFileHelper {
    public static final Serializer SERIALIZER = new Persister(new AnnotationStrategy(),
            new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" standalone=\"yes\"?>"));

    static void generateTemplateByPlatformType(PlatformType platformType) {
        if(platformType == null) {
            platformType = PlatformType.LinkForDevice;
        }
        Connector connector = null;
        if(platformType != null && PlatformType.LinkForWeb.equals(platformType)) {
            final String XSD_VERSION_2_1 = "v2.1";
            connector = new Connector(XSD_VERSION_2_1);
        } else {
            connector = new Connector();
        }
        connector.setUuid(UUID.randomUUID());
        connector.setName("Sample application");
        connector.setVendorName("Company Name");
        connector.setDate(Constants.DATE_FORMAT.format(new Date()));
        connector.setPlatformType(platformType);

        if(PlatformType.LinkForWeb.equals(platformType)) {
            connector.setInstallFile("SampleButton.xml");
        } else {
            connector.setInstallFile("SampleApp.apk");
        }

        try (FileOutputStream outputStream = new FileOutputStream(new File(Connector.XML_FILENAME))) {
            connector.writeTo(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createCpk(final File outputFile, final Connector connector, final File installFile, final File defaultConfigFile)
                    throws Exception {
        //if (sign) {
        //    createSigned(outputFile, connector, installFile);
        //} else {
            createUnsigned(outputFile, connector, installFile, defaultConfigFile);
        //}
    }

    public static void createCpk(final File outputFile, final Connector connector, final InputStream installFile, final InputStream defaultConfigFile)
            throws Exception {
        //if (sign) {
        //    createSigned(outputFile, connector, installFile);
        //} else {
        createUnsigned(outputFile, connector, installFile, defaultConfigFile);
        //}
    }

    private static void createUnsigned(final File outputFile, final Connector connector, final File installFile, final File defaultConfigFile)
            throws Exception {
        if(defaultConfigFile != null) {
            createUnsigned(outputFile, connector, new FileInputStream(installFile), new FileInputStream(defaultConfigFile));
        } else {
            createUnsigned(outputFile, connector, new FileInputStream(installFile), null);
        }
    }

    private static void createUnsigned(final File outputFile, final Connector connector, final InputStream installFile, final InputStream defaultConfigFile)
            throws Exception {

        if(Constants.DEFAULT_EXTENSION.equalsIgnoreCase(Utils.getExtension(outputFile))) {
        }

        ZipOutputStream zos = null;

        try {
            //1. "hpk.xml"
            zos = new ZipOutputStream(new FileOutputStream(outputFile));
            ZipEntry connectorEntry = new ZipEntry(Connector.XML_FILENAME);
            zos.putNextEntry(connectorEntry);
            SERIALIZER.write(connector, zos);
            zos.closeEntry();

            //2. installFile
            if (connector.getInstallFile() != null) {
                ZipEntry installedFileEntry = new ZipEntry(connector.getInstallFile());
                zos.putNextEntry(installedFileEntry);
                copyStream(installFile, zos);
                zos.closeEntry();
            }

            //3. defaultConfigFile
            if (defaultConfigFile != null &&
                    connector.getDefaultConfig() != null && !connector.getDefaultConfig().isEmpty()) {
                ZipEntry installedFileEntry = new ZipEntry(connector.getDefaultConfig());
                zos.putNextEntry(installedFileEntry);
                copyStream(defaultConfigFile, zos);
                zos.closeEntry();
            }

            zos.close();
        } catch (Exception ioe) {
            throw ioe;
        } finally {
            if(zos != null) {
                try {
                    zos.close();
                } catch (IOException ioe) {}
            }
        }
    }

    /*
        private static void createSigned(final File outputFile, final Connector connector, final File installFile) throws Exception {
            SignedJar signedCpk = new SignedJar(new FileOutputStream(outputFile),
                    certList, publicCertificate, privateKey);

            signedCpk.addFileContents(Connector.XML_FILENAME, connector.asString().getBytes("utf-8"));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copyStream(new FileInputStream(installFile), bos);

            signedCpk.addFileContents(connector.getInstallFile(), bos.toByteArray());

            signedCpk.close();
        }

        private static void signCpk(final String inputFile, final String outputCpk) throws Exception {
            loadKeystore(signKeystore, signKeystoreType, signKeyAlias, signKeystorePassword, signKeyPassword,
                    signPublicCertificate, signPrivateKey);

            SignedJar signedCpk = new SignedJar(new FileOutputStream(outputCpk),
                    certList, publicCertificate, privateKey);

            try (JarFile inputCpkFile = new JarFile(new File(inputFile))) {
                Enumeration<JarEntry> entries = inputCpkFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (!entry.isDirectory() && !entryName.startsWith("META-INF/")) {
                        InputStream is = inputCpkFile.getInputStream(entry);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        copyStream(is, bos);

                        signedCpk.addFileContents(entryName, bos.toByteArray());
                    }
                }
            } finally {
                signedCpk.close();
            }
        }

        private static void loadKeystore(final String keystoreFile, final String keystoreType,
                                         final String keyAlias, final String keystorePassword,
                                         final String keyPassword, final String pubCert,
                                         final String priKey) throws Exception {
            if (keystoreFile != null) {
                KeyStore ks = KeyStore.getInstance(keystoreType);
                ks.load(new FileInputStream(keystoreFile), keystorePassword.toCharArray());

                String alias = keyAlias;
                if (alias == null) {
                    if (ks.size() == 1) {
                        alias = ks.aliases().nextElement();
                    } else {
                        throw new IllegalArgumentException("Key store contains multiple keys but [" + OPT_ALIAS + "] option is missing");
                    }
                }
                Key key = ks.getKey(alias, keyPassword.toCharArray());
                privateKey = (PrivateKey) key;

                publicCertificate = (X509Certificate) ks.getCertificate(keyAlias);
            } else {
                CertificateFactory fact = CertificateFactory.getInstance("X.509");
                FileInputStream certInputStream = new FileInputStream(pubCert);
                publicCertificate = (X509Certificate) fact.generateCertificate(certInputStream);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                FileInputStream keyInputStream = new FileInputStream(priKey);
                copyStream(keyInputStream, bos);

                EncryptedPrivateKeyInfo keyInfo = new EncryptedPrivateKeyInfo(bos.toByteArray());
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(keyInfo.getAlgName());
                SecretKey secretKey = secretKeyFactory.generateSecret(new PBEKeySpec(keyPassword.toCharArray()));

                Cipher cipher = Cipher.getInstance(keyInfo.getAlgName());
                cipher.init(Cipher.DECRYPT_MODE, secretKey, keyInfo.getAlgParameters());

                KeySpec keySpec = keyInfo.getKeySpec(cipher);
                if (keySpec == null) {
                    keySpec = new PKCS8EncodedKeySpec(bos.toByteArray());
                }

                try {
                    privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
                } catch (InvalidKeySpecException e) {
                    privateKey = KeyFactory.getInstance("DSA").generatePrivate(keySpec);
                }
            }

            certList = new ArrayList<>();
            certList.add(publicCertificate);
        }
    */

    public static void copyStream(final InputStream input, final OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[512 * 1024]; // or other buffer size
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            input.close();
        }
    }
}
