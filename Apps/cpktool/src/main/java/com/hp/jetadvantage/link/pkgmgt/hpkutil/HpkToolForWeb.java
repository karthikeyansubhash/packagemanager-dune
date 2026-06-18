package com.hp.jetadvantage.link.pkgmgt.hpkutil;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.Connector;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import joptsimple.OptionSet;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.UUID;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.*;

public class HpkToolForWeb extends HpkToolApplication {
    /**
     * <p>HPKToolForWeb Main Method</p>
     * <p>{@link #processCLI}</p>
     * @param args command string
     */
    public static void main(String[] args) {
        final PlatformType LOCAL_PLATFORM_TYPE = PlatformType.LinkForWeb;
        Constants.DEFAULT_PLATFORM_TYPE = PlatformType.LinkForWeb;
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
            if(TEST == null) {
                exit(0);
            }
        }
    }

    /**
     * <p>Parse and execute CLI command</p>
     * <pre>
     * --create             Create HPK file from specified field values
     * --xml                Create HPK file using existing Web application manifest file
     * --generate           Generate template for hpk.xml
     * --install            Install HPK file to device
     * --uninstall          Delete the installed application
     * --help               Shows this help
     * --app-list           Retrieve the application list
     * --app-detail         Retrieve the details of application
     * </pre>
     * @param args command string
     * @param platformType platformType
     * @throws IllegalArgumentException Wrong command, there are no command or two or more commands
     * @throws Throwable Throw Throwable form sub methods.
     */
    private static void processCLI(String[] args, PlatformType platformType) throws Throwable{
        OptionSet cmd = getDefaultOptionParser().parse(args);
        switch (getMainCommand(cmd)) {
            case HELP:
                printHelp(platformType);
                break;
            case CREATE:
                createHPK(cmd, platformType);
                break;
            case XML:
                createXML(cmd, platformType);
                break;
            case INSTALL:
                isOXPd(cmd);
                installHPK(cmd);
                break;
            case UNINSTALL:
                isOXPd(cmd);
                uninstallHPK(cmd);
                break;
            case TEMPLATE:
                HpkFileHelper.generateTemplateByPlatformType(platformType);
                break;
            case APP_LIST:
                isOXPd(cmd);
                getAppList(cmd);
                break;
            case APP_DETAIL:
                getAppDetail(cmd);
                break;
            default:
                throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_main_command"));
        }
    }

    /**
     * <p>Create HPK File</p>
     * <p>Check before change: {@link HpkTool#createHPK}</p>
     * <pre>
     * Options: for --create:
     * --output &lt;hpk-file&gt;           Output HPK package file path
     * --installfile &lt;install-file&gt;  Install file path
     * --uuid &lt;uuid&gt;                 UUID associated with Application (RFC 4122)
     * --name &lt;app-name&gt;             Name of Application that will be shown in Application Galleries
     * --vendor &lt;vendor&gt;             Name of Vendor that developed Application
     * --date &lt;yyyymmdd&gt;             Date of creation package for Application (default current date)
     * </pre>
     * @param cmd command set
     * @param platformType platformType
     * @throws IllegalArgumentException Throw when command is wrong.
     * @throws Exception Throw exception from sub methods.
     */
    @SuppressWarnings("JavadocReference")
    private static void createHPK(OptionSet cmd, PlatformType platformType) throws Exception {
        final String XSD_VERSION_2_1 = "v2.1";
        String outputCpk = (String) cmd.valueOf(OPT_OUTPUT);
        String xmlPath = (String) cmd.valueOf(OPT_INSTALLFILE);

        Connector connector = new Connector(XSD_VERSION_2_1);
        connector.setPlatformType(platformType);

        if (!"xml".equalsIgnoreCase(Utils.getExtension(xmlPath))) {
            throw new IllegalArgumentException(Constants.MESSAGE.getString("wrong_value_for_option") + OPT_INSTALLFILE + ": " + xmlPath);
        }
        File installFile = new File(xmlPath);
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

        HpkFileHelper.createCpk(new File(outputCpk), connector, installFile, null);
        System.out.println(Constants.MESSAGE.getString("msg_success"));
    }

    /**
     * <p>For Hpk Tool for web, check ixOXPd option and set DEFAULT_DEVICE_MODE.</p>
     * @param cmd command set
     */
    private static void isOXPd(OptionSet cmd) {
        String isOXPd = (String) cmd.valueOf(OPT_NON_LINK);
        if ("true".equals(isOXPd)) {
            Constants.DEFAULT_DEVICE_MODE = DeviceMode.OXPD;
        }
    }
}
