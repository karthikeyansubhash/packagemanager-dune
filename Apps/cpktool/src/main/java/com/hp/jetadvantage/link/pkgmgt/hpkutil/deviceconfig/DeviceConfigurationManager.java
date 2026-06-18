package com.hp.jetadvantage.link.pkgmgt.hpkutil.deviceconfig;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceConfiguration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Error;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class DeviceConfigurationManager extends CommonManager {

    public DeviceConfigurationManager(String host) {
        super(host);
    }


    public void getDeviceConfiguration(DeviceConfigurationListener listener) {
        try {
            listener.status(PackageInstallerState.psInProgress, null);
            if (!isInstallerStateIdle(getPkgMgtUrl())) {
                listener.status(PackageInstallerState.psFailed, "Device is busy. please try to after sometime.");
                return;
            }
            DeviceConfiguration result = getDeviceConfiguration();
            listener.complete(result);
        } catch (Exception e) {
            listener.status(PackageInstallerState.psFailed, e.getMessage());
        } finally {
            closeClient();
        }
    }

    private DeviceConfiguration getDeviceConfiguration() throws Exception {
        ClientResource resource = newClientResource(getDeviceConfigurationServiceUrl());

        try {
            Representation response = resource.get();
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                DeviceConfiguration deviceConfiguration = JsonHelper.fromJson(response.getStream(), DeviceConfiguration.class);
                return deviceConfiguration;
            } else {
                throw new Exception(Constants.MESSAGE.getString("msg_fail_connect"));
            }
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
