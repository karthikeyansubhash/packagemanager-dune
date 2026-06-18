package com.hp.jetadvantage.link.pkgmgt;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final String TAG = "[PM]";

    public static final String VND_HP_PACKAGE_INFO = "vnd.hp.package-info";
    public static final String VND_HP_CONFIG_INFO = "vnd.hp.config-info";
    public static final String VND_HP_INSTALLER_INFO = "vnd.hp.installer-info";
    public static final String VND_HP_SYSTEM_CONFIG_INFO = "vnd.hp.system-config-info";
    public static final String VND_HP_AVATAR_REGISTRATION_INFO = "vnd.hp.avatar-registration-info";
    public static final String VND_HP_PROVIDERS_INFO = "vnd.hp.providers-info";
    public static final String VND_HP_ATTESTATION_INFO = "vnd.hp.attestation-info";

    // Dialog identifiers used in showDialog
    private static final int DLG_BASE = 0;
    public static final int DLG_INSTALL = DLG_BASE + 1;
    public static final int DLG_UNINSTALL = DLG_BASE + 2;
    public static final int DLG_PACKAGE_ERROR = DLG_BASE + 3;

    public static final String DLG_ARG_APP_NAME = "appName";
    public static final String DLG_ARG_APP_VERSION = "appVersion";
    public static final String DLG_ARG_PACKAGE_NAME = "packageName";
    public static final String DLG_ARG_INSTALLER_VALUES = "installerDetails";
    public static final String DLG_ARG_ERROR_CODE = "errorCode";
    public static final String DLG_ARG_ERROR_MSG = "errorMessage";
    public static final String DLG_ARG_ERROR_OBJ = "errorObject";
    public static final String DLG_ARG_APP_TRUSTED = "trustedApp";

    public static final String PKGMGT_URI_CONTEXT = "/hp/device/webservices/ext/pkgmgt";

    // CWS URL for getting device state
    public static final String DEVICE_STATE = "/hp/device/apis/v1/deviceState/state";
    // CDM appPlatformConfiguration for getting link platform version (from hpk 2.4)
    public static final String LINK_PLATFORM_VERSION = "/hp/device/apis/deviceConfiguration/v1/appPlatformConfiguration";
    public static final String DEFAULT_LINK_PLATFORM_VERSION = "31.9";
    // auth agent
    public static final String AUTHNPROVIDERS = "/hp/device/apis/v1/authNProviders";
    // statistics agent
    public static final String STATISTICSPROVIDERS = "/hp/device/apis/v1/statisticsProviders";
    // oauth
    public static final String OAUTH = "/hp/device/apis/v1/validateResourceRoute?expand=href";


//    public static final String SOAP__UI_CONFIGURATION_SERVICE_URL = OXPdConnect.SCHEME_HTTP + "://"
//            + OXPdConnect.OXPD_HOST_INTERNAL + ":" + OXPdConnect.OXPD_PORT_INTERNAL
//            + "/hp/device/webservices/OXPd/UIConfigurationService";
//    public static final String SOAP__SYSTEM_CONFIG_SERVICE_URL = OXPdConnect.SCHEME_HTTP + "://"
//            + OXPdConnect.OXPD_HOST_INTERNAL + ":" + OXPdConnect.OXPD_PORT_INTERNAL
//            + "/systemconfiguration";


    public static final int CONFIG_DATA_MAX_SIZE = 65536; //16384; //Req HPCC

    public static final String ACTION_START_SERVER = "com.hp.jetadvantage.link.pkgmgt.server.START_SERVER";

    // Main folder for Package Manager files
    public static final File PACKAGE_MANAGER_FOLDER = new File(Environment.getExternalStorageDirectory(), "PackageManager");
    // Folder for temporary files
    public static final File TEMPORARY_FOLDER = new File(PACKAGE_MANAGER_FOLDER, "tmp");

    public static final String TRUSTED_KEYSTORE_PATH = "keystore/cacerts";
    public static final String TRUSTED_KEYSTORE_DEV_PATH = "keystore/cacertsdev";
    public static final String TRUSTED_GOLDLIST_KEYSTORE_PATH = "keystore/cacertsgold";
    public static final String TRUSTED_INTER_OEM_SEC_KEYSTORE_PATH = "keystore/secoeminter.bks";
    public static final String XSD_V2_1_PATH = "xsd/hpk_2.1.xsd";
    public static final String XSD_V2_2_PATH = "xsd/hpk_2.2.xsd";
    public static final String XSD_V2_3_PATH = "xsd/hpk_2.3.xsd";
    public static final String XSD_V2_4_PATH = "xsd/hpk_2.4.xsd";
    public static final String XSD_V2_5_PATH = "xsd/hpk_2.5.xsd";
    public static final String XSD_VERSION_2_1 = "v2.1";
    public static final String XSD_VERSION_2_2 = "v2.2";
    public static final String XSD_VERSION_2_3 = "v2.3";
    public static final String XSD_VERSION_2_4 = "v2.4";
    public static final String XSD_VERSION_2_5 = "v2.5";

    public static final double APK_INSTALL_SIZE_MULTIPLIER = 2.5;
    public static final double LOW_SPACE_WARNING_THRESHOLD = 0.2;

    public static final String CONTENT_FILTER_PARAM = "contentFilter";
    public static final String CONTENT_FILTER_ALL = "*";

    public static final String PROVIDER_TYPE_ACCESSORIES = "ACCESSORIES";
    public static final String PROVIDER_TYPE_STATISTICS = "STATISTICS";
    public static final String PROVIDER_TYPE_WEBSERVICES = "WEBSERVICES";
    public static final String PROVIDERS_AUTHN = "provider:authn";
    public static final String PROVIDERS_HOME_SCREEN = "provider:homescreen";
    public static final String PROVIDERS_ACCESSORIES = "provider:accessories";
    public static final String PROVIDERS_STATISTICS = "provider:statistics";
    public static final String PROVIDERS_GOLD = "GOLD";

    public static final String SOLUTIONS = "solutions";
    public static final String SOLUTION_PATH = "/solution";
    public static final String DUNE_INSTALLER = "dune_installer";

    public static final String PACKAGE_TAG = "package:";

    public static final String FILE_PATH = "file_path";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String MESSAGE_INSTALL_STATUS = "installStatus";
    public static final String MESSAGE_UNINSTALL_STATUS = "uninstallStatus";
    public static final String MESSAGE_SEND = "sendMessage";

    /**
     * Keys used for transporting data.
     */
    public static final class Keys {

        private Keys() {
            // Utility class
        }

        /**
         * Key to manage queue id.
         */
        public static final String KEY_QUEUE_ID = "queueId";

        /**
         * Key to download hpk file.
         */
        public static final String KEY_DOWNLOAD_URL = "downloadUrl";
    }
}
