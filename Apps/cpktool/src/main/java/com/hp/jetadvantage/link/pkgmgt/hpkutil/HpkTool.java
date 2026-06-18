package com.hp.jetadvantage.link.pkgmgt.hpkutil;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.*;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import com.hp.jetadvantage.link.pkgmgt.lib.Provider;
import com.hp.jetadvantage.link.pkgmgt.lib.SubApp;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.*;

public class HpkTool extends HpkToolApplication {
    /**
     * <p>HPKTool Main Method</p>
     * <p>{@link #processCLI}</p>
     *
     * @param args
     */
    public static void main(String[] args) {
        final PlatformType LOCAL_PLATFORM_TYPE = PlatformType.LinkForDevice;
        Constants.DEFAULT_PLATFORM_TYPE = PlatformType.LinkForDevice;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));

        try {
            launchGUI(args, LOCAL_PLATFORM_TYPE);
            processCLI(args, LOCAL_PLATFORM_TYPE);
        } catch (Throwable e) {
            exitWithError(e.getMessage());
        } finally {
            if (TEST == null) {
                exit(0);
            }
        }
    }

    /**
     * <p>Parse and execute CLI command</p>
     * <pre>
     * --create             Create HPK file from specified field values
     * --create-xml         Create hpk.xml file from specified field values (internal use only, not for public)
     * --xml                Create HPK file using existing Web application manifest file
     * --generate           Generate template for hpk.xml
     * --install            Install HPK file to device
     * --uninstall          Delete the installed application
     * --config-get         Retrieve the configuration of the installed application
     * --config-update      Update the configuration of the installed application
     * --help               Shows this help
     * --app-list           Retrieve the application list
     * --app-detail         Retrieve the details of application
     * --provider-list      Retrieve the provider list
     * --attestation-update Updating client credentials with user information for supporting App Attestation Debug Flow.
     * </pre>
     *
     * @param args
     * @param platformType
     * @throws IllegalArgumentException Wrong command, there are no command or two or more commands
     * @throws Throwable                Throw Throwable form sub methods.
     */
    private static void processCLI(String[] args, PlatformType platformType) throws Throwable {
        try {
            OptionSet cmd = getDefaultOptionParser().parse(args);
            switch (getMainCommand(cmd)) {
                case HELP:
                    printHelp(platformType);
                    break;
                case CREATE:
                    createHPK(cmd, platformType);
                    break;
                case CREATE_XML:
                    createHpkXml(cmd, platformType);
                    break;
                case XML:
                    createXML(cmd, platformType);
                    break;
                case INSTALL:
                    installHPK(cmd);
                    break;
                case UNINSTALL:
                    uninstallHPK(cmd);
                    break;
                case CONFIG_GET:
                    getAppConfig(cmd);
                    break;
                case CONFIG_UPDATE:
                    updateAppConfig(cmd);
                    break;
                case TEMPLATE:
                    HpkFileHelper.generateTemplateByPlatformType(platformType);
                    break;
                case APP_LIST:
                    getAppList(cmd);
                    break;
                case APP_DETAIL:
                    getAppDetail(cmd);
                    break;
                case ATTESTATION_UPDATE:
                    updateAttestation(cmd);
                    break;
                case PROVIDER_LIST:
                    getProviderList(cmd);
                    break;
                default:
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_main_command"));
            }
        } catch (Throwable throwable) {
            throw throwable;
        }
    }

    /**
     * <p>Create HPK file</p>
     * <p>Check before change: {@link HpkToolForWeb#createHPK}</p>
     * <pre>
     * Options: for --create:
     * --output &lt;hpk-file&gt;                                                           Output HPK package file path
     * --installfile &lt;install-file&gt;                                                  Install file path
     * --uuid &lt;uuid&gt;                                                                 UUID associated with Application (RFC 4122)
     * --name &lt;app-name&gt;                                                             Name of Application that will be shown in Application Galleries
     * --vendor &lt;vendor&gt;                                                             Name of Vendor that developed Application
     * --date &lt;yyyymmdd&gt;                                                             Date of creation package for Application (default current date)
     * --defaultconfig &lt;defaultconfig&gt;                                               The file name that included configuration
     * --subactivity1 &lt;uuid,package/Activity,(Optional)type&gt;                         Details of Sub application which is a part of parent application
     * --linkauthagent &lt;-p:name={"":""} -p:description={"":""} -p:url=package/Activity -p:prerompt=bool&gt;
     *                                                                               Details for adding Link Authentication Agent
     * --statisticsagent &lt;-s:name={"":""} -s:description={"":""} -s:ackrequiredfordelete=bool&gt;
     *                                                                               Details for adding Statistics Agent
     * --usehomescreenmode &lt;use-homescreen-mode&gt;                                     Flag to set application as home screen
     * --sethomescreenasdefault &lt;set-as-default&gt;                                     Flag to set as default homescreen for launching automatically after installation
     * --accessory1 &lt;type,vender-id,product-id,serial-number&gt;                        Details of accessories which need to be registered automatically for installation
     * --webservice &lt;-w:name={"":""} -w:description={"":""} -w:endpoint=method,category,absolutePath,authType&gt;
     *                                                                               Details of webservice
     * --platformversion &lt;platform-version&gt;                                          Target Link Platform version
     * --schemaversion &lt;schema-version&gt;                                              Schema version
     * </pre>
     *
     * @param cmd
     * @param platformType
     * @throws IllegalArgumentException
     * @throws Exception                Throw exception from sub methods.
     */
    @SuppressWarnings("JavadocReference")
    private static void createHPK(OptionSet cmd, PlatformType platformType) throws Exception {
        try {
            String outputCpk = (String) cmd.valueOf(OPT_OUTPUT);
            Connector connector = generateConnector(cmd, platformType);

            String apkPath = (String) cmd.valueOf(OPT_INSTALLFILE);
            File installFile = new File(apkPath);

            String defaultConfigPath = (String) cmd.valueOf(OPT_DEFAULT_CONFIG);
            File defaultConfigFile = null;
            if (defaultConfigPath != null && !defaultConfigPath.isEmpty()) {
                defaultConfigFile = Utils.checkDefaultConfigValidation(defaultConfigPath);
            }

            HpkFileHelper.createCpk(new File(outputCpk), connector, installFile, defaultConfigFile);
            System.out.println(Constants.MESSAGE.getString("msg_success"));
        } catch (Exception exception) {
            throw exception;
        }
    }

    /**
     * <p>Create hpk.xml file</p>
     * <p>Check before change: {@link #createHPK}</p>
     * <pre>
     * Options: for --create-xml:
     * --output &lt;hpk-file&gt;                                                           Output hkp.xml package file path
     * --installfile &lt;install-file&gt;                                                  Install file path
     * --uuid &lt;uuid&gt;                                                                 UUID associated with Application (RFC 4122)
     * --name &lt;app-name&gt;                                                             Name of Application that will be shown in Application Galleries
     * --vendor &lt;vendor&gt;                                                             Name of Vendor that developed Application
     * --date &lt;yyyymmdd&gt;                                                             Date of creation package for Application (default current date)
     * --defaultconfig &lt;defaultconfig&gt;                                               The file name that included configuration
     * --subactivity1 &lt;uuid,package/Activity,(Optional)type&gt;                         Details of Sub application which is a part of parent application
     * --linkauthagent &lt;-p:name={"":""} -p:description={"":""} -p:url=package/Activity -p:prerompt=bool&gt;
     *                                                                               Details for adding Link Authentication Agent
     * --statisticsagent &lt;-s:name={"":""} -s:description={"":""} -s:ackrequiredfordelete=bool&gt;
     *                                                                               Details for adding Statistics Agent
     * --usehomescreenmode &lt;use-homescreen-mode&gt;                                     Flag to set application as home screen
     * --sethomescreenasdefault &lt;set-as-default&gt;                                     Flag to set as default homescreen for launching automatically after installation
     * --accessory1 &lt;type,vender-id,product-id,serial-number&gt;                        Details of accessories which need to be registered automatically for installation
     * --webservice &lt;-w:name={"":""} -w:description={"":""} -w:endpoint=method,category,absolutePath,authType&gt;
     *                                                                               Details of webservice
     * --platformversion &lt;platform-version&gt;                                          Target Link Platform version
     * --schemaversion &lt;schema-version&gt;                                              Schema version
     * </pre>
     *
     * @param cmd
     * @param platformType
     * @throws IllegalArgumentException
     * @throws Exception                Throw exception from sub methods.
     */
    private static void createHpkXml(OptionSet cmd, PlatformType platformType) throws Exception {
        try {
            Connector connector = generateConnector(cmd, platformType);
            String outputFile = (String) cmd.valueOf(OPT_OUTPUT);

            try (FileOutputStream outputStream = new FileOutputStream(new File(outputFile))) {
                connector.writeTo(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            System.out.println(Constants.MESSAGE.getString("msg_success"));
        } catch (Exception exception) {
            throw exception;
        }
    }

    /**
     * Generate XML Connector from input optionSet.
     * @param cmd
     * @param platformType
     * @return Connector
     * @throws Exception
     */
    private static Connector generateConnector(OptionSet cmd, PlatformType platformType) throws Exception {
        try {
            String apkPath = (String) cmd.valueOf(OPT_INSTALLFILE);

            Connector connector = new Connector();
            connector.setPlatformType(platformType);

            if (!"apk".equalsIgnoreCase(Utils.getExtension(apkPath))) {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_INSTALLFILE + ": " + apkPath);
            }
            File installFile = new File(apkPath);
            connector.setInstallFile(installFile.getName());

            String uuid = (String) cmd.valueOf(OPT_UUID);
            try {
                UUID uuidValue = UUID.fromString(uuid);
                connector.setUuid(uuidValue);
            } catch (Exception e) {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_UUID + ": " + uuid);
            }

            String name = (String) cmd.valueOf(OPT_NAME);
            connector.setName(name);

            String vendor = (String) cmd.valueOf(OPT_VENDOR);
            connector.setVendorName(vendor);

            if (cmd.has(OPT_DATE)) {
                String date = (String) cmd.valueOf(OPT_DATE);
                try {
                    Constants.DATE_FORMAT.parse(date);
                    connector.setDate(date);
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_DATE + ": " + date);
                }
            } else {
                connector.setDate(Constants.DATE_FORMAT.format(new Date()));
            }

            String defaultConfigPath = (String) cmd.valueOf(OPT_DEFAULT_CONFIG);
            if (defaultConfigPath != null && !defaultConfigPath.isEmpty()) {
                File configFile = Utils.checkDefaultConfigValidation(defaultConfigPath);
                connector.setDefaultConfig(configFile.getName());
            }

            ArrayList<Provider> providerList = new ArrayList<>();

            HPKVersion hpkVersion = HPK_LATEST_VERSION;
            if (cmd.has(OPT_SCHEMA_VERSION)) {
                String hpkVersionValue = null;
                try {
                    hpkVersionValue = (String) cmd.valueOf(OPT_SCHEMA_VERSION);
                    hpkVersion = HPKVersion.getHPKVersion(hpkVersionValue);
                    if (hpkVersion == null) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                                + OPT_SCHEMA_VERSION + ": " + hpkVersionValue);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_SCHEMA_VERSION + ": " + hpkVersionValue);
                }
            }

            LinkPlatformVersion linkPlatformVersion = LinkPlatformVersion.getEnumByValue(DEFAULT_PLATFORM_VERSION.toString());
            if (cmd.has(OPT_PLATFORM_VERSION[0])) {
                if (hpkVersion.getLevel() < HPKVersion.HPK_1_3.getLevel()) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_PLATFORM_VERSION[0] + " -" + OPT_SCHEMA_VERSION + ": " + hpkVersion.toString());
                }
                String platformVersionValue = null;
                try {
                    platformVersionValue = (String) cmd.valueOf(OPT_PLATFORM_VERSION[0]);
                    linkPlatformVersion = LinkPlatformVersion.getEnumByValue(platformVersionValue);
                    if (linkPlatformVersion == null) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                                + OPT_PLATFORM_VERSION[0] + " or -" + OPT_PLATFORM_VERSION[1] + ": " + platformVersionValue);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_PLATFORM_VERSION[0] + " or -" + OPT_PLATFORM_VERSION[1] + ": " + platformVersionValue);
                }
                if (linkPlatformVersion.getHpkVersion().getLevel() > hpkVersion.getLevel()) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_PLATFORM_VERSION[0] + ": " + linkPlatformVersion.toString() + ", " + OPT_SCHEMA_VERSION + ": " + hpkVersion.toString());
                }
            }
            if (hpkVersion.getLevel() >= HPKVersion.HPK_1_3.getLevel()) {
                connector.setPlatformVersion(linkPlatformVersion.toString());
            }

            // Sub App
            ArrayList<SubApp> subAppList = new ArrayList<>();
            if (cmd.has(OPT_SUBACTIVITY_1)) {
                String subAppStr = (String) cmd.valueOf(OPT_SUBACTIVITY_1);
                try {
                    SubApp subApp = parseSubApp(subAppStr);
                    subAppList.add(subApp);
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_SUBACTIVITY_1 + ": " + subAppStr);
                }
            }

            if (cmd.has(OPT_SUBACTIVITY_2)) {
                String subAppStr = (String) cmd.valueOf(OPT_SUBACTIVITY_2);
                try {
                    SubApp subApp = parseSubApp(subAppStr);
                    subAppList.add(subApp);
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_SUBACTIVITY_2 + ": " + subAppStr);
                }
            }

            if (cmd.has(OPT_SUBACTIVITY_3)) {
                String subAppStr = (String) cmd.valueOf(OPT_SUBACTIVITY_3);
                try {
                    SubApp subApp = parseSubApp(subAppStr);
                    subAppList.add(subApp);
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_SUBACTIVITY_3 + ": " + subAppStr);
                }
            }

            if (cmd.has(OPT_SUBACTIVITY_4)) {
                String subAppStr = (String) cmd.valueOf(OPT_SUBACTIVITY_4);
                try {
                    SubApp subApp = parseSubApp(subAppStr);
                    subAppList.add(subApp);
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_SUBACTIVITY_4 + ": " + subAppStr);
                }
            }

            if (cmd.has(OPT_SUBACTIVITY_5)) {
                String subAppStr = (String) cmd.valueOf(OPT_SUBACTIVITY_5);
                try {
                    SubApp subApp = parseSubApp(subAppStr);
                    subAppList.add(subApp);
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_SUBACTIVITY_5 + ": " + subAppStr);
                }
            }

            if (subAppList.size() > 0) {
                connector.setSubAppList(subAppList);
            }
            boolean hasAuthAgent = false;

            // Auth Agent
            if (cmd.has(OPT_LINK_AUTH_AGENT)) {
                if (hpkVersion.getLevel() < HPKVersion.HPK_1_1.getLevel()) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_LINK_AUTH_AGENT + " -" + OPT_SCHEMA_VERSION + ": " + hpkVersion.toString());
                }
                if (subAppList.size() > 0) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("sub_app_cannot_use_other_option"));
                }
                OptionParser linkAuthAgentOptionParser = getLinkAuthAgentOptionParser();
                List<String> params = (List<String>) cmd.valuesOf(PARAM_LINK_AUTH_AGENT);

                List<String> agentOptions = new ArrayList<>();
                for (String param : params) {
                    agentOptions.add(param.replaceFirst(":", "-"));
                }
                OptionSet agentOptionSet = linkAuthAgentOptionParser.parse(agentOptions.toArray(new String[agentOptions.size()]));

                try {
                    if (!(agentOptionSet.has(PARAM_NAME) && agentOptionSet.has(PARAM_DESCRIPTION) && agentOptionSet.has(PARAM_URL))) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_LINK_AUTH_AGENT + ": " + agentOptions.toString());
                    }
                    providerList.add(parseAuthAgent(agentOptionSet));
                    hasAuthAgent = true;
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_LINK_AUTH_AGENT + ": " + agentOptions.toString());
                }
            }

            // Statistics Agent
            if (cmd.has(OPT_STATISTICS_AGENT)) {
                if (linkPlatformVersion.getHpkVersion().getLevel() < HPKVersion.HPK_1_4.getLevel()) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_STATISTICS_AGENT + " -" + OPT_PLATFORM_VERSION[0] + ": " + linkPlatformVersion.toString());
                }
                if (subAppList.size() > 0) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("sub_app_cannot_use_statistics_agent_option"));
                }
                OptionParser statisticsAgentOptionParser = getStatisticsAgentOptionParser();
                List<String> params = (List<String>) cmd.valuesOf(PARAM_STATISTICS_AGENT);

                List<String> agentOptions = new ArrayList<>();
                for (String param : params) {
                    agentOptions.add(param.replaceFirst(":", "-"));
                }
                OptionSet agentOptionSet = statisticsAgentOptionParser.parse(agentOptions.toArray(new String[agentOptions.size()]));

                try {
                    if (!(agentOptionSet.has(PARAM_NAME) && agentOptionSet.has(PARAM_DESCRIPTION))) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_STATISTICS_AGENT + ": " + agentOptions.toString());
                    }
                    providerList.add(parseStatisticsAgent(agentOptionSet));
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_STATISTICS_AGENT + ": " + agentOptions.toString());
                }
            }

            // HomeScreen
            Provider homeScreen = new Provider();
            if (cmd.has(OPT_USE_HOMESCREEN_MODE[0])) {
                String useMode = (String) cmd.valueOf(OPT_USE_HOMESCREEN_MODE[0]);

                if (!("true".equals(useMode) || "false".equals(useMode))) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_USE_HOMESCREEN_MODE[0] + " or -" + OPT_USE_HOMESCREEN_MODE[1] + ": " + useMode);
                }
                if (hpkVersion.getLevel() < HPKVersion.HPK_1_1.getLevel()) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_USE_HOMESCREEN_MODE[0] + " -" + OPT_SCHEMA_VERSION + ": " + hpkVersion.toString());
                }
                if (subAppList.size() > 0) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("sub_app_cannot_use_other_option"));
                }
                if (hasAuthAgent) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("home_screen_cannot_use_other_option"));
                }

                homeScreen.setType(Constants.PROVIDER_TYPE_HOME_SCREEN);
                homeScreen.setEnableHomeScreenMode(useMode);

                if (Boolean.valueOf(homeScreen.getEnableHomeScreenMode())) {
                    if (cmd.has(OPT_SET_HOMESCREEN_DEFAULT[0])) {
                        String setDefault = (String) cmd.valueOf(OPT_SET_HOMESCREEN_DEFAULT[0]);

                        if (!("true".equals(setDefault) || "false".equals(setDefault))) {
                            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                                    + OPT_SET_HOMESCREEN_DEFAULT[0] + " or -" + OPT_SET_HOMESCREEN_DEFAULT[1] + ": " + setDefault);
                        }
                        homeScreen.setConfigOnInstall(setDefault);
                    } else {
                        homeScreen.setConfigOnInstall("false");
                    }
                    providerList.add(homeScreen);
                }
            }

            // Accessory
            boolean hasAccessory = false;
            for (int number = 1; number <= MAX_ACCESSORIES; number++) {
                if (cmd.has(OPT_ACCESSORY + number)) {
                    if (hpkVersion.getLevel() < HPKVersion.HPK_1_2.getLevel()) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                                + OPT_ACCESSORY + number + " -" + OPT_SCHEMA_VERSION + ": " + hpkVersion);
                    }
                    String accessoryStr = (String) cmd.valueOf(OPT_ACCESSORY + number);
                    try {
                        providerList.add(parseAccessory(accessoryStr));
                    } catch (Exception e) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_ACCESSORY + number + ": " + accessoryStr);
                    }
                    hasAccessory = true;
                }
            }

            if (hasAccessory) {
                if (subAppList.size() > 0) {
                    throw new Exception(Constants.MESSAGE.getString("sub_app_cannot_use_accessory_option"));
                }
            }

            // Webservice
            if (cmd.has(OPT_WEBSERVICE)) {
                if (linkPlatformVersion.getHpkVersion().getLevel() < HPKVersion.HPK_1_4.getLevel()) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option")
                            + OPT_WEBSERVICE + " -" + OPT_SCHEMA_VERSION + ": " + hpkVersion.toString());
                }
                OptionParser webServiceOptionParser = getWebServiceOptionParser();
                List<String> params = (List<String>) cmd.valuesOf(PARAM_WEBSERVICE_AGENT);

                List<String> webserviceOptions = new ArrayList<>();
                for (String param : params) {
                    webserviceOptions.add(param.replaceFirst(":", "-"));
                }
                OptionSet webServiceOptionSet = webServiceOptionParser.parse(webserviceOptions.toArray(new String[webserviceOptions.size()]));

                try {
                    if (!(webServiceOptionSet.has(PARAM_NAME) && webServiceOptionSet.has(PARAM_DESCRIPTION) && webServiceOptionSet.has(PARAM_ENDPOINT))) {
                        throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_WEBSERVICE + ": " + webserviceOptions.toString());
                    }
                    providerList.add(parseWebService(webServiceOptionSet));
                } catch (Exception e) {
                    throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_WEBSERVICE + ": " + webserviceOptions.toString());
                }
            }

            connector.setSchemaLocation(Constants.NAMESPACE + hpkVersion.toString() + " " + Constants.XSD);
            connector.setNamespace(Constants.NAMESPACE + hpkVersion.toString());


            if (providerList.size() > 0) {
                connector.setProviders(providerList);
            }

            return connector;
        } catch (Exception exception) {
            throw exception;
        }
    }
}
