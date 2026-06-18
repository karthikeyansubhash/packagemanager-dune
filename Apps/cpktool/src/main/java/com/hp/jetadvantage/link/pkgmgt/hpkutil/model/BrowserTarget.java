package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root
public class BrowserTarget {

    @Element (required = false)
    private String initialPostQueryFormatString;

    @Element
    private WebApplication webApplication;

    public String getInitialPostQueryFormatString() { return initialPostQueryFormatString; }

    public void setInitialPostQueryFormatString(String initialPostQueryFormatString) { this.initialPostQueryFormatString = initialPostQueryFormatString; }

    public WebApplication getWebApplication() { return webApplication; }

    public void setWebApplication(WebApplication webApplication) { this.webApplication = webApplication; }
}
