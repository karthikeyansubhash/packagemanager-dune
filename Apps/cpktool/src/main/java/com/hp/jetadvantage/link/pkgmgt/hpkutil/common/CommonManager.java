package com.hp.jetadvantage.link.pkgmgt.hpkutil.common;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.OXPdMessageHelper;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;

import static com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants.*;

public class CommonManager extends RestClient {
    private String host;
    protected String getHost() { return this.host; }
    private static final int PORT = Constants.DEFAULT_PORT;
    private static final String PKGMGT_URI_CONTEXT = Constants.DEFAULT_PKGMGT_URI;

    public CommonManager(String host) {
        this.host = host;
    }

    protected String getPkgMgtUrl() {
        return Constants.DEFAULT_SCHEME + this.host + ":" + PORT + PKGMGT_URI_CONTEXT;
    }

    protected String getDeviceConfigurationServiceUrl() {
        return Constants.DEFAULT_SCHEME + this.host + ":" + PORT + DEFAULT_DEVICE_CONFIGURATION;
    }

    protected String getConfigurationServiceUrl() {
        return Constants.DEFAULT_SCHEME + this.host + ":" + EXTERNAL_PORT + DEFAULT_CONFIGURATION_SERVICE;
    }

    protected String getSystemConfigurationUrl() {
        return Constants.DEFAULT_SCHEME + this.host + ":" + EXTERNAL_PORT + DEFAULT_SYSTEM_CONFIGURATION;
    }

    protected boolean setTrustSites(String trustedUri) throws Exception{
        // create clientResource for get trustSites
        if(trustedUri != null) {
            URI uri = new URI(trustedUri);
            ClientResource resource = getClientResource(getConfigurationServiceUrl());
            String getTrustSiteBody = OXPdMessageHelper.getSoapTrustSites(getConfigurationServiceUrl());

            Representation response = resource.post(getTrustSiteBody);
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                String result = IoUtils.toString(response.getStream(), CharacterSet.UTF_8);
                closeClient();
                ArrayList<URI> trustSites = getTrustSites(result);
                // 1. add original uri
                trustSites.add(uri);
                // 2. add asterisk uri
                trustSites.add(getAstreriskUri(uri));
                // 3. remove same trusted site uri
                ArrayList<String> urlList = new ArrayList<>();
                for(URI trustUri: trustSites){
                    urlList.add((trustUri.getHost() == null)? trustUri.getPath() : trustUri.getHost());
                }
                HashSet<String> trustSiteData = new HashSet<>(urlList);
                urlList = new ArrayList<>(trustSiteData);
                // 4. create body
                String setTrustSiteBody = OXPdMessageHelper.setSoapTrustSites(getConfigurationServiceUrl(), urlList);
                // 5. post
                Representation setResponse = resource.post(setTrustSiteBody);
                if (resource.getStatus().equals(Status.SUCCESS_OK) && setResponse != null) {
                    return true;
                }
            }
            closeClient();
        }
        return false;
    }

    protected boolean setCors() {
        ClientResource resource = getClientResource(getSystemConfigurationUrl());
        String corsBody = OXPdMessageHelper.setSoapCors(getSystemConfigurationUrl(), "true");
        // post corsBody
        Representation response = resource.post(corsBody);
        if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
            closeClient();
            return true;
        } else {
            closeClient();
            return false;
        }
    }

    private URI getAstreriskUri(URI trustedUri) throws Exception{
        String uri = (trustedUri.getHost() == null)? trustedUri.getPath() : trustedUri.getHost();
        String[] splitUri = uri.split("\\.");
        if(splitUri.length > 2) {
            splitUri[0] = "*";
        } else {
            String[] newSplitUri = new String[splitUri.length + 1];
            int index = 0;
            newSplitUri[index++] = "*";
            for(String split: splitUri){
                newSplitUri[index++] = split;
            }
            splitUri = newSplitUri;
        }
        String newUri = splitUri[0];
        for(int i=1; i < splitUri.length; i++ ){
            newUri += "." + splitUri[i];
        }
        return new URI(newUri);
    }

    private ArrayList<URI> getTrustSites(String resource) throws Exception{
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
