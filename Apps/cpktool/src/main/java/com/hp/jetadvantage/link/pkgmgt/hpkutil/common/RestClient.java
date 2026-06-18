package com.hp.jetadvantage.link.pkgmgt.hpkutil.common;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.installer.InstallListener;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Installer;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.InstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceMode;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.Error;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstaller;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.Constants;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.JsonHelper;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.utils.SecurityHelper;
import com.hp.jetadvantage.link.pkgmgt.lib.PlatformType;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.*;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

public class RestClient {
    private static Context context;
    private static Client client;
    private static ClientResource resource;

    protected static synchronized ClientResource newClientResource(String url) {
        if (context == null) {
            Engine.setRestletLogLevel(Level.OFF);

            context = new Context();
            ignoreSSLContext(context);

            client = new Client(Protocol.HTTP);
        }

        resource = new ClientResource(context, url);
        resource.setNext(client);

        addAuthentication(resource);
        return resource;
    }

    protected static synchronized void closeClient() {
        try {
            if (client != null) {
                client.stop();
                client = null;
            }
            if (resource != null) {
                resource.release();
                resource = null;
            }
            context = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void waitForComplete(String installerUri, InstallListener listener) throws IOException {
        try {
            PackageInstaller packageInstaller = null;
            int tries = 30; // wait only 30*1000ms = 30 seconds
            do {
                sleep(5000);
                ClientResource resource = newClientResource(installerUri);
                Representation response = resource.get();

                if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                    packageInstaller = JsonHelper.fromJson(response.getStream(), PackageInstaller.class);
                    if (packageInstaller.getState() != PackageInstallerState.psFailed) {
                        listener.status(packageInstaller.getState(), null);
                    }
                } else {
                    break;
                }
                closeClient();
                tries--;
            }
            while (packageInstaller != null && packageInstaller.getState().equals(PackageInstallerState.psInProgress) && tries > 0);

            if (packageInstaller != null && packageInstaller.getState().equals(PackageInstallerState.psFailed)) {
                Error err = JsonHelper.fromJson(packageInstaller.getError(), Error.class);
                if (err.getCause().contains("code -15")) {
                    listener.status(PackageInstallerState.psFailed, Constants.MESSAGE.getString("error_code_15"));
                } else {
                    listener.status(PackageInstallerState.psFailed, err.getCause());
                }
            }
            listener.finished();
        } catch (ResourceException e) {
            throw e;
        } finally {
            closeClient();
        }
    }

    private static void addAuthentication(ClientResource resource) {
        resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, Constants.DEFAULT_USER_NAME, Constants.DEFAULT_USER_PASSWORD);
    }

    private static void ignoreSSLContext(Context context) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, new TrustManager[]{tm}, null);
            context.getAttributes().put("sslContextFactory", new SslContextFactory() {
                public void init(Series<Parameter> parameters) {
                }

                public SSLContext createSslContext() {
                    return sslContext;
                }
            });
            context.getAttributes().put("hostnameVerifier", new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static TrustManager tm = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType)
                throws CertificateException {
            // This will never throw an exception.
            // This doesn't check anything at all: it's insecure.
        }
    };

    private static void sleep(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            //
        }
    }

    public void setUsername(String userName) {
        if (userName.isEmpty()) {
            Constants.DEFAULT_USER_NAME = userName;
        }
    }

    public void setUserPassword(String userPassword) {
        Constants.DEFAULT_USER_PASSWORD = userPassword;
    }

    protected boolean isInstallerStateIdle(String pkgmgtUrl) throws Exception {
        ClientResource resource = newClientResource(pkgmgtUrl + "/installer");
        try {
            Constants.DEFAULT_DEVICE_MODE = DeviceMode.LINKFORWEB;
            Representation response = resource.get();
            if (resource.getStatus().equals(Status.SUCCESS_OK) && response != null) {
                Installer installer = JsonHelper.fromJson(response.getStream(), Installer.class);
                if (installer != null && installer.getState().equals(InstallerState.insIdle)) {
                    return true;
                }
            }
        } catch (ResourceException e) {
            if (e.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED) || e.getStatus().equals(Status.CLIENT_ERROR_FORBIDDEN)) {
                throw new IllegalArgumentException(Constants.MESSAGE.getString("msg_pw_error"));
            } else if (e.getStatus().equals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE)) {
                throw new Exception(Constants.MESSAGE.getString("error_service_unavailable"));
            } else if (e.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
                if (PlatformType.LinkForWeb.equals(Constants.DEFAULT_PLATFORM_TYPE)) {
                    Constants.DEFAULT_DEVICE_MODE = DeviceMode.OXPD;
                    throw new Exception(Constants.MESSAGE.getString("link_platform_is_not_enabled"));
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        } finally {
            closeClient();
        }
        return false;
    }

    protected ClientResource getClientResource(String uri) {
        ClientResource resource = newClientResource(uri);
        Request req = resource.getRequest();
        Series<Header> headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add(Constants.HEADER_AUTHORIZATION, "Basic " + SecurityHelper.encodeAuth(Constants.DEFAULT_USER_PASSWORD));
        headerValue.add(Constants.HEADER_CONTENT_TYPE, "application/soap+xml; charset=utf-8");
        return resource;
    }
}
