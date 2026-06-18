package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.HPKVersion;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.LinkPlatformVersion;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class Constants {
    public static final String TOOL_NAME = "<HPK Command line Tool-file>";
    public static final String TOOL_BUILD_DATE = "20231122";
    public static final String TOOL_VERSION = "1.6.2.1";
    public static final String NAMESPACE = "http://www.hp.com/schemas/jetadvantage/link/hpk/";
    public static final String XSD = "hpk.xsd";

    public static final HPKVersion HPK_LATEST_VERSION = HPKVersion.HPK_1_4;
    public static final LinkPlatformVersion LATEST_PLATFORM_VERSION = LinkPlatformVersion.LINK_PLATFORM_31_8;

    public static final String DEFAULT_EXTENSION = "hpk";

    public static final String DEFAULT_SCHEME = "https://";
    public static final int DEFAULT_PORT = 443;
    public static final int EXTERNAL_PORT = 7627;
    public static final String DEFAULT_PKGMGT_URI = "/hp/device/webservices/ext/pkgmgt";
    public static final String DEFAULT_CONFIG_URI = "/defaultConfig";
    public static final String DEFAULT_PACKAGES = "/packages";
    public static final String DEFAULT_DEVICE_CONFIGURATION = "/hp/device/apis/deviceConfiguration/v1/appPlatformConfiguration";
    public static final String DEFAULT_CONFIGURATION_SERVICE = "/hp/device/webservices/OXPd/UIConfigurationService";
    public static final String DEFAULT_SYSTEM_CONFIGURATION = "/systemconfiguration";
    public static final String DEFAULT_PROVIDERS = "/providers";

    public static final String HPK_BUTTON_XSD_FILE_PATH = "/hpk_button.xsd";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static String DEFAULT_USER_NAME = "admin";
    public static String DEFAULT_USER_PASSWORD = "";
    public static String DEFAULT_HOST = "";
    public static String DEFAULT_UUID = "";
    public static DeviceMode DEFAULT_DEVICE_MODE = DeviceMode.LINKFORWEB;
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static File DEFAULT_HPK = null;

    public static final int SCREEN_MIN_WIDTH = 655;
    public static final int SCREEN_MIN_HEIGHT = 670;

    public static final int SCREEN_INSTALL_MIN_HEIGHT = 200;
    public static final int SCROLL_PANE_MAX_HEIGHT = 700;
    public static final int DIALOG_MIN_WIDTH = 400;

    public static final String TEMP_INSTALL_FILE_NAME = "installFile";
    public static final String LAUNGUAGE_RESOURCE = "LangBundle";

    public static final String KEY_SET_AS_HOME = "setAsHome";
    public static final String KEY_CONFIG_ON_INSTALL = "configOnInstall";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.US);
    public static final Pattern DATE_REGEX = Pattern.compile("\\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])");

    public static final ResourceBundle MESSAGE = ResourceBundle.getBundle(Constants.LAUNGUAGE_RESOURCE, Constants.DEFAULT_LOCALE);
    public static PlatformType DEFAULT_PLATFORM_TYPE = PlatformType.LinkForDevice;
    public static LinkPlatformVersion DEFAULT_PLATFORM_VERSION = LinkPlatformVersion.LINK_PLATFORM_31_8;
    public static String TEST = System.getenv("TEST");

    // Command line
    public static final Pattern SEMVER_FORMAT = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.)?(\\d*)(\\.|-|\\+)?([0-9A-Za-z-.]*)?");

    public static final String NEWLINE = System.lineSeparator();

    public static final String CMD_HELP = "help";
    public static final String CMD_CREATE = "create";
    public static final String CMD_CREATE_XML = "create-xml";
    public static final String CMD_XML = "xml";
    public static final String CMD_INSTALL = "install";
    public static final String CMD_UNINSTALL = "uninstall";
    public static final String CMD_CONFIG_GET = "config-get";
    public static final String CMD_CONFIG_UPDATE = "config-update";
    //private static final String CMD_SIGN = "sign";
    public static final String CMD_TEMPLATE = "generate";
    public static final String CMD_APP_LIST = "app-list";
    public static final String CMD_APP_DETAIL = "app-detail";
    public static final String CMD_ATTESTATION_UPDATE = "attestation-update";
    public static final String CMD_PROVIDER_LIST = "provider-list";

    public static final String OPT_OUTPUT = "output";
    public static final String OPT_CONNECTOR = "manifest";
    public static final String OPT_INSTALLFILE = "installfile";
    public static final String OPT_FORCEINSTASLL = "force";
    public static final String OPT_DEFAULT_CONFIG = "defaultconfig";
    public static final String OPT_UUID = "uuid";
    public static final String OPT_NAME = "name";
    public static final String OPT_VENDOR = "vendor";
    public static final String OPT_DATE = "date";
    public static final String OPT_HOST = "host";
    public static final String OPT_PASSWORD = "password";
    public static final String OPT_DATA = "data";
    public static final String OPT_NON_LINK = "nonLink";
    public static final String OPT_USERNAME = "username";
    public static final String OPT_LDB_KEY = "ldbkey";
    public static final String OPT_CREDENTIALS = "credentials";
    public static final String OPT_COMMAND_LOCATION = "cmdloc";

    public static final String OPT_SUBACTIVITY_1 = "subactivity1";
    public static final String OPT_SUBACTIVITY_2 = "subactivity2";
    public static final String OPT_SUBACTIVITY_3 = "subactivity3";
    public static final String OPT_SUBACTIVITY_4 = "subactivity4";
    public static final String OPT_SUBACTIVITY_5 = "subactivity5";

    public static final String OPT_LINK_AUTH_AGENT = "linkauthagent";
    public static final String OPT_STATISTICS_AGENT = "statisticsagent";
    public static final String[] OPT_USE_HOMESCREEN_MODE = {"usehomescreenmode", "home"}; //Do not delete it.
    public static final String[] OPT_SET_HOMESCREEN_DEFAULT = {"sethomescreenasdefault", "default"}; //Do not delete it.
    public static final String OPT_ACCESSORY = "accessory";
    public static final String OPT_WEBSERVICE = "webservice";
    public static final String[] OPT_PLATFORM_VERSION = {"platformversion", "target"}; //Do not delete it.
    public static final String OPT_SCHEMA_VERSION = "schemaversion"; //Do not delete it.

    public static final String PARAM_LINK_AUTH_AGENT = "p";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_URL = "url";
    public static final String PARAM_PREPROMPT = "preprompt";
    public static final String PARAM_STATISTICS_AGENT = "s";
    public static final String PARAM_ACK_REQUIRED_FOR_DELETE = "ackrequiredfordelete";
    public static final String PARAM_ENDPOINT = "endpoint";
    public static final String PARAM_WEBSERVICE_AGENT = "w";

    public static final String CONTENTFILTER_ALL = "?contentFilter=*";

    public static final String EN_US = "en-US";
    public static final String WJ1DOT5 = "WJ1.5";
    public static final String WJ2DOT7 = "WJ2.7";
    public static final String OMNI90 = "Omni:90x90";
    public static final String OMNI140 = "Omni:140x140";
    public static final String OMNI179 = "Omni:179x179";

    public static final int ICON_LEN_WJ27 = 46;
    public static final int ICON_LEN_WJ15 = 66;
    public static final int ICON_LEN_OMNI90 = 90;
    public static final int ICON_LEN_OMNI140 = 140;
    public static final int ICON_LEN_OMNI179 = 179;
    public static final int ICON_MAXIMUM_SIZE = 65535;

    public static final String BTN_WJ27 = "btnWJ2dot7Icon";
    public static final String BTN_WJ15 = "btnWJ1dot5Icon";
    public static final String BTN_OMNI90 = "btnOmni90Icon";
    public static final String BTN_OMNI140 = "btnOmni140Icon";
    public static final String BTN_OMNI179 = "btnOmni179Icon";

    public static final int IMAGE_MARGIN = 2;
    public static final int MAX_ACCESSORIES = 10;

    public static final String PROVIDER_TYPE_AUTHN = "AUTHN";
    public static final String PROVIDER_TYPE_STATISTICS = "STATISTICS";
    public static final String PROVIDER_TYPE_HOME_SCREEN = "HOMESCREEN";
    public static final String PROVIDER_TYPE_ACCESSORIES = "ACCESSORIES";
    public static final String PROVIDER_TYPE_WEBSERVICES = "WEBSERVICES";

    public static final String PROPERTY_WEBSERVICE_METHOD = "method";
    public static final String PROPERTY_WEBSERVICE_CATEGORY = "category";
    public static final String PROPERTY_WEBSERVICE_ABSOLUTEPATH = "absolutePath";
    public static final String PROPERTY_WEBSERVICE_AUTHTYPE = "authtype";

    public static HPKVersion getHPKVersion() {
        Preferences pref = Preferences.userNodeForPackage(Constants.class);
        String hpkVersion = pref.get(Constants.HPK_LATEST_VERSION.toString(), Constants.HPK_LATEST_VERSION.toString());
        return HPKVersion.getHPKVersion(hpkVersion);
    }

    public static void setHPKVersion(HPKVersion hpkVersion) {
        if (hpkVersion != null) {
            Preferences pref = Preferences.userNodeForPackage(Constants.class);
            pref.put(Constants.HPK_LATEST_VERSION.toString(), hpkVersion.toString());
        }
    }

    public static LinkPlatformVersion getPlatformVersion() {
        Preferences pref = Preferences.userNodeForPackage(Constants.class);
        String platformVersion = pref.get(Constants.LATEST_PLATFORM_VERSION.toString(), Constants.LATEST_PLATFORM_VERSION.toString());
        return LinkPlatformVersion.getEnumByValue(platformVersion);
    }

    public static void setPlatformVersion(LinkPlatformVersion platformVersion) {
        if (platformVersion != null) {
            Preferences pref = Preferences.userNodeForPackage(Constants.class);
            pref.put(Constants.LATEST_PLATFORM_VERSION.toString(), platformVersion.toString());
        }
    }
}
