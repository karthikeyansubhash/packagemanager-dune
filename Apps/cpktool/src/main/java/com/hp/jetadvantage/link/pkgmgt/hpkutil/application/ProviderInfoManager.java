package com.hp.jetadvantage.link.pkgmgt.hpkutil.application;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ProviderInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.util.ArrayList;
import java.util.List;

public class ProviderInfoManager extends CommonManager  {

    public ProviderInfoManager(String host) {
        super(host);
    }

    public List<ProviderInfo> getProviderList(TaskInterface taskInterface) throws Exception {

        List<ProviderInfo> appInfoList = new ArrayList();
        if (!isInstallerStateIdle(getPkgMgtUrl())) {
            taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("device_busy")));
            return null;
        }

        try {
            ClientResource resource = newClientResource(getPkgMgtUrl() + Constants.DEFAULT_PROVIDERS);
            Representation response = resource.get();
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                appInfoList = JsonHelper.fromJsonToList(response.getStream(), ProviderInfo[].class);
                taskInterface.onSucceed(appInfoList);
            } else {
                taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("msg_fail_connect")));
            }
        } catch (ResourceException ex) {
            taskInterface.onFailed(ex);
        } finally {
            closeClient();
        }

        return appInfoList;
    }
}
