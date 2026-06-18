package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ClientId;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Error;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class HPKUninstaller extends CommonManager {
    public HPKUninstaller(String host) {
        super(host);
    }

    public void uninstall(String Uuid, InstallListener listener) {
        try {
            listener.status(PackageInstallerState.psInProgress, "Start to uninstall file: " + Uuid);
            if (!isInstallerStateIdle(getPkgMgtUrl())) {
                listener.status(PackageInstallerState.psFailed, "Device is busy. please try to after sometime.");
                return;
            }
            String uninstallUri = uninstallCpkFile(Uuid, ClientId.JAMC);
            waitForComplete(uninstallUri, listener);
        } catch (Exception e) {
            e.printStackTrace();
            listener.status(PackageInstallerState.psFailed, e.getMessage());
        } finally {
            closeClient();
        }
    }

    private String uninstallCpkFile(String uuid, String clientId) throws Exception {
        ClientResource resource = newClientResource(getPkgMgtUrl() + "/installer/uninstall");

        if (uuid != null) resource.addQueryParameter("uuid", uuid);
        if (clientId != null) resource.addQueryParameter("clientId", clientId);

        try {
            resource.post(null);
            return resource.getLocationRef().toString();
        } catch (ResourceException e) {
            if (e.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
                Error err = JsonHelper.fromJson(e.getResponse().getEntity().getText(), Error.class);

                if (err == null) throw e;
                else if (err.getCause() != null && err.getCause().toLowerCase().contains("certificate")) {
                    throw new Exception(err.getCause() +
                            "\r\n" + Constants.MESSAGE.getString("error_bad_request"));
                } else {
                    throw new Exception(err.getCause());
                }
            } else {
                throw e;
            }
        } finally {
            closeClient();
        }
    }
}
