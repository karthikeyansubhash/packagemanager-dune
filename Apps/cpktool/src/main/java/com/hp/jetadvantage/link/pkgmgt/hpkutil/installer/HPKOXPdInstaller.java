package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.OXPdButtonHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.OXPdMessageHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.File;

public class HPKOXPdInstaller extends CommonManager {

    public HPKOXPdInstaller(String host) {
        super(host);
    }

    public void install(File installFile, InstallListener listener) {
        try {
            listener.status(PackageInstallerState.psInProgress, null);
            sendRequest(installFile);
            listener.status(PackageInstallerState.psCompleted, null);
            listener.finished();
        } catch (Exception e) {
            listener.status(PackageInstallerState.psFailed, e.getMessage());
        }
    }

    public boolean sendRequest(File installFile) throws Exception {
        String iconSize = getIconSize();
        ClientResource resource = getClientResource(getConfigurationServiceUrl());

        HpkFile hpkFile = null;
        ButtonInfo buttonInfo;
        try {
            hpkFile = new HpkFile(installFile);
            buttonInfo = Utils.getButtonInfo(hpkFile.getInstallFile(), getClass());
        } finally {
            if (hpkFile != null) {
                try {
                    hpkFile.close();
                } catch (Exception e) {}
            }
        }

        String buttonBody = OXPdButtonHelper.getOXPdButtonString(buttonInfo, iconSize);
        String body = OXPdMessageHelper.getOXPdInstallBody(getConfigurationServiceUrl(), buttonBody);
        Representation response = resource.post(body);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            closeClient();
            try {
                setTrustSites(buttonInfo.getBrowserTarget().getWebApplication().getUri());
            } catch (Exception e) {
            }
            try {
                setCors();
            } catch (Exception e) {
            }
            return true;
        } else {
            closeClient();
            throw new Exception(resource.getStatus().toString());
        }
    }

    private String getIconSize() {
        String iconSize = getIconSizeFromUIAttributes();
        if (iconSize != null) {
            return iconSize;
        }
        return getIconSizeFromUIProfile();
    }

    private String getIconSizeFromUIAttributes() {
        try {
            ClientResource resource = getClientResource(getConfigurationServiceUrl());
            String body = OXPdMessageHelper.getSoapUIAttributes(getConfigurationServiceUrl());
            Representation response = resource.post(body);
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                String result = IoUtils.toString(response.getStream(), CharacterSet.UTF_8);

                String iconSize = findKeyValue("userInterfaceId", result);
                String height = findKeyValue("buttonIconHeight", result);
                String width = findKeyValue("buttonIconWidth", result);

                if (iconSize == null
                        || height == null
                        || width == null) {
                    return null;
                } else {
                    return iconSize + ":" + height + "x" + width;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeClient();
        }
        return null;
    }

    private String findKeyValue(String key, String resource) {
        String size = null;
        String keyTag = "<key>" + key + "</key>";
        String valueStringBegining = "<valueString>";
        String valueStringEnd = "</valueString>";
        if (resource.indexOf(keyTag) > 0) {
            int point = resource.indexOf(keyTag) + keyTag.length();
            size = resource.substring(resource.indexOf(valueStringBegining, point) + valueStringBegining.length(), resource.indexOf(valueStringEnd, point));
        }
        return size;
    }

    private String getIconSizeFromUIProfile() {
        ClientResource resource = getClientResource(getConfigurationServiceUrl());
        String body = OXPdMessageHelper.getSoapUIProfile(getConfigurationServiceUrl());
        Representation response = resource.post(body);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            try {
                String result = IoUtils.toString(response.getStream(), CharacterSet.UTF_8);
                closeClient();
                return getLookAndFeel(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        closeClient();
        return null;
    }

    private String getLookAndFeel(String resource) {
        String size = null;
        String lookAndFeelBegining = "<lookAndFeel>";
        String lookAndFeelEnd = "</lookAndFeel>";
        if (resource.indexOf(lookAndFeelBegining) > 0) {
            int point = resource.indexOf(lookAndFeelBegining) + lookAndFeelBegining.length();
            size = resource.substring(point, resource.indexOf(lookAndFeelEnd, point));
        }
        return size;
    }
}
