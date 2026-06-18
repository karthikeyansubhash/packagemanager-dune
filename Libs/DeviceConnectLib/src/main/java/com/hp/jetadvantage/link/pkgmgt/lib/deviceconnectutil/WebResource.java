package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

public class WebResource {
    public String uri;
    public String binding;
    public NetworkCredentials networkCredentials;

    public WebResource() {
    }

    public WebResource(String uri, String binding, NetworkCredentials networkCredentials) {
        this.uri = uri;
        this.binding = binding;
        this.networkCredentials = networkCredentials;
    }

    public String toString() {
        return "[" + "uri=" + this.uri + ", " + "binding=" + (this.binding == null?"null":this.binding) + ", " + "networkCredentials=" + (this.networkCredentials == null?"null":this.networkCredentials.toString()) + "]";
    }
}
