package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

public class BrowserTargetCommon {
    public String initialPostQueryFormatString;
    public WebResource webApplication;

    public BrowserTargetCommon() {
    }

    public BrowserTargetCommon(String initialPostQueryFormatString, WebResource webApplication) {
        this.initialPostQueryFormatString = initialPostQueryFormatString;
        this.webApplication = webApplication;
    }

    public String toString() {
        return "[" + "initialPostQueryFormatString=" + this.initialPostQueryFormatString + ", " + "webApplication=" + (this.webApplication == null?"null":this.webApplication.toString()) + "]";
    }
}