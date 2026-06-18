package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root (name = "lookAndFeelSpecificIcon")
public class LookAndFeelSpecificIcon {

    @Element
    private String lookAndFeel;

    @Element
    private String icon;

    public void setLookAndFeel(String lookAndFeel) { this.lookAndFeel = lookAndFeel; }

    public String getLookAndFeel() { return lookAndFeel; }

    public void setIcon(String icon) { this.icon = icon; }

    public String getIcon() { return icon; }
}
