package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

public class LookAndFeelSpecificIcon {
    public String lookAndFeel;
    public String icon;

    public LookAndFeelSpecificIcon() {
    }

    public LookAndFeelSpecificIcon(String lookAndFeel, String icon) {
        this.lookAndFeel = lookAndFeel;
        this.icon = icon;
    }

    public String toString() {
        return "[" + "lookAndFeel=" + this.lookAndFeel + ", " + "icon=" + this.icon + "]";
    }
}