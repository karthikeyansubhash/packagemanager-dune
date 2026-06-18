package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root (name = "webApplication")
public class WebApplication {

    @Element
    private String uri;

    @Element
    private String binding;

    @Element (required = false)
    private NetworkCredentials networkCredentials;

    public String getUri() { return uri; }

    public void setUri(String uri) { this.uri = uri; }

    public String getBinding() { return binding; }

    public void setBinding(String binding) { this.binding = binding; }

    public NetworkCredentials getNetworkCredentials() { return networkCredentials; }

    public void setNetworkCredentials(NetworkCredentials networkCredentials) { this.networkCredentials = networkCredentials; }
}
