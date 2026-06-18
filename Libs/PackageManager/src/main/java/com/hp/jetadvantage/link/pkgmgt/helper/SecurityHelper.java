package com.hp.jetadvantage.link.pkgmgt.helper;

import android.content.Context;
import android.content.pm.Signature;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.hp.jetadvantage.link.pkgmgt.Constants;
import com.hp.jetadvantage.link.pkgmgt.PackageManagerApplication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SecurityHelper {
    private static final String TAG = Constants.TAG + "SecurityHelper";

    public static final String SYSTEM_SDK_PACKAGE_NAME = "com.hp.jetadvantage.link.services";
    private static final String SYSTEM_PACMAN_PACKAGE_NAME = "com.hp.jetadvantage.link.packagemanager";
    public static final String SYSTEM_DOR_PACKAGE_NAME = "com.hp.jetadvantage.link.datacollector";

    public static final String SYSTEM_SERVICE_PACKAGE_NAME = "com.hp.jetadvantage.link.system";
    public static final String SYSTEM_SVC_PACKAGE_NAME = "com.hp.jetadvantage.link.websvcmanager";
    public static final String SYSTEM_LOG_PACKAGE_NAME = "com.hp.jetadvantage.link.logdaemon";
    private static final String SDK_TEST_PACKAGE_NAME = "com.hp.jetadvantage.link";


    private static KeyStore keyStoreForGold = null;

    /**
     * Gets calling package (first package of Uid)
     *
     * @param context {@link Context}
     * @return String package name or empty string
     */
    public static String getCallingPackage(final Context context) {
        final int caller = Binder.getCallingUid();

        if (caller == 0) {
            return "";
        }

        final String[] packages = context.getPackageManager().getPackagesForUid(caller);

        return ((packages != null && packages.length > 0) ? packages[0] : "");
    }

    /**
     * Ensures caller permission for system application.
     *
     * @param context {@link Context}
     */
    public static void ensureSystemPermission(final Context context) {
        final String callingPackage = getCallingPackage(context);

        if (TextUtils.isEmpty(callingPackage)) {
            throw new SecurityException("Pacman permissions are not granted to empty packages");
        }

        try {
            ensureSystemPermission(context, callingPackage);
        } catch (Exception sue) {
            Log.e(TAG, sue.getMessage());
            throw new SecurityException("Pacman permissions are not granted");
        }
    }

    /**
     * Ensures caller permission for system application.
     *
     * @param context            {@link Context}
     * @param callingPackageName to check permission of callingPackageName
     */
    public static void ensureSystemPermission(final Context context, final String callingPackageName) throws Exception {
        ensureSystemPermission(context, callingPackageName, false);
    }

    private static void ensureSystemPermission(final Context context, final String callingPackageName, final boolean isForSystems) throws Exception {
        // do nothing for now
        if (TextUtils.isEmpty(context.getPackageName())) {
            throw new Exception("Service is not allowed");
        } else {
            String[] callingPackages = context.getPackageManager().getPackagesForUid(Binder.getCallingUid());
            if (callingPackages != null) {
                for (String pkg : callingPackages) {
                    if (SYSTEM_PACMAN_PACKAGE_NAME.equalsIgnoreCase(pkg)) {
                        return;
                    }

                    if (isForSystems) {
                        if (SYSTEM_SDK_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_DOR_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_SERVICE_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_LOG_PACKAGE_NAME.equalsIgnoreCase(pkg)) { //TRUE
                            return;
                        }
                    }
                }
                throw new SecurityException("Service is not allowed for unknown callers");
            }
            Log.d(TAG, "Caller is unknown, access is restricted");
            throw new SecurityException("Service is not allowed for unknown callers");
        }
    }

    public static void checkCallingPackage(final Context context) {
        checkCallingPackage(context, false);
    }

    public static void checkCallingPackageForLink(final Context context) {
        checkCallingPackage(context, true);
    }

    private static void checkCallingPackage(final Context context, final boolean isForSystemApp) {
        if (context != null && context.getPackageManager() != null) {
            String[] callingPackages = context.getPackageManager().getPackagesForUid(Binder.getCallingUid());
            if (callingPackages != null) {
                String pacManAppPackage = context.getPackageName();
                for (String pkg : callingPackages) {
                    if (SYSTEM_PACMAN_PACKAGE_NAME.equalsIgnoreCase(pkg) || pkg.equals(pacManAppPackage)) {
                        // it's PacMan, access granted
                        Log.d(TAG, "Requested");
                        return;
                    }

                    if (isForSystemApp) {
                        if (SYSTEM_SDK_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_DOR_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_SERVICE_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_SVC_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                SYSTEM_LOG_PACKAGE_NAME.equalsIgnoreCase(pkg) ||
                                // TODO: This is a temporary solution to allow SDK test code to run in production
                                // for testing SDK test code but we have to check Production and Development execution mode.
                                pkg.startsWith(SDK_TEST_PACKAGE_NAME)
                        ) {
                            return;
                        }
                    }
                }
                throw new SecurityException("Modify is not allowed for unknown callers");
            }
            Log.w(TAG, "Caller is unknown, access is restricted");
            throw new SecurityException("Modify is not allowed for unknown callers");
        } else {
            Log.w(TAG, "PackageManager is not available");
            throw new IllegalArgumentException("Context or PackageManager are not available");
        }
    }

    /**
     * Build an Authorization header
     *
     * @param username Username to login in as
     * @param password Password associated with username
     * @return HttpHeader encoded with user credentials
     */
    public static String buildAuthorizationHeader(String username, String password) {
        StringBuilder builder = new StringBuilder("Basic");
        builder.append(' ');

        if (password == null) password = "";

        try {
            builder.append(Base64.encodeToString(String.format(Locale.US, "%s:%s", username, password).getBytes("UTF-8"), Base64.URL_SAFE | Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to build authorization header" + e.getMessage(), e);
        }

        return builder.toString();
    }

    public static boolean isValidPlatformVersion(String platformVersion) throws Exception {
        boolean result = false;

        String currentPlatformVersion = Constants.DEFAULT_LINK_PLATFORM_VERSION;

        String[] subCurrentPlatformVersion = currentPlatformVersion.split("\\.");
        if (subCurrentPlatformVersion.length != 2) {
            Log.e(TAG, "invalid current information : " + subCurrentPlatformVersion);
            throw new InvalidParameterException("Current PlatformVersion is not valid.");
        }

        if (TextUtils.isEmpty(platformVersion)) {
            throw new NullPointerException("PlatformVersion is null.");
        }
        if (!platformVersion.matches("^\\d+\\.\\d+$")) {
            Log.e(TAG, "invalid information : " + platformVersion);
            throw new InvalidParameterException("PlatformVersion format is not valid.");
        }
        String[] sub = platformVersion.split("\\.");
        if (sub.length != 2) {
            Log.e(TAG, "invalid information : " + platformVersion);
            throw new InvalidParameterException("PlatformVersion format is not valid 01.");
        }
        if (Integer.valueOf(sub[0]) > Integer.valueOf(subCurrentPlatformVersion[0])) {
            Log.e(TAG, "invalid sub1 target: " + sub[0]);
            throw new InvalidParameterException("PlatformVersion format is not valid 02.");
        }
        if (Integer.valueOf(sub[1]) > Integer.valueOf(subCurrentPlatformVersion[1])) {
            Log.e(TAG, "invalid sub2 target: " + sub[1]);
            throw new InvalidParameterException("PlatformVersion format is not valid 03.");
        }
        if (Integer.valueOf(sub[0]) == 19 && Integer.valueOf(sub[1]) > 4) {
            Log.e(TAG, "invalid target for kitkat");
            throw new InvalidParameterException("PlatformVersion format is not valid 04.");
        }
        return true;
    }

    public static boolean isGoldListed(final Signature[] signatures, String filePath) {
        if (signatures == null || signatures.length == 0) {
            Log.w(TAG, "Gold Failed to get sig");
            return false;
        }

        try {
            //for (final Signature signature: signatures) {
            Signature signature = signatures[0];
            final byte[] rawCert = signature.toByteArray();
            InputStream certStream = new ByteArrayInputStream(rawCert);
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            X509Certificate x509Cert;
            try {
                x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);
            } catch (CertificateException e) {
                Log.w(TAG, "Gold list is not X.509 " + e.getMessage());
                return false;
            } finally {
                try {
                    certStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close stream" + e.getMessage(), e);
                }
            }
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();

            Set<X509Certificate> trustedCerts = getGoldListCerts();

            verifyCertificate(x509Cert, trustedCerts, intermediateCerts, filePath);
            Log.d(TAG, "Gold Certificate chain (" + signatures.length + ") is trusted");

            return true;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Gold Certificate chain is not trusted or cannot be verified " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "Gold Failed to load trusted keystore" + e.getMessage(), e);
        }

        Log.d(TAG, "Gold Certificate chain (" + signatures.length + ") is NOT trusted");

        return false;
    }

    private static Set<X509Certificate> getGoldListCerts() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {
        Set<X509Certificate> trustedCerts = new HashSet<X509Certificate>();

        KeyStore keystore = getKeyStoreForGold();

        Enumeration en = keystore.aliases();
        while (en.hasMoreElements()) {
            String ali = (String) en.nextElement();
            if (keystore.isCertificateEntry(ali)) {
                X509Certificate storecert = (X509Certificate) keystore.getCertificate(ali);
                trustedCerts.add(storecert);
            }
        }

        Log.d(TAG, "Found CA certificates for gold: " + trustedCerts.size());

        return trustedCerts;
    }

    private static synchronized KeyStore getKeyStoreForGold() {
        if (keyStoreForGold == null) {
            InputStream in = null;
            try {
                in = PackageManagerApplication.getAppContext().getAssets().open(Constants.TRUSTED_GOLDLIST_KEYSTORE_PATH);

                keyStoreForGold = KeyStore.getInstance("BKS");
                keyStoreForGold.load(in, "password".toCharArray());
            } catch (Exception e) {
                Log.e(TAG, "Failed to load keystore gold", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close stream", e);
                    }
                }
            }
        }

        return keyStoreForGold;
    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates (trust anchors) and a
     * set of intermediate certificates (to be used as part of the chain).
     *
     * @param certificate       - certificate chain for validation
     * @param trustedRootCerts  - set of trusted root CA certificates
     * @param intermediateCerts - set of intermediate certificates
     * @throws GeneralSecurityException - if the verification is not successful (e.g. certification
     *                                  path cannot be built or some certificate in the chain is
     *                                  expired)
     */
    private static void verifyCertificate(
            X509Certificate certificate, Set<X509Certificate> trustedRootCerts,
            Set<X509Certificate> intermediateCerts, String hpkFile) throws GeneralSecurityException {

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance("PKIX");

        // Create the trust anchors (set of root CA certificates)
        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for (X509Certificate trustedRootCert : trustedRootCerts) {
            trustAnchors.add(new TrustAnchor(trustedRootCert, null));
        }

        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        pkixParameters.setRevocationEnabled(false);

        // Specify a list of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCerts));
        pkixParameters.addCertStore(intermediateCertStore);
        pkixParameters.addCertPathChecker(new PKIXCertPathChecker() {
            @Override
            public void init(boolean forward) throws CertPathValidatorException {
                // ignore
            }

            @Override
            public boolean isForwardCheckingSupported() {
                return true;
            }

            @Override
            public Set<String> getSupportedExtensions() {
                return null;
            }

            @Override
            public void check(Certificate cert, Collection<String> unresolvedCritExts) throws CertPathValidatorException {
                Log.d(TAG, "Certificate " + cert.getType() + " Unsupported Extensions: " + unresolvedCritExts);
                unresolvedCritExts.remove("2.5.29.37");
            }
        });

        List<X509Certificate> certificates = new ArrayList<X509Certificate>();
        certificates.add(certificate);

        CertPath cp = certificateFactory.generateCertPath(certificates);
        try {
            validator.validate(cp, pkixParameters);
        } catch (CertPathValidatorException validatorException) {
            if (validatorException.getCause() instanceof java.security.cert.CertificateExpiredException) {
                Log.d(TAG, "Start timestamp");
                JarFile inputFile = null;
                try {
                    inputFile = new JarFile(new File(hpkFile));
                    Enumeration<JarEntry> entries = inputFile.entries();
                    String entryName = "";
                    boolean entryVerifiedFlag = false;
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        entryName = entry.getName();
                        if (!entry.isDirectory() &&
                                (entryName.endsWith(".MF")
                                        || entryName.endsWith(".SF")
                                        || entryName.endsWith(".RSA")
                                        || entryName.endsWith(".DSA")
                                        || entryName.endsWith(".EC"))) {
                            Log.d(TAG, "Request to check timestamp " + entry.getName());
                            entryVerifiedFlag = verifyEntryWithTimeStamp(entry, certificate);
                            break;
                        }
                    }
                    if (!entryVerifiedFlag) {
                        throw validatorException;
                    }
                } catch (IOException ioe) {
                    throw validatorException;
                } finally {
                    if (inputFile != null) {
                        try {
                            inputFile.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
            } else {
                throw validatorException;
            }
        }
    }

    public static boolean verifyEntryWithTimeStamp(JarEntry entry, Certificate certificate) {
        if (certificate != null && certificate instanceof X509Certificate) {
            X509Certificate x509Certificate = (X509Certificate) certificate;
            Log.d(TAG, "Check timestamp:" + x509Certificate.getNotAfter() + ", " + x509Certificate.getNotBefore());
            if (entry.getTime() >= x509Certificate.getNotBefore().getTime() && entry.getTime() <= x509Certificate.getNotAfter().getTime()) {
                return true;
            }
        }
        return false;
    }
}
