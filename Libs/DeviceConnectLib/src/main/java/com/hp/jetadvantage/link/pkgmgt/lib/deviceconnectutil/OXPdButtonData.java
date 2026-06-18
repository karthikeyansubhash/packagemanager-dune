package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

import java.util.List;

public class OXPdButtonData {
    public String id;
    public String name;
    public List<LocalizedString> title;
    public List<LocalizedString> description;
    public int requestedPosition;
    public BrowserTargetCommon browserTarget;
    public List<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons;

    public OXPdButtonData() {
    }

    public OXPdButtonData(String id, String name, List<LocalizedString> title, List<LocalizedString> description, int requestedPosition, BrowserTargetCommon browserTarget, List<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.description = description;
        this.requestedPosition = requestedPosition;
        this.browserTarget = browserTarget;
        this.lookAndFeelSpecificIcons = lookAndFeelSpecificIcons;
    }

    @Override
    public String toString() {
        return "OXPdButtonData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", title=" + title +
                ", description=" + description +
                ", requestedPosition=" + requestedPosition +
                ", browserTarget=" + browserTarget +
                ", lookAndFeelSpecificIcons=" + lookAndFeelSpecificIcons +
                '}';
    }
}
