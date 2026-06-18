
package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

public class NetworkCredentials {
    public String userName;
    public String password;
    public String domain;

    public NetworkCredentials() {
    }

    public NetworkCredentials(final String userName, final String password, final String domain) {
        this.userName = userName;
        this.password = password;
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "NetworkCredentials{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
