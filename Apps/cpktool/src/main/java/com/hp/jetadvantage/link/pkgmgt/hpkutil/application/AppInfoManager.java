package com.hp.jetadvantage.link.pkgmgt.hpkutil.application;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.AppInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.OXPdMessageHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.TaskInterface;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppInfoManager extends CommonManager {
    private UUID uuid;

    public AppInfoManager(String host, UUID uuid) {
        super(host);
        this.uuid = uuid;
    }

    public List<AppInfo> getAppInfoList(TaskInterface taskInterface) throws Exception {

        List<AppInfo> appInfoList = new ArrayList();
        if (!isInstallerStateIdle(getPkgMgtUrl())) {
            taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("device_busy")));
            return null;
        }

        try {
            ClientResource resource = newClientResource(getPkgMgtUrl() + Constants.DEFAULT_PACKAGES);
            Representation response = resource.get();
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                appInfoList = JsonHelper.fromJsonToList(response.getStream(), AppInfo[].class);
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

    public AppInfo getDetailAppInfo(TaskInterface taskInterface) throws Exception {

        AppInfo detailAppInfo = new AppInfo();
        try {
            ClientResource resource = newClientResource(getPkgMgtUrl() + "/packages/" + uuid + Constants.CONTENTFILTER_ALL);
            Representation response = resource.get();
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                detailAppInfo = JsonHelper.fromJson(response.getStream(), AppInfo.class);
                taskInterface.onSucceed(detailAppInfo);
            } else {
                taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("msg_fail_connect")));
            }
        } catch (ResourceException ex) {
            taskInterface.onFailed(ex);
        } finally {
            closeClient();
        }

        return detailAppInfo;
    }

    public List<AppInfo> getOXPdAppInfoList(TaskInterface taskInterface) throws Exception {
        List<AppInfo> appInfoList = new ArrayList();
        ClientResource resource = getClientResource(getConfigurationServiceUrl());
        String body = OXPdMessageHelper.getTopLevelButtonRecords(getConfigurationServiceUrl());
        Representation response = resource.post(body);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            String result = IoUtils.toString(response.getStream(), CharacterSet.UTF_8);
            appInfoList = getAppInfoFromOXPd(result);
            taskInterface.onSucceed(appInfoList);
        } else {
            taskInterface.onFailed(new Exception(Constants.MESSAGE.getString("device_not_support_applist")));
        }
        closeClient();
        return appInfoList;
    }

    private ArrayList<AppInfo> getAppInfoFromOXPd(String resource) throws Exception{
        ArrayList<AppInfo> appInfos = new ArrayList<>();
        String idBegining = "<id>";
        String idEnd = "</id>";
        String valueBegining = "<value>";
        String valueEnd = "</value>";
        int indexing = 0;

        while (resource.indexOf(idBegining, indexing) > 0) {
            int idPoint = resource.indexOf(idBegining, indexing) + idBegining.length();
            String id = resource.substring(idPoint, resource.indexOf(idEnd, idPoint));
            int valuePoint = resource.indexOf(valueBegining, idPoint) + valueBegining.length();
            String value = resource.substring(valuePoint, resource.indexOf(valueEnd, idPoint));
            AppInfo appInfo = new AppInfo();
            appInfo.setUuid(id);
            appInfo.setName(value);
            appInfo.setPlatformType(PlatformType.LinkForWeb.name());
            appInfos.add(appInfo);
            indexing = resource.indexOf(valueEnd, valuePoint) + valueEnd.length();
        }
        return appInfos;
    }
}