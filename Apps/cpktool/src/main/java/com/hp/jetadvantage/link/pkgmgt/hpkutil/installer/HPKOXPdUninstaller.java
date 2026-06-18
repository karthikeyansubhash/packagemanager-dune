package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.OXPdMessageHelper;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.net.URI;
import java.util.ArrayList;

public class HPKOXPdUninstaller extends CommonManager {

    public HPKOXPdUninstaller(String host) {
        super(host);
    }

    public void uninstall(String uuid, InstallListener listener) {
        try {
            listener.status(PackageInstallerState.psInProgress, null);
            if(!isExistApp(uuid)) {
                throw new Exception(Constants.MESSAGE.getString("error_not_found"));
            }
            sendRequest(uuid);
            listener.status(PackageInstallerState.psCompleted, null);
            listener.finished();
        } catch (Exception e) {
            listener.status(PackageInstallerState.psFailed, e.getMessage());
        }
    }

    public boolean sendRequest(String uuid) throws Exception {
        ClientResource resource = getClientResource(getConfigurationServiceUrl());
        String body = OXPdMessageHelper.getOXPdUninstallBody(getConfigurationServiceUrl(), uuid);
        Representation response = resource.post(body);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            closeClient();
            return true;
        } else {
            throw new Exception(resource.getStatus().toString());
        }
    }

    private boolean isExistApp(String uuid) throws Exception {
        ClientResource resource = getClientResource(getConfigurationServiceUrl());
        String body = OXPdMessageHelper.getTopLevelButtonRecord(getConfigurationServiceUrl(), uuid);
        Representation response = resource.post(body);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            String result = IoUtils.toString(response.getStream(), CharacterSet.UTF_8);
            closeClient();
            String responseUUID = getUUID(result);
            if (responseUUID != null ||
                    uuid.equals(responseUUID)) {
                return true;
            }
        }
        return false;
    }

    private boolean deleteTrustSites(String trustSite) throws Exception {
        // create clientResource for get trustSites
        ClientResource resource = getClientResource(getConfigurationServiceUrl());
        String getTrustSiteBody = OXPdMessageHelper.getSoapTrustSites(getConfigurationServiceUrl());

        Representation response = resource.post(getTrustSiteBody);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            String result = IoUtils.toString(response.getStream(), CharacterSet.UTF_8);
            closeClient();
            ArrayList<URI> trustSites = getTrustSites(result);
            ArrayList<String> newTrustSites = new ArrayList<>();
            for (URI site : trustSites) {
                if (!site.getPath().equals(trustSite)) {
                    newTrustSites.add(site.getPath());
                }
            }
            String setTrustSiteBody = OXPdMessageHelper.setSoapTrustSites(getConfigurationServiceUrl(), newTrustSites);

            Representation setResponse = resource.post(setTrustSiteBody);
            if (resource.getStatus().equals(Status.SUCCESS_OK) && setResponse != null) {
                return true;
            }
        }
        closeClient();
        return false;
    }

    private String getUUID(String resource) {
        String uuid = null;
        String idBegining = "<id>";
        String idEnd = "</id>";
        if (resource.indexOf(idBegining) > 0) {
            int point = resource.indexOf(idBegining) + idBegining.length();
            uuid = resource.substring(point, resource.indexOf(idEnd, point));
        }
        return uuid;
    }

    private String getUri(String resource) throws Exception {
        URI uri = null;
        String uriBegining = "<uri";
        String urlBegined = ">";
        String uriEnd = "</uri>";
        if (resource.indexOf(uriBegining) > 0) {
            int point = resource.indexOf(urlBegined, resource.indexOf(uriBegining)) + urlBegined.length();
            uri = new URI(resource.substring(point, resource.indexOf(uriEnd, point)));
        }
        String uriString = null;
        if (uri != null) {
            uriString = (uri.getHost() == null)? uri.getPath() : uri.getHost();
        }
        return uriString;
    }

    private ArrayList<URI> getTrustSites(String resource) throws Exception {
        ArrayList<URI> trustSites = new ArrayList<>();
        String trustSiteBegining = "<trustedSite>";
        String trustSiteEnd = "</trustedSite>";
        int indexing = 0;

        while (resource.indexOf(trustSiteBegining, indexing) > 0) {
            int point = resource.indexOf(trustSiteBegining, indexing) + trustSiteBegining.length();
            trustSites.add(new URI(resource.substring(point, resource.indexOf(trustSiteEnd, point))));
            indexing = resource.indexOf(trustSiteEnd, point) + trustSiteEnd.length();
        }

        return trustSites;
    }
}
