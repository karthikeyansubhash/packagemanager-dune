package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import com.hp.jetadvantage.link.pkgmgt.lib.UUIDConverter;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;

import java.util.ArrayList;
import java.util.UUID;

@Root (name = "FleetTopLevelButtonRecord")
@NamespaceList ({
        @Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi"),
        @Namespace(reference = "http://www.w3.org/2001/XMLSchema", prefix = "xsd"),
        @Namespace(reference = "http://www.hp.com/schemas/jetadvantage/link/hpk/v2.1")
})
public class ButtonInfo {

    @Element
    @Convert(UUIDConverter.class)
    private UUID id;

    @Element
    private String name;

    @ElementList
    private ArrayList<LocalizedString> title;

    @ElementList
    private ArrayList<LocalizedString> description;

    @Element
    private String requestedPosition;

    @Element
    private BrowserTarget browserTarget;

    @ElementList (required = false)
    private ArrayList<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons;

    public ButtonInfo() {
        title = new ArrayList<>();
        description = new ArrayList<>();
    }

    public UUID getUuid() {
        return id;
    }

    public void setUuid(UUID id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<LocalizedString> getTitle() { return title; }

    public void setTitle(ArrayList<LocalizedString> title) { this.title = title; }

    public void addTitle(LocalizedString title) {
        boolean isExist = false;
        if(this.title == null) this.title = new ArrayList<>();
        for(LocalizedString localeTitle: this.title){
            if(localeTitle.getCode().equals(title.getCode())){
                isExist = true;
                localeTitle.setValue(title.getValue());
                break;
            }
        }
        if(!isExist){
            this.title.add(0, title);
        }
    }

    public ArrayList<LocalizedString> getDescription() { return description; }

    public void setDescription(ArrayList<LocalizedString>  description) { this.description = description; }

    public void addDescription(LocalizedString description){
        boolean isExist = false;
        if(this.description == null) this.description = new ArrayList<>();
        for(LocalizedString localeDesc: this.description){
            if(localeDesc.getCode().equals(description.getCode())){
                isExist = true;
                localeDesc.setValue(description.getValue());
                break;
            }
        }

        if(!isExist){
            this.description.add(0, description);
        }
    }

    public String getRequestedPosition() { return requestedPosition; }

    public void setRequestedPosition(String requestedPosition) { this.requestedPosition = requestedPosition; }

    public BrowserTarget getBrowserTarget() { return browserTarget; }

    public void setBrowserTarget(BrowserTarget browserTarget) { this.browserTarget = browserTarget; }

    public ArrayList<LookAndFeelSpecificIcon> getLookAndFeelSpecificIcons() { return lookAndFeelSpecificIcons; }

    public void setLookAndFeelSpecificIcons(ArrayList<LookAndFeelSpecificIcon> lookAndFeelSpecificIcons) { this.lookAndFeelSpecificIcons = lookAndFeelSpecificIcons; }
}
