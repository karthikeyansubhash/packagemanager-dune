package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.common.CommonManager;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ButtonInfo;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.ClientId;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Error;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.InstallSource;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Utils;
import com.hp.jetadvantage.link.pkgmgt.lib.HpkFile;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HPKInstaller extends CommonManager {

    public HPKInstaller(String host) {
        super(host);
    }

    public void install(File installFile, InstallListener listener) {
        install(installFile, Boolean.FALSE, listener);
    }

    public void install(File installFile, Boolean forceInstall, InstallListener listener) {
        try {
            listener.status(PackageInstallerState.psSending, null);
            if (!isInstallerStateIdle(getPkgMgtUrl())) {
                listener.status(PackageInstallerState.psFailed, "Device is busy. please try to after sometime.");
                return;
            }
            String installerUri = installHpkFile(installFile, forceInstall, listener);
            listener.status(PackageInstallerState.psInProgress, null);
            waitForComplete(installerUri, listener);
        } catch (Exception e) {
            listener.status(PackageInstallerState.psFailed, e.getMessage());
        } finally {
            closeClient();
        }
    }

    private String installHpkFile(File installFile, Boolean forceInstall, InstallListener listener) throws Exception {
        return installHpkFile(installFile, ClientId.JAMC, InstallSource.INSTALL_SOURCE_STANDARD_REPOSITORY, forceInstall, true, listener);
    }

    private String installHpkFile(File hpkFile, String clientId, String installSource, Boolean forceInstall, Boolean acceptPermissions, InstallListener listener) throws Exception {
        HttpHost targetHost = new HttpHost(getHost(), Constants.DEFAULT_PORT, "https");

        List<NameValuePair> urlParameters = new ArrayList<>();
        if (clientId != null) urlParameters.add(new BasicNameValuePair("clientId", clientId));
        if (installSource != null) urlParameters.add(new BasicNameValuePair("installSource", installSource));
        if (forceInstall != null) urlParameters.add(new BasicNameValuePair("forceInstall", forceInstall.toString()));
        if (acceptPermissions != null) urlParameters.add(new BasicNameValuePair("acceptPermissions", acceptPermissions.toString()));
        URIBuilder uriBuilder = new URIBuilder(getPkgMgtUrl() + "/installer/install");
        uriBuilder.addParameters(urlParameters);
        HttpPost httpPost = new HttpPost(uriBuilder.build());

        if (!Utils.checkSupportFormat(hpkFile).equals(Constants.DEFAULT_PLATFORM_TYPE)) {
            throw new Exception(Constants.MESSAGE.getString("error_not_support_format"));
        }
        final long[] fileLength = {hpkFile.length()};
        final long[] sentLength = {0};
        CountableFileBody fileBody = new CountableFileBody(hpkFile, ContentType.create("application/vnd.hp.package-archive"), hpkFile.getName());
        fileBody.setStreamListener(new StreamListener() {
            @Override
            public void counterChanged(int delta) {
                sentLength[0] += delta;
                double percent = (double) 100 * sentLength[0] / fileLength[0];
                listener.status(PackageInstallerState.psSending, String.format("%.2f%%", percent));
            }
        });
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        FormBodyPart formBodyPart = FormBodyPartBuilder.create()
                .setName("file")
                .setBody(fileBody)
                .build();
        multipartEntityBuilder.addPart(formBodyPart);
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httpPost.setEntity(httpEntity);

        HpkFile hpk = null;

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, new TrustManager[]{tm}, null);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(Constants.DEFAULT_USER_NAME, Constants.DEFAULT_USER_PASSWORD));

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setCredentialsProvider(credentialsProvider);
        httpClientContext.setAuthCache(authCache);

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        try {
            CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, httpClientContext);
            if (response.getStatusLine().getStatusCode() == 202) {
                hpk = new HpkFile(hpkFile);
                String installerUri = response.getFirstHeader("Location").getValue().replaceFirst(":443", "");

                if (PlatformType.LinkForWeb.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
                    setTrustSiteAndCors(hpk.getInstallFile());
                }
                return installerUri;
            } else if (response.getStatusLine().getStatusCode() == 400) {
                Error err = JsonHelper.fromJson(response.getEntity().getContent(), Error.class);
                if (err == null) throw new Exception(response.getStatusLine().toString());
                else if (err.getCause() != null && err.getCause().toLowerCase().contains("certificate")) {
                    throw new Exception(err.getCause() +
                            "\r\n" + Constants.MESSAGE.getString("error_bad_request"));
                } else {
                    throw new Exception(err.getCause());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            httpClient.close();
            if (hpk != null) {
                try {
                    hpk.close();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private void setTrustSiteAndCors(InputStream installFile) {
        try {
            ButtonInfo buttonInfo = Utils.getButtonInfo(installFile, getClass());
            setTrustSites(buttonInfo.getBrowserTarget().getWebApplication().getUri());
        } catch (Exception e) {}
        try {
            setCors();
        } catch (Exception e) {}
    }
}
