package com.hp.jetadvantage.link.pkgmgt.hpkutil;

import com.google.gson.*;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.application.AppInfoGetService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.application.ProviderInfoGetService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.attestation.AttestationUpdateService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration.ConfigGetService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration.ConfigUpdateService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.InstallService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.UninstallService;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.*;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.*;
import com.hp.jetadvantage.link.pkgmgt.lib.*;

import com.hp.jetadvantage.link.pkgmgt.lib.LocalizedString;
import javafx.application.Application;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.IOException;
import java.lang.Error;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.*;

public class HpkToolApplication {

    protected static final OptionParser commandsParser = new OptionParser();
    protected static final OptionParser createParser = new OptionParser();
    protected static final OptionParser xmlParser = new OptionParser();
    protected static final OptionParser installParser = new OptionParser();
    protected static final OptionParser uninstallParser = new OptionParser();
    protected static final OptionParser configGetParser = new OptionParser();
    protected static final OptionParser configUpdateParser = new OptionParser();
    protected static final OptionParser appListParser = new OptionParser();
    protected static final OptionParser appDetailParser = new OptionParser();
    protected static final OptionParser providerListParser = new OptionParser();
    protected static final OptionParser attestationParser = new OptionParser();
    //private static final OptionParser signParser = new OptionParser();

    /**
     * <p>Launch HPK Tool GUI</p>
     * <p>If args is empty and UI type is not console launch GUI.<br>
     * If any exception occurs (JavaFX not found or not supported), show help.</p>
     * @param args
     * @param platformType
     * @throws Exception bypass exception from {@link #printHelp}
     */
    protected static void launchGUI(String[] args, PlatformType platformType) throws Exception{
        if (args.length == 0) {
            if (!"console".equals(System.getProperty("ui.type"))) {
                try {
                    //JavaFX should be used only here for this class.
                    Application.launch(JavaFxApplication.class);
                    System.exit(0);
                    return;
                } catch (Throwable e) {
                    System.out.println("Error: Failed to launch GUI program - " + e.getClass().getName() + ": " + e.getMessage());
                    System.out.println();
                }
            }
            try {
                getDefaultOptionParser();
                printHelp(platformType);
                System.exit(0);
                return;
            } catch (Exception exception){
                throw exception;
            }
        }
    }

    protected enum CliMainCmd{
        NULL,
        ERR,
        HELP,
        CREATE,
        XML,
        INSTALL,
        UNINSTALL,
        CONFIG_GET,
        CONFIG_UPDATE,
        TEMPLATE,
        APP_LIST,
        APP_DETAIL,
        ATTESTATION_UPDATE,
        PROVIDER_LIST,
        CREATE_XML
    }

    /**
     * <p>Get Main Command</p>
     * <p>Currently, this function's exception is useless because parser make exception with illegal commands.</p>
     * @param cmd
     * @return Return CliMainCmd type. <br>
     * If no main command return NULL.<br>
     * If 2 or more main command return ERR.
     */
    protected static CliMainCmd getMainCommand(OptionSet cmd) {
        CliMainCmd ret = CliMainCmd.NULL;
        if (cmd.has(CMD_HELP)) {
            ret = CliMainCmd.HELP;
        }
        if (cmd.has(CMD_CREATE)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.CREATE : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_CREATE_XML)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.CREATE_XML : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_XML)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.XML : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_INSTALL)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.INSTALL : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_UNINSTALL)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.UNINSTALL : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_CONFIG_GET)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.CONFIG_GET : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_CONFIG_UPDATE)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.CONFIG_UPDATE : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_TEMPLATE)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.TEMPLATE : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_APP_LIST)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.APP_LIST : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_APP_DETAIL)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.APP_DETAIL : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_ATTESTATION_UPDATE)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.ATTESTATION_UPDATE : CliMainCmd.ERR;
        }
        if (cmd.has(CMD_PROVIDER_LIST)) {
            ret = ret == CliMainCmd.NULL ? CliMainCmd.PROVIDER_LIST : CliMainCmd.ERR;
        }
        return ret;
    }

    /**
     * <p>Create HPK file with xml data.</p>
     * <pre>
     * Options: for --xml:
     * --output &lt;hpk-file&gt;                Output HPK package file path
     * --manifest &lt;xml-file&gt;              App XML file path
     * --installfile &lt;install-file&gt;       Install file path
     * --defaultconfig &lt;defaultconfig&gt;    The file name that included configuration
     * </pre>
     * If manifest file has different or empty default config file name overwrite.
     * If manifest file has default config file name, but can't find default config file from system, throw exception.
     * @param cmd
     * @param platformType
     * @throws IllegalArgumentException
     * @throws Exception Throw exception from sub method.
     */
    protected static void createXML(OptionSet cmd, PlatformType platformType) throws Exception {
        try {
            String outputCpk = (String) cmd.valueOf(OPT_OUTPUT);
            String installFilePath = (String) cmd.valueOf(OPT_INSTALLFILE);
            String connectorPath = (String) cmd.valueOf(OPT_CONNECTOR);
            String defaultConfigPath = (String) cmd.valueOf(OPT_DEFAULT_CONFIG);
            if (!"apk".equalsIgnoreCase(Utils.getExtension(installFilePath))) {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_INSTALLFILE + ": " + installFilePath);
            }

            File installFile = new File(installFilePath);
            Connector connector = HpkFileHelper.SERIALIZER.read(Connector.class, new File(connectorPath));
            connector.setInstallFile(installFile.getName());

            String nameSpace = connector.getSchemaLocation().split(" ")[0];
            if (nameSpace.isEmpty()) {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_CONNECTOR + ": " + connectorPath);
            }
            connector.setNamespace(nameSpace);

            File defaultConfigFile = null;
            if (PlatformType.LinkForWeb.equals(platformType)) {
                connector.setPlatformType(platformType);
            } else {
                if (defaultConfigPath != null && !defaultConfigPath.isEmpty()) {
                    defaultConfigFile = Utils.checkDefaultConfigValidation(defaultConfigPath);
                    connector.setDefaultConfig(defaultConfigFile.getName());
                } else if (connector.getDefaultConfig() != null && !connector.getDefaultConfig().isEmpty()) {
                    defaultConfigFile = Utils.checkDefaultConfigValidation(connector.getDefaultConfig());
                    connector.setDefaultConfig(defaultConfigFile.getName());
                }
            }

            HpkFileHelper.createCpk(new File(outputCpk), connector, installFile, defaultConfigFile);
            System.out.println(Constants.MESSAGE.getString("msg_success"));
        } catch (Exception exception) {
            throw exception;
        }
    }

    /**
     * <p>Install HPK file.</p>
     * <pre>
     * Options: for --install:
     * --installfile &lt;install-file&lt;  Install file path
     * --host &lt;host&lt;                 Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&lt;         Password for admin account (default empty)
     * --force                       Force Install options enable (default false)
     * </pre>
     * @param cmd
     * @throws IllegalArgumentException
     */
    protected static void installHPK(OptionSet cmd) throws IllegalArgumentException {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);
        String hpkPath = (String) cmd.valueOf(OPT_INSTALLFILE);
        Boolean forceInstall = Boolean.FALSE;
        if (cmd.has(OPT_FORCEINSTASLL)) {
            forceInstall = Boolean.TRUE;
        }
        File installFile = new File(hpkPath);
        if (!installFile.exists()) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_file_not_exist") + hpkPath);
        }

        InstallService installService = new InstallService(taskInterface);
        installService.setCurrentFile(installFile);
        installService.setAccount(Constants.DEFAULT_USER_NAME, password != null ? password : "");
        installService.setHost(host);
        installService.setOptions(forceInstall);
        installService.execute();
    }

    /**
     * <p>Unsintall App with UUID.</p>
     * <pre>
     * Options: for --uninstall:
     * --uuid &lt;uuid&gt;          UUID associated with Application (RFC 4122)
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * </pre>
     * @param cmd
     * @throws IllegalArgumentException
     */
    protected static void uninstallHPK(OptionSet cmd) throws IllegalArgumentException {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);

        UninstallService uninstallService = new UninstallService(taskInterface);

        String uuid = (String) cmd.valueOf(OPT_UUID);
        try {
            UUID uuidValue = UUID.fromString(uuid);
            uninstallService.setUuid(uuidValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_UUID + ": " + uuid);
        }

        uninstallService.setHost(host);
        uninstallService.setAccount(Constants.DEFAULT_USER_NAME, password != null ? password : "");
        uninstallService.execute();
    }


    /**
     * <p>Get App Configuration with UUID.</p>
     * <pre>
     * Options: for --config-get:
     * --uuid &lt;uuid&gt;          UUID associated with Application (RFC 4122)
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * </pre>
     * @param cmd
     * @throws IllegalArgumentException
     */
    protected static void getAppConfig(OptionSet cmd) throws IllegalArgumentException {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);
        Constants.DEFAULT_USER_PASSWORD = password != null ? password : "";

        String uuid = (String) cmd.valueOf(OPT_UUID);
        UUID uuidValue;
        try {
            uuidValue = UUID.fromString(uuid);
        } catch (Exception e) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_UUID + ": " + uuid);
        }

        ConfigGetService configGetService = new ConfigGetService(host, uuidValue, taskInterface);
        configGetService.execute();
    }

    /**
     * <p>Update App Configuration with UUID and JSON data.</p>
     * <pre>
     * Options: for --config-update:
     * --uuid &lt;uuid&gt;          UUID associated with Application (RFC 4122)
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * --data &lt;data&gt;          Application configuration data in JSON
     * </pre>
     * @param cmd
     * @throws IllegalArgumentException
     * @throws JsonSyntaxException Data error.
     */
    protected static void updateAppConfig(OptionSet cmd) throws IllegalArgumentException, JsonSyntaxException {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);

        Configuration configuration = new Configuration();
        Constants.DEFAULT_USER_PASSWORD = password != null ? password : "";

        String uuid = (String) cmd.valueOf(OPT_UUID);
        UUID uuidValue;
        try {
            uuidValue = UUID.fromString(uuid);
            configuration.setUuid(uuidValue.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_UUID + ": " + uuid);
        }

        String data = (String) cmd.valueOf(OPT_DATA);
        if (!Utils.isValidJSON(data)) {
            throw new JsonSyntaxException(Constants.MESSAGE.getString("msg_json_invalid_error") + data);
        }

        JsonParser parser = new JsonParser();
        JsonObject configurationData = parser.parse(data).getAsJsonObject();
        configuration.setData(configurationData);

        ConfigUpdateService configUpdateService = new ConfigUpdateService(host, uuidValue, configuration, taskInterface);
        configUpdateService.execute();
    }

    /**
     * <p>Get App List.</p>
     * <pre>
     * Options: for --app-list
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * </pre>
     * @param cmd
     */
    protected static void getAppList(OptionSet cmd) {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);

        Constants.DEFAULT_USER_PASSWORD = password != null ? password : "";
        AppInfoGetService appInfoGetService = new AppInfoGetService(host, getListTaskInterface);
        appInfoGetService.execute();
    }

    /**
     * <p>Get Details of App.</p>
     * <pre>
     * Options: for --app-detail
     * --uuid &lt;uuid&gt;          UUID associated with Application (RFC 4122)
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * </pre>
     * @param cmd
     * @throws IllegalArgumentException
     */
    protected static void getAppDetail(OptionSet cmd) throws IllegalArgumentException {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);
        Constants.DEFAULT_USER_PASSWORD = password != null ? password : "";

        String uuid = (String) cmd.valueOf(OPT_UUID);
        UUID uuidValue;
        try {
            uuidValue = UUID.fromString(uuid);
        } catch (Exception e) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_UUID + ": " + uuid);
        }

        AppInfoGetService appInfoGetService = new AppInfoGetService(host, uuidValue, getDetailTaskInterface);
        appInfoGetService.execute();
    }

    /**
     * <p>Update Client Credentials with User Information for Supporting App Attestation Debug Flow.<br>
     * This feature need adb(Android Debug Bridge).</p>
     * <pre>
     * Options: for --attestation-update
     * --uuid &lt;uuid&gt;          UUID associated with Application (RFC 4122)
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * --username &lt;username&gt;        A user name on your user profile at HP.io developer portal.
     * --ldbkey &lt;ldbkey&gt;            A password (User profile at HP.io developer portal > Service Keys Tab)
     * --credentials &lt;credentials&gt;  JSON array containing all the client credentials.
     * </pre>
     * @param cmd
     */
    protected static void updateAttestation(OptionSet cmd) {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);
        Constants.DEFAULT_USER_PASSWORD = password != null ? password : "";

        String uuid = (String) cmd.valueOf(OPT_UUID);
        UUID uuidValue;
        try {
            uuidValue = UUID.fromString(uuid);
        } catch (Exception e) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_UUID + ": " + uuid);
        }

        String username = (String) cmd.valueOf(OPT_USERNAME);
        String ldbKey = (String) cmd.valueOf(OPT_LDB_KEY);
        String credentials = (String) cmd.valueOf(OPT_CREDENTIALS);

        if (!Utils.isValidJSONArray(credentials)) {
            throw new JsonSyntaxException(Constants.MESSAGE.getString("msg_json_invalid_error") + credentials);
        }

        File command_location = null;
        if (cmd.has(OPT_COMMAND_LOCATION)) {
            command_location = new File((String) cmd.valueOf(OPT_COMMAND_LOCATION));
        }

        Attestation attestation = new Attestation();

        JsonParser parser = new JsonParser();
        JsonArray clientsInfo = parser.parse(credentials).getAsJsonArray();

        attestation.setHost(host);
        attestation.setUuid(uuid);
        attestation.setUser(username);
        attestation.setKey(ldbKey);
        attestation.setData(clientsInfo);

        AttestationUpdateService attestationUpdateService = new AttestationUpdateService(
                host, uuidValue, attestation, command_location, taskInterface);
        attestationUpdateService.execute();
    }

    /**
     * <p>Get Provider List.</p>
     * <pre>
     * Options: for --provider-list
     * --host &lt;host&gt;          Device IP Address (ex. 192.168.1.1)
     * --password &lt;password&gt;  Password for admin account (default empty)
     * </pre>
     * @param cmd
     */
    protected static void getProviderList(OptionSet cmd) {
        String host = (String) cmd.valueOf(OPT_HOST);
        String password = (String) cmd.valueOf(OPT_PASSWORD);
        Constants.DEFAULT_USER_PASSWORD = password != null ? password : "";

        ProviderInfoGetService providerInfoGetService = new ProviderInfoGetService(host, getProvidersTaskInterface);
        providerInfoGetService.execute();
    }

    /**
     * <p>Sign CPK</p>
     * <p>Not used method. Old code from refactoring.</p>
     * @param cmd
     * @throws Exception
     */
    /*
    protected static void signCPK(OptionSet cmd) throws Exception {
        if (cmd.has(OPT_KEYSTORE)) {
            signKeystore = (String) cmd.valueOf(OPT_KEYSTORE);
            if (cmd.has(OPT_STORETYPE)) {
                signKeystoreType = (String) cmd.valueOf(OPT_STORETYPE);
            } else {
                signKeystoreType = KeyStore.getDefaultType();
            }
            signKeystorePassword = (String) cmd.valueOf(OPT_STOREPASSWD);
            signKeyAlias = (String) cmd.valueOf(OPT_ALIAS);
            if (cmd.has(OPT_KEYPASSWD)) {
                signKeyPassword = (String) cmd.valueOf(OPT_KEYPASSWD);
            } else {
                signKeyPassword = signKeystorePassword;
            }
        } else {
            signPublicCertificate = (String) cmd.valueOf(OPT_PUB);
            signPrivateKey = (String) cmd.valueOf(OPT_PRI);
            signKeyPassword = (String) cmd.valueOf(OPT_KEYPASSWD);
        }

        if (cmd.has(OPT_CPK)) {
            String inputCpk = (String) cmd.valueOf(OPT_CPK);
            signCpk(inputCpk, outputCpk);
            return;
        }

        sign = true;
    }
    */

    /**
     * Get OptionParser that is used by CLI client.
     * @return OptionParser
     * @throws Exception
     */
    protected static OptionParser getDefaultOptionParser() throws Exception {
        OptionParser p = new OptionParser();

        p.accepts(CMD_CREATE, Constants.MESSAGE.getString("cmd_create"));
        p.accepts(CMD_CREATE_XML, Constants.MESSAGE.getString("cmd_create_xml"));
        p.accepts(CMD_XML, Constants.MESSAGE.getString("cmd_xml"));
        //p.accepts(CMD_SIGN, Constants.MESSAGE.getString("cmd_sign"));
        p.accepts(CMD_TEMPLATE, Constants.MESSAGE.getString("cmd_template"));
        p.accepts(CMD_INSTALL, Constants.MESSAGE.getString("cmd_install"));
        p.accepts(CMD_UNINSTALL, Constants.MESSAGE.getString("cmd_uninstall"));
        if (PlatformType.LinkForDevice.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
            p.accepts(CMD_CONFIG_GET, Constants.MESSAGE.getString("cmd_config_get"));
            p.accepts(CMD_CONFIG_UPDATE, Constants.MESSAGE.getString("cmd_config_update"));
            p.accepts(CMD_PROVIDER_LIST, Constants.MESSAGE.getString("cmd_provider_list"));
            p.accepts(CMD_ATTESTATION_UPDATE, Constants.MESSAGE.getString("cmd_attestation_update"));
        }
        p.accepts(CMD_HELP, Constants.MESSAGE.getString("cmd_help"));
        p.accepts(CMD_APP_LIST, Constants.MESSAGE.getString("cmd_app_list"));
        p.accepts(CMD_APP_DETAIL, Constants.MESSAGE.getString("cmd_app_detail"));

        commandsParser.accepts(CMD_CREATE, Constants.MESSAGE.getString("cmd_create"));
//        commandsParser.accepts(CMD_CREATE_XML, Constants.MESSAGE.getString("cmd_create_xml"));
        commandsParser.accepts(CMD_XML, Constants.MESSAGE.getString("cmd_xml"));
        //commandsParser.accepts(CMD_SIGN, Constants.MESSAGE.getString("cmd_sign"));
        commandsParser.accepts(CMD_TEMPLATE, Constants.MESSAGE.getString("cmd_template"));
        commandsParser.accepts(CMD_INSTALL, Constants.MESSAGE.getString("cmd_install"));
        commandsParser.accepts(CMD_UNINSTALL, Constants.MESSAGE.getString("cmd_uninstall"));
        if (PlatformType.LinkForDevice.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
            commandsParser.accepts(CMD_CONFIG_GET, Constants.MESSAGE.getString("cmd_config_get"));
            commandsParser.accepts(CMD_CONFIG_UPDATE, Constants.MESSAGE.getString("cmd_config_update"));
            commandsParser.accepts(CMD_PROVIDER_LIST, Constants.MESSAGE.getString("cmd_provider_list"));
            commandsParser.accepts(CMD_ATTESTATION_UPDATE, Constants.MESSAGE.getString("cmd_attestation_update"));
        }
        commandsParser.accepts(CMD_HELP, Constants.MESSAGE.getString("cmd_help"));
        commandsParser.accepts(CMD_APP_LIST, Constants.MESSAGE.getString("cmd_app_list"));
        commandsParser.accepts(CMD_APP_DETAIL, Constants.MESSAGE.getString("cmd_app_detail"));

        p.accepts(OPT_OUTPUT, Constants.MESSAGE.getString("opt_output")).requiredIf(CMD_CREATE, CMD_CREATE_XML,CMD_XML/*, CMD_SIGN*/).withRequiredArg().describedAs("hpk-file");
        p.accepts(OPT_CONNECTOR, Constants.MESSAGE.getString("opt_connector")).requiredIf(CMD_XML).withRequiredArg().describedAs("xml-file");
        p.accepts(OPT_INSTALLFILE, Constants.MESSAGE.getString("opt_installfile")).requiredIf(CMD_CREATE, CMD_CREATE_XML,CMD_XML, CMD_INSTALL).withRequiredArg().describedAs("install-file");
        p.accepts(OPT_FORCEINSTASLL, Constants.MESSAGE.getString("opt_forceinstall"));

        p.accepts(OPT_NAME, Constants.MESSAGE.getString("opt_name"))
                .requiredIf(CMD_CREATE, CMD_CREATE_XML).withRequiredArg().describedAs(OPT_NAME);
        p.accepts(OPT_VENDOR, Constants.MESSAGE.getString("opt_vendor"))
                .requiredIf(CMD_CREATE, CMD_CREATE_XML).withRequiredArg().describedAs(OPT_VENDOR);
        p.accepts(OPT_DATE, Constants.MESSAGE.getString("opt_date"))
                .withRequiredArg().describedAs("yyyymmdd");

        if (PlatformType.LinkForDevice.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
            p.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid"))
                    .requiredIf(CMD_CREATE, CMD_CREATE_XML, CMD_UNINSTALL, CMD_CONFIG_GET, CMD_CONFIG_UPDATE, CMD_APP_DETAIL, CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(OPT_UUID);
            p.accepts(OPT_DEFAULT_CONFIG, Constants.MESSAGE.getString("opt_default_config"))
                    .withRequiredArg().describedAs(OPT_DEFAULT_CONFIG);
            p.accepts(OPT_SUBACTIVITY_1, Constants.MESSAGE.getString("opt_1st") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            p.accepts(OPT_SUBACTIVITY_2, Constants.MESSAGE.getString("opt_2nd") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            p.accepts(OPT_SUBACTIVITY_3, Constants.MESSAGE.getString("opt_3rd") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            p.accepts(OPT_SUBACTIVITY_4, Constants.MESSAGE.getString("opt_4th") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            p.accepts(OPT_SUBACTIVITY_5, Constants.MESSAGE.getString("opt_5th") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            p.accepts(OPT_LINK_AUTH_AGENT, Constants.MESSAGE.getString("opt_link_auth_agent"))
                    .withOptionalArg().describedAs("-p:name={\"\":\"\"} -p:description={\"\":\"\"} -p:url=package/activity (Optional)-p:preprompt=enablePrePrompt");
            p.accepts(OPT_STATISTICS_AGENT, Constants.MESSAGE.getString("opt_statistics_agent"))
                    .withOptionalArg().describedAs("-s:name={\"\":\"\"} -s:description={\"\":\"\"} (Optional)-s:ackrequiredfordelete=enableAckRequiredForDelete");
            p.acceptsAll(Arrays.asList(OPT_USE_HOMESCREEN_MODE), Constants.MESSAGE.getString("opt_use_homescreen_mode"))
                    .withRequiredArg().describedAs("enable-homescreen-mode");
            p.acceptsAll(Arrays.asList(OPT_SET_HOMESCREEN_DEFAULT), Constants.MESSAGE.getString("opt_set_homescreen_as_default"))
                    .withRequiredArg().describedAs("set-as-default");
            for (int number = 1; number <= MAX_ACCESSORIES; number++) {
                p.accepts(OPT_ACCESSORY + Integer.toString(number), "#" + Integer.toString(number) + " " + Constants.MESSAGE.getString("opt_accessory"))
                        .withRequiredArg().describedAs("type,vender-id,product-id,(Optional)serial-number");
            }
            p.accepts(OPT_WEBSERVICE, Constants.MESSAGE.getString("opt_webservice"))
                    .withOptionalArg().describedAs("-w:name={\"\":\"\"} -w:description={\"\":\"\"} -w:endpoint=method,category,absolutePath,authType:method,category,absolutePath,authType:...");
            p.acceptsAll(Arrays.asList(OPT_PLATFORM_VERSION), Constants.MESSAGE.getString("opt_platform_version"))
                    .withRequiredArg().describedAs("platform-version");
            p.accepts(OPT_SCHEMA_VERSION, Constants.MESSAGE.getString("opt_schema_version"))
                    .withRequiredArg().describedAs("schema-version");

            p.accepts(OPT_USERNAME, Constants.MESSAGE.getString("opt_username")).requiredIf(CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(OPT_USERNAME);
            p.accepts(OPT_LDB_KEY, Constants.MESSAGE.getString("opt_ldbkey")).requiredIf(CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(OPT_LDB_KEY);
            p.accepts(OPT_CREDENTIALS, Constants.MESSAGE.getString("opt_credentials")).requiredIf(CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(OPT_CREDENTIALS);
            p.accepts(OPT_COMMAND_LOCATION, Constants.MESSAGE.getString("opt_command_location")).withRequiredArg().describedAs(OPT_COMMAND_LOCATION);

            p.accepts(PARAM_LINK_AUTH_AGENT).withRequiredArg();
            p.accepts(PARAM_WEBSERVICE_AGENT).withRequiredArg();
            p.accepts(PARAM_STATISTICS_AGENT).withRequiredArg();


            p.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host"))
                    .requiredIf(CMD_INSTALL, CMD_UNINSTALL, CMD_CONFIG_GET, CMD_CONFIG_UPDATE, CMD_APP_LIST, CMD_APP_DETAIL, CMD_PROVIDER_LIST, CMD_ATTESTATION_UPDATE).withRequiredArg().describedAs(OPT_HOST);
            p.accepts(OPT_DATA, Constants.MESSAGE.getString("opt_data"))
                    .requiredIf(CMD_CONFIG_UPDATE).withRequiredArg().describedAs(OPT_DATA);

        } else {
            p.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid"))
                    .requiredIf(CMD_CREATE, CMD_UNINSTALL, CMD_APP_DETAIL).withRequiredArg().describedAs(OPT_UUID);
            p.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host"))
                    .requiredIf(CMD_INSTALL, CMD_UNINSTALL, CMD_APP_LIST, CMD_APP_DETAIL).withRequiredArg().describedAs(OPT_HOST);
        }

        p.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);

        p.accepts(OPT_NON_LINK, Constants.MESSAGE.getString("opt_non_link")).withRequiredArg().describedAs(OPT_NON_LINK);

        xmlParser.accepts(OPT_OUTPUT, Constants.MESSAGE.getString("opt_output")).withRequiredArg().describedAs("hpk-file").required();
        xmlParser.accepts(OPT_CONNECTOR, Constants.MESSAGE.getString("opt_connector")).withRequiredArg().describedAs("xml-file").required();
        xmlParser.accepts(OPT_INSTALLFILE, Constants.MESSAGE.getString("opt_installfile")).withRequiredArg().describedAs("install-file").required();
        xmlParser.accepts(OPT_DEFAULT_CONFIG, Constants.MESSAGE.getString("opt_default_config")).withRequiredArg().describedAs(OPT_DEFAULT_CONFIG);

        createParser.accepts(OPT_OUTPUT, Constants.MESSAGE.getString("opt_output")).withRequiredArg().describedAs("hpk-file").required();
        createParser.accepts(OPT_INSTALLFILE, Constants.MESSAGE.getString("opt_installfile")).withRequiredArg().describedAs("install-file").required();
        createParser.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid"))
                .withRequiredArg().describedAs(OPT_UUID).required();
        createParser.accepts(OPT_NAME, Constants.MESSAGE.getString("opt_name"))
                .withRequiredArg().describedAs("app-name").required();
        createParser.accepts(OPT_VENDOR, Constants.MESSAGE.getString("opt_vendor"))
                .withRequiredArg().describedAs(OPT_VENDOR).required();
        createParser.accepts(OPT_DATE, Constants.MESSAGE.getString("opt_date"))
                .withRequiredArg().describedAs("yyyymmdd");

        if (PlatformType.LinkForDevice.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
            createParser.accepts(OPT_DEFAULT_CONFIG, Constants.MESSAGE.getString("opt_default_config"))
                    .withRequiredArg().describedAs(OPT_DEFAULT_CONFIG);
            createParser.accepts(OPT_SUBACTIVITY_1, Constants.MESSAGE.getString("opt_1st") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            createParser.accepts(OPT_SUBACTIVITY_2, Constants.MESSAGE.getString("opt_2nd") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            createParser.accepts(OPT_SUBACTIVITY_3, Constants.MESSAGE.getString("opt_3rd") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            createParser.accepts(OPT_SUBACTIVITY_4, Constants.MESSAGE.getString("opt_4th") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            createParser.accepts(OPT_SUBACTIVITY_5, Constants.MESSAGE.getString("opt_5th") + " " + Constants.MESSAGE.getString("opt_sub_activity"))
                    .withRequiredArg().describedAs(OPT_UUID + ",package/Activity,(Optional)type");
            createParser.accepts(OPT_LINK_AUTH_AGENT, Constants.MESSAGE.getString("opt_link_auth_agent"))
                    //.withRequiredArg().describedAs("name{code:value,...,code:value},description{code:value,...,code:value},package/activity");
                    .withRequiredArg().describedAs("-p:name={\"\":\"\"} -p:description={\"\":\"\"} -p:url=package/activity (Optional)-p:preprompt=enablePrePrompt");
            createParser.accepts(OPT_STATISTICS_AGENT, Constants.MESSAGE.getString("opt_statistics_agent"))
                    .withRequiredArg().describedAs("-s:name={\"\":\"\"} -s:description={\"\":\"\"} (Optional)-s:ackrequiredfordelete=enableAckRequiredForDelete");
            createParser.acceptsAll(Arrays.asList(OPT_USE_HOMESCREEN_MODE), Constants.MESSAGE.getString("opt_use_homescreen_mode"))
                    .withRequiredArg().describedAs("enable-homescreen-mode");
            createParser.acceptsAll(Arrays.asList(OPT_SET_HOMESCREEN_DEFAULT), Constants.MESSAGE.getString("opt_set_homescreen_as_default"))
                    .withRequiredArg().describedAs("set-as-default");
            for (int number = 1; number <= MAX_ACCESSORIES; number++) {
                createParser.accepts(OPT_ACCESSORY + Integer.toString(number), "#" + Integer.toString(number) + " " + Constants.MESSAGE.getString("opt_accessory"))
                        .withRequiredArg().describedAs("type,vender-id,product-id,(Optional)serial-number");
            }
            createParser.accepts(OPT_WEBSERVICE, Constants.MESSAGE.getString("opt_webservice"))
                    .withRequiredArg().describedAs("-w:name={\"\":\"\"} -w:description={\"\":\"\"} -w:endpoint=method,category,absolutePath,authType:method,category,absolutePath,authType:...");
            createParser.acceptsAll(Arrays.asList(OPT_PLATFORM_VERSION), Constants.MESSAGE.getString("opt_platform_version"))
                    .withRequiredArg().describedAs("platform-version");
            createParser.accepts(OPT_SCHEMA_VERSION, Constants.MESSAGE.getString("opt_schema_version"))
                    .withRequiredArg().describedAs("schema-version");
        }
        installParser.accepts(OPT_INSTALLFILE, Constants.MESSAGE.getString("opt_installfile")).withRequiredArg().describedAs("install-file").required();
        installParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        installParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);
        installParser.accepts(OPT_FORCEINSTASLL, Constants.MESSAGE.getString("opt_forceinstall"));

        uninstallParser.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid")).withRequiredArg().describedAs(OPT_UUID).required();
        uninstallParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        uninstallParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);

        configGetParser.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid")).withRequiredArg().describedAs(OPT_UUID).required();
        configGetParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        configGetParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);

        configUpdateParser.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid")).withRequiredArg().describedAs(OPT_UUID).required();
        configUpdateParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        configUpdateParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);
        configUpdateParser.accepts(OPT_DATA, Constants.MESSAGE.getString("opt_data")).withRequiredArg().describedAs(OPT_DATA).required();

        appListParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        appListParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);

        appDetailParser.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid")).withRequiredArg().describedAs(OPT_UUID).required();
        appDetailParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        appDetailParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);

        providerListParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        providerListParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);

        attestationParser.accepts(OPT_UUID, Constants.MESSAGE.getString("opt_uuid")).withRequiredArg().describedAs(OPT_UUID).required();
        attestationParser.accepts(OPT_HOST, Constants.MESSAGE.getString("opt_host")).withRequiredArg().describedAs(OPT_HOST).required();
        attestationParser.accepts(OPT_PASSWORD, Constants.MESSAGE.getString("opt_password")).withRequiredArg().describedAs(OPT_PASSWORD);
        attestationParser.accepts(OPT_USERNAME, Constants.MESSAGE.getString("opt_username")).withRequiredArg().describedAs(OPT_USERNAME).required();
        attestationParser.accepts(OPT_LDB_KEY, Constants.MESSAGE.getString("opt_ldbkey")).withRequiredArg().describedAs(OPT_LDB_KEY).required();
        attestationParser.accepts(OPT_CREDENTIALS, Constants.MESSAGE.getString("opt_credentials")).withRequiredArg().describedAs(OPT_CREDENTIALS).required();
        attestationParser.accepts(OPT_COMMAND_LOCATION, Constants.MESSAGE.getString("opt_command_location")).withRequiredArg().describedAs(OPT_COMMAND_LOCATION);

        /*
        p.accepts(OPT_CPK, "Source HPK package file to sign")
                .requiredIf(CMD_SIGN).withRequiredArg().describedAs("cpk-file");
        p.accepts(OPT_KEYSTORE, "Keystore location")
                .withRequiredArg().describedAs(OPT_KEYSTORE);
        p.accepts(OPT_STORETYPE, "Key store type - jks, pkcs12, jceks (default " + KeyStore.getDefaultType() + ")")
                .withRequiredArg().describedAs("jks|pkcs12|jceks");
        p.accepts(OPT_STOREPASSWD, "Key store password").requiredIf(OPT_KEYSTORE).withRequiredArg().describedAs("password");
        p.accepts(OPT_ALIAS, "Keystore location").requiredIf(OPT_KEYSTORE).withRequiredArg().describedAs(OPT_KEYSTORE);
        p.accepts(OPT_PUB, "X.509 Public Key certificate").requiredUnless(OPT_KEYSTORE, CMD_CREATE, CMD_XML, CMD_TEMPLATE, CMD_HELP).withRequiredArg().describedAs("pub-key");
        p.accepts(OPT_PRI, "PKCS#8 Private Key").requiredUnless(OPT_KEYSTORE, CMD_CREATE, CMD_XML, CMD_TEMPLATE, CMD_HELP).withRequiredArg().describedAs("pri-key");
        p.accepts(OPT_KEYPASSWD, "Key password").requiredIf(CMD_SIGN).withRequiredArg().describedAs("password");

        signParser.accepts(OPT_OUTPUT, "Output HPK package file path").withRequiredArg().describedAs("cpk-file");
        signParser.accepts(OPT_CPK, "Source HPK package file to sign")
                .withRequiredArg().describedAs("cpk-file");
        signParser.accepts(OPT_KEYSTORE, "Keystore location")
                .withRequiredArg().describedAs(OPT_KEYSTORE);
        signParser.accepts(OPT_STORETYPE, "Key store type - jks, pkcs12, jceks (default " + KeyStore.getDefaultType() + ")")
                .withRequiredArg().describedAs("jks|pkcs12|jceks");
        signParser.accepts(OPT_STOREPASSWD, "Key store password").requiredIf(OPT_KEYSTORE).withRequiredArg().describedAs("password");
        signParser.accepts(OPT_ALIAS, "Keystore location").withRequiredArg().describedAs(OPT_KEYSTORE);
        signParser.accepts(OPT_PUB, "X.509 Public Key certificate").withRequiredArg().describedAs("pub-key");
        signParser.accepts(OPT_PRI, "PKCS#8 Private Key").withRequiredArg().describedAs("pri-key");
        signParser.accepts(OPT_KEYPASSWD, "Key password").withRequiredArg().describedAs("password");
        */

        return p;
    }

    protected static OptionParser getLinkAuthAgentOptionParser() throws Exception {
        OptionParser p = new OptionParser();
        p.accepts(PARAM_NAME).withRequiredArg();
        p.accepts(PARAM_DESCRIPTION).withRequiredArg();
        p.accepts(PARAM_URL).withRequiredArg();
        p.accepts(PARAM_PREPROMPT).withRequiredArg().ofType(Boolean.class);

        return p;
    }

    protected static OptionParser getWebServiceOptionParser() throws Exception {
        OptionParser p = new OptionParser();
        p.accepts(PARAM_NAME).withRequiredArg();
        p.accepts(PARAM_DESCRIPTION).withRequiredArg();
        p.accepts(PARAM_ENDPOINT).withRequiredArg();

        return p;
    }

    protected static OptionParser getStatisticsAgentOptionParser() throws Exception {
        OptionParser p = new OptionParser();
        p.accepts(PARAM_NAME).withRequiredArg();
        p.accepts(PARAM_DESCRIPTION).withRequiredArg();
        p.accepts(PARAM_ACK_REQUIRED_FOR_DELETE).withRequiredArg().ofType(Boolean.class);

        return p;
    }

    protected static void exit(int code) {
        if (!"true".equals(System.getProperty("test")))
            System.exit(code);
    }

    private static ArrayList<LocalizedString> getLocalizedArrayFromJson(JsonObject jsonObject) {
        ArrayList<LocalizedString> localizedList = new ArrayList<>();

        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            LocalizedString localizedStr = new LocalizedString();
            localizedStr.setCode(key);
            localizedStr.setValue(jsonObject.get(key).getAsString());
            localizedList.add(localizedStr);
        }

        return localizedList;
    }

    private static ArrayList<LocalizedString> getExplicitLocalizedArrayFromJson(JsonObject jsonObject) {
        ArrayList<LocalizedString> localizedList = new ArrayList<>();

        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            LocalizedString localizedStr = new LocalizedString();
            localizedStr.setLanguageTag(key);
            localizedStr.setValue(jsonObject.get(key).getAsString());
            localizedList.add(localizedStr);
        }

        return localizedList;
    }
    private static JsonObject getJsonObjectFromOptionSet(OptionSet optionSet, String optionName) throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject result = new JsonObject();
        List<String> nameValues = (List<String>) optionSet.valuesOf(optionName);
        for (String nameValue : nameValues) {
            try {
                JsonObject jsonObject = parser.parse(nameValue).getAsJsonObject();
                Set<String> keys = jsonObject.keySet();
                for (String key : keys) {
                    result.addProperty(key, jsonObject.get(key).getAsString());
                }
            } catch (Exception e) {
                if (nameValue.startsWith("{") && nameValue.endsWith("}")) {
                    throw e;
                }
                int index = nameValue.indexOf(":");
                String key = nameValue.substring(0, index);
                String value = nameValue.substring(index + 1);
                result.addProperty(key, value);
            }
        }
        return result;
    }
    protected static Provider parseAuthAgent(OptionSet linkAuthAgentOptionSet) throws Exception {
        JsonObject jsonName = getJsonObjectFromOptionSet(linkAuthAgentOptionSet,PARAM_NAME);
        JsonObject jsonDes = getJsonObjectFromOptionSet(linkAuthAgentOptionSet,PARAM_DESCRIPTION);

        Provider provider = new Provider();
        provider.setType(Constants.PROVIDER_TYPE_AUTHN);
        provider.setUuid(UUID.randomUUID().toString());
        provider.setTitle(getLocalizedArrayFromJson(jsonName));
        provider.setDescription(getLocalizedArrayFromJson(jsonDes));
        provider.setAuthenticationUrl(linkAuthAgentOptionSet.valueOf(PARAM_URL).toString());
        provider.setEnablePrePromptCheck(linkAuthAgentOptionSet.has(PARAM_PREPROMPT) ?
                linkAuthAgentOptionSet.valueOf(PARAM_PREPROMPT).toString() : "false");

        return provider;
    }

    protected static Provider parseStatisticsAgent(OptionSet statisticsAgentOptionSet) throws Exception {
        JsonObject jsonName = getJsonObjectFromOptionSet(statisticsAgentOptionSet,PARAM_NAME);
        JsonObject jsonDes = getJsonObjectFromOptionSet(statisticsAgentOptionSet,PARAM_DESCRIPTION);

        Provider provider = new Provider();
        provider.setType(Constants.PROVIDER_TYPE_STATISTICS);
        provider.setUuid(UUID.randomUUID().toString());
        provider.setTitle(getExplicitLocalizedArrayFromJson(jsonName));
        provider.setDescription(getExplicitLocalizedArrayFromJson(jsonDes));
        provider.setAckRequiredForDelete(statisticsAgentOptionSet.has(PARAM_ACK_REQUIRED_FOR_DELETE) ?
                statisticsAgentOptionSet.valueOf(PARAM_ACK_REQUIRED_FOR_DELETE).toString() : "false");

        return provider;
    }

    protected static Provider parseAccessory(String accessoryStr) throws Exception {
        String[] accessoryInfo = accessoryStr.split(",", -1);
        Provider provider = new Provider();
        provider.setType(Constants.PROVIDER_TYPE_ACCESSORIES);

        boolean isRegistrationType = false;
        for (AccessoryInfo.RegistrationType type : AccessoryInfo.RegistrationType.values()) {
            if (type.name().equalsIgnoreCase(accessoryInfo[0])) {
                isRegistrationType = true;
                break;
            }
        }

        if (isRegistrationType) {
            provider.setRegistrationType(accessoryInfo[0]);
        } else {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + ": " + accessoryStr);
        }

        provider.setVendorId(accessoryInfo[1]);
        provider.setProductId(accessoryInfo[2]);
        if (accessoryInfo.length > 3) {
            if (accessoryInfo[3].equalsIgnoreCase("null")) {
                accessoryInfo[3] = "NULL";
            }
            provider.setSerialNumber(accessoryInfo[3]);
        } else {
            provider.setSerialNumber("NULL");
        }
        return provider;
    }

    protected static Provider parseWebService(OptionSet webServiceOptionSet) throws Exception {
        JsonObject jsonName = getJsonObjectFromOptionSet(webServiceOptionSet, PARAM_NAME);
        JsonObject jsonDesc = getJsonObjectFromOptionSet(webServiceOptionSet, PARAM_DESCRIPTION);
        JsonArray endPointArray = parseWebServiceEndPoint(webServiceOptionSet.valueOf(PARAM_ENDPOINT).toString());

        Provider provider = new Provider();
        provider.setType(PROVIDER_TYPE_WEBSERVICES);
        provider.setUuid(UUID.randomUUID().toString());
        provider.setTitle(getLocalizedArrayFromJson(jsonName));
        provider.setDescription(getLocalizedArrayFromJson(jsonDesc));
        provider.setEndPoints(endPointArray.toString());

        return provider;
    }

    private static JsonArray parseWebServiceEndPoint(String endPointsStr) throws Exception {
        String[] endPoints = endPointsStr.split(":",-1);
        JsonArray array = new JsonArray();
        for (String endPoint : endPoints) {
            array.add(parseWebServiceEndPointObject(endPoint));
        }
        return array;
    }

    private static JsonObject parseWebServiceEndPointObject(String endPointStr) throws Exception {
        String[] endPointInfo = endPointStr.split(",",-1);
        JsonObject object = new JsonObject();

        boolean isMethodType = false;
        for (WebServiceEndPoint.MethodType type : WebServiceEndPoint.MethodType.values()) {
            if (type.name().equalsIgnoreCase(endPointInfo[0])) {
                isMethodType = true;
                break;
            }
        }
        if (isMethodType) {
            object.addProperty(PROPERTY_WEBSERVICE_METHOD, endPointInfo[0].toLowerCase());
        } else {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + ": " + endPointStr);
        }

        object.addProperty(PROPERTY_WEBSERVICE_CATEGORY, endPointInfo[1]);
        object.addProperty(PROPERTY_WEBSERVICE_ABSOLUTEPATH, endPointInfo[2]);

        // JALPINF-1094 Process hpk files that do not have an authType in previous version.
        if (endPointInfo.length > 3) {
            boolean isAuthType = false;
            for (WebServiceEndPoint.AuthType type : WebServiceEndPoint.AuthType.values()) {
                if (type.getName().equalsIgnoreCase(endPointInfo[3])) {
                    isAuthType = true;
                    break;
                }
            }
            if (isAuthType) {
                object.addProperty(PROPERTY_WEBSERVICE_AUTHTYPE, endPointInfo[3].toLowerCase());
            } else {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + ": " + endPointStr);
            }
        } else {
            object.addProperty(PROPERTY_WEBSERVICE_AUTHTYPE, WebServiceEndPoint.AuthType.NONE.getName().toLowerCase());
        }
        if (endPointInfo.length > 4) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + ": " + endPointStr);
        }
        return object;
    }

    protected static SubApp parseSubApp(String subAppStr) throws Exception {
        String[] subAppInfo = subAppStr.split(",");
        SubApp subApp = new SubApp();
        subApp.setUuid(UUID.fromString(subAppInfo[0]));
        subApp.setPlatformId(subAppInfo[1]);
        return subApp;
    }

    protected static void printHelp(PlatformType platformType) throws Exception {
        System.out.println(Constants.MESSAGE.getString("menu_toolname") + " " + Constants.MESSAGE.getString("menu_version") + " " + Constants.TOOL_VERSION + "(" + TOOL_BUILD_DATE + ")");

        System.out.print(Constants.MESSAGE.getString("prefix_usage"));
        System.out.print(Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("prefix_example"));

        if (PlatformType.LinkForDevice.equals(platformType)) {
            System.out.println(Constants.TOOL_NAME + " -> " + "HPKTool-cli.exe");
        } else if (PlatformType.LinkForWeb.equals(platformType)) {
            System.out.println(Constants.TOOL_NAME + " -> " + "HPKToolForWeb-cli.exe");
        }
        System.out.println();

        System.out.println(Constants.MESSAGE.getString("prefix_commands"));
        commandsParser.formatHelpWith(new HelpFormatter());
        commandsParser.printHelpOn(System.out);
        System.out.println();

        System.out.println(Constants.MESSAGE.getString("usage_create"));
        System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --create:");
        createParser.formatHelpWith(new HelpFormatter());
        createParser.printHelpOn(System.out);
        System.out.println(Constants.MESSAGE.getString("footer_create"));
        if (PlatformType.LinkForDevice.equals(platformType)) {
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create1"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create2"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create3"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create5"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create9"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create10"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create6"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create7"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create8"));
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create11"));
        } else if (PlatformType.LinkForWeb.equals(platformType)) {
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_create4"));
        }
        System.out.println();

        System.out.println(Constants.MESSAGE.getString("usage_xml"));
        System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --xml:");
        xmlParser.formatHelpWith(new HelpFormatter());
        xmlParser.printHelpOn(System.out);
        System.out.println(Constants.MESSAGE.getString("footer_xml"));
        if (PlatformType.LinkForDevice.equals(platformType)) {
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_xml"));
        } else if (PlatformType.LinkForWeb.equals(platformType)) {
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_xml_web"));
        }

        System.out.println(Constants.MESSAGE.getString("usage_install"));
        System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --install:");
        installParser.formatHelpWith(new HelpFormatter());
        installParser.printHelpOn(System.out);
        System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_install"));

        System.out.println(Constants.MESSAGE.getString("usage_uninstall"));
        System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --uninstall:");
        uninstallParser.formatHelpWith(new HelpFormatter());
        uninstallParser.printHelpOn(System.out);
        System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_uninstall"));

        if (PlatformType.LinkForDevice.equals(platformType)) {
            System.out.println(Constants.MESSAGE.getString("usage_config_get"));
            System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --config-get:");
            configGetParser.formatHelpWith(new HelpFormatter());
            configGetParser.printHelpOn(System.out);
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_config_get"));

            System.out.println(Constants.MESSAGE.getString("usage_config_update"));
            System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --config-update:");
            configUpdateParser.formatHelpWith(new HelpFormatter());
            configUpdateParser.printHelpOn(System.out);
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_config_update"));
        }

        System.out.println(Constants.MESSAGE.getString("usage_app_list"));
        System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --app-list");
        appListParser.formatHelpWith(new HelpFormatter());
        appListParser.printHelpOn(System.out);
        System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_app_list"));

        System.out.println(Constants.MESSAGE.getString("usage_app_detail"));
        System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --app-detail");
        appDetailParser.formatHelpWith(new HelpFormatter());
        appDetailParser.printHelpOn(System.out);
        System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_app_detail"));

        if (PlatformType.LinkForDevice.equals(platformType)) {
            System.out.println(Constants.MESSAGE.getString("usage_provider_list"));
            System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --provider-list");
            providerListParser.formatHelpWith(new HelpFormatter());
            providerListParser.printHelpOn(System.out);
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_provider_list"));

            System.out.println(Constants.MESSAGE.getString("usage_attestation_update"));
            System.out.println(Constants.MESSAGE.getString("prefix_options") + " for --attestation-update");
            attestationParser.formatHelpWith(new HelpFormatter());
            attestationParser.printHelpOn(System.out);
            System.out.println(Constants.MESSAGE.getString("example") + Constants.TOOL_NAME + " " + Constants.MESSAGE.getString("example_attestation_update"));
        }

        System.out.flush();
    }

    protected static void exitWithError(String msg) {
        System.out.println(Constants.MESSAGE.getString("prefix_error") + msg + NEWLINE);
        exit(1);
    }

    protected static class HelpFormatter extends BuiltinHelpFormatter {
        HelpFormatter() {
            super(255, 2);
        }

        @Override
        public String format(Map<String, ? extends OptionDescriptor> options) {
            addRows(new LinkedHashSet<>(options.values()));
            return formattedHelpOutput();
        }

        @Override
        protected void addHeaders(Collection<? extends OptionDescriptor> options) {
        }
    }

    protected static TaskInterface getProvidersTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            if (obj != null) {
                List<ProviderInfo> providerList = (List<ProviderInfo>) obj;
                ArrayList<ProviderSimpleInfo> providerSimpleInfoList = new ArrayList<>();
                if (providerList == null || providerList.size() == 0) {
                    System.out.println(Constants.MESSAGE.getString("noApps"));
                    return;
                }

                for (ProviderInfo provider : providerList) {
                    ProviderSimpleInfo simpleInfo = new ProviderSimpleInfo();
                    simpleInfo.functionType = provider.getFunctionType();
                    simpleInfo.parentUuid = provider.getParentUuid();
                    simpleInfo.uuid = provider.getUuid();
                    providerSimpleInfoList.add(simpleInfo);
                }
                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    System.out.println(gson.toJson(providerSimpleInfoList));
                } catch (Exception e) {
                    System.out.println(Constants.MESSAGE.getString("noApps"));
                }
            } else {
                System.out.println(Constants.MESSAGE.getString("noApps"));
            }

            exit(0);
        }

        @Override
        public void onFailed(Exception e) {
            if (e instanceof ResourceException) {
                String msg = e.getMessage();
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                } else if (re.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
                    try {
                        Error err = JsonHelper.fromJson(re.getResponse().getEntity().getText(), Error.class);
                        msg = err.getCause() + Constants.MESSAGE.getString("error_bad_request");
                    } catch (Exception err) {
                        // do nothing.
                    }
                }
                System.out.println(msg);
            } else {
                System.out.println(e.getMessage());
            }
            exit(1);
        }

        class ProviderSimpleInfo {
            String functionType;
            UUID parentUuid;
            UUID uuid;
        }
    };

    protected static TaskInterface getDetailTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            if (obj != null && obj.getClass() == AppInfo.class) {
                AppInfo appInfo = (AppInfo) obj;
                if (appInfo.getPlatformType().equalsIgnoreCase(Constants.DEFAULT_PLATFORM_TYPE.toString())) {
                    System.out.println(appInfo.toString());
                } else {
                    System.out.println(Constants.MESSAGE.getString("noApps"));
                }
            } else {
                System.out.println(Constants.MESSAGE.getString("noApps"));
            }
            exit(0);
        }

        @Override
        public void onFailed(Exception e) {
            if (e instanceof ResourceException) {
                String msg = e.getMessage();
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                } else if (re.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
                    try {
                        Error err = JsonHelper.fromJson(re.getResponse().getEntity().getText(), Error.class);
                        msg = err.getCause() + Constants.MESSAGE.getString("error_bad_request");
                    } catch (Exception err) {
                        // do nothing.
                    }
                }
                System.out.println(msg);
            } else {
                System.out.println(e.getMessage());
            }
            exit(1);
        }
    };

    protected static TaskInterface getListTaskInterface = new TaskInterface() {
        @Override
        public String updateMessage(TaskStatus msg) {
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            if (obj != null) {
                List<AppInfo> appInfoList = (List<AppInfo>) obj;
                ArrayList<AppSimpleInfo> appSimpleInfoList = new ArrayList<>();
                if (appInfoList == null || appInfoList.size() == 0) {
                    System.out.println(Constants.MESSAGE.getString("noApps"));
                    return;
                }

                for (AppInfo appInfo : appInfoList) {
                    if (appInfo.getPlatformType().equalsIgnoreCase(Constants.DEFAULT_PLATFORM_TYPE.toString())) {
                        AppSimpleInfo simpleInfo = new AppSimpleInfo();
                        simpleInfo.uuid = appInfo.getUuid();
                        simpleInfo.name = appInfo.getName();
                        appSimpleInfoList.add(simpleInfo);
                    }
                }
                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    System.out.println(gson.toJson(appSimpleInfoList));
                } catch (Exception e) {
                    System.out.println(Constants.MESSAGE.getString("noApps"));
                }
            } else {
                System.out.println(Constants.MESSAGE.getString("noApps"));
            }

            exit(0);
        }

        @Override
        public void onFailed(Exception e) {
            if (e instanceof ResourceException) {
                String msg = e.getMessage();
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                } else if (re.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
                    try {
                        Error err = JsonHelper.fromJson(re.getResponse().getEntity().getText(), Error.class);
                        msg = err.getCause() + Constants.MESSAGE.getString("error_bad_request");
                    } catch (Exception err) {
                        // do nothing.
                    }
                }
                System.out.println(msg);
            } else {
                System.out.println(e.getMessage());
            }
            exit(1);
        }

        class AppSimpleInfo {
            String uuid;
            String name;
        }
    };


    protected static TaskInterface taskInterface = new TaskInterface() {
        TaskStatus currentStatus = new TaskStatus(PackageInstallerState.psCompleted, null);
        @Override
        public String updateMessage(final TaskStatus taskStatus) {
            if (taskStatus.getState().equals(PackageInstallerState.psSending)) {
                if (!currentStatus.getState().equals(PackageInstallerState.psSending)) {
                    currentStatus = taskStatus;
                    System.out.println(Constants.MESSAGE.getString("msg_sending"));
                }
            } else if (taskStatus.getState().equals(PackageInstallerState.psInProgress)) {
                if (!currentStatus.getState().equals(PackageInstallerState.psInProgress)) {
                    currentStatus = taskStatus;
                    System.out.println(Constants.MESSAGE.getString("msg_progressing"));
                }
            } else if (taskStatus.getCause() != null) {
                currentStatus = taskStatus;
                System.out.println(Constants.MESSAGE.getString("prefix_error") + taskStatus.getCause());
            }
            return null;
        }

        @Override
        public void onSucceed(Object obj) {
            if (obj != null && obj.getClass() == Configuration.class) {
                System.out.println(((Configuration) obj).getData());
            } else {
                System.out.println(Constants.MESSAGE.getString("msg_finished"));
            }
            exit(0);
        }

        @Override
        public void onFailed(Exception e) {
            if (e instanceof ResourceException) {
                String msg = e.getMessage();
                ResourceException re = (ResourceException) e;
                if (re.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
                    msg = Constants.MESSAGE.getString("msg_install_failed");
                } else if (re.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)
                        || re.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                    msg = Constants.MESSAGE.getString("msg_pw_error");
                } else if (re.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
                    try {
                        Error err = JsonHelper.fromJson(re.getResponse().getEntity().getText(), Error.class);
                        msg = err.getCause() + Constants.MESSAGE.getString("error_bad_request");
                    } catch (IOException err) {
                        // do nothing.
                    }
                }
                System.out.println(msg);
            } else {
                System.out.println(e.getMessage());
            }
            exit(1);
        }
    };
}
