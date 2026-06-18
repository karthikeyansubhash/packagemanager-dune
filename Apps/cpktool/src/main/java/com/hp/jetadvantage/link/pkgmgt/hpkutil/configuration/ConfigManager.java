package com.hp.jetadvantage.link.pkgmgt.hpkutil.configuration;

import com.google.gson.JsonObject;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Configuration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.util.UUID;

public class ConfigManager extends CommonManager {
    private UUID uuid;

    public ConfigManager(String host, UUID uuid) {
        super(host);
        this.uuid = uuid;
    }

    public Configuration getConfiguration(TaskInterface taskInterface) throws Exception {
        Configuration configuration = null;
        if (!isInstallerStateIdle(getPkgMgtUrl())) {
            taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("device_busy")));
            return null;
        }

        try {
            ClientResource resource = newClientResource(getPkgMgtUrl() + "/packages/" + uuid + Constants.DEFAULT_CONFIG_URI);
            Representation response = resource.get();
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                configuration = JsonHelper.fromJson(response.getStream(), Configuration.class);
                taskInterface.onSucceed(configuration);
            } else {
                taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("msg_fail_connect")));
            }
        } catch (ResourceException ex) {
            taskInterface.onFailed(ex);
        } finally {
            closeClient();
        }

        return configuration;
    }

    public Configuration putConfiguration(Configuration configuration, TaskInterface taskInterface) throws Exception {
        Configuration result = null;
        try {
            ClientResource resource = newClientResource(getPkgMgtUrl() + "/packages/" + uuid + Constants.DEFAULT_CONFIG_URI);
            Representation response = resource.put(JsonHelper.toJson(configuration));

            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                result = JsonHelper.fromJson(response.getStream(), Configuration.class);
                if(PlatformType.LinkForWeb.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
                    setTrustSiteAndCors(result.getData());
                }
                taskInterface.onSucceed(result);
            } else {
                taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("msg_fail_connect")));
            }
        } catch (ResourceException ex) {
            ex.printStackTrace();
            taskInterface.onFailed(ex);
        } finally {
            closeClient();
        }

        return result;
    }

    private void setTrustSiteAndCors(JsonObject data) {
        try {
            String uri = data.get("url").getAsString();
            setTrustSites(uri);
        } catch (Exception e) {}
        try {
            setCors();
        }catch (Exception e) {}
    }
}
