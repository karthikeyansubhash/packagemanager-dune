package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import java.util.ArrayList;
import java.util.UUID;

public class StatisticsAgentInfo {
    UUID uuid;
    ArrayList<ExplicitLocalizedString> titles;
    ArrayList<ExplicitLocalizedString> descriptions;
    boolean ackRequiredForDelete;

    public StatisticsAgentInfo(){
        uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ArrayList<ExplicitLocalizedString> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<ExplicitLocalizedString> titles) {
        this.titles = titles;
    }

    public ArrayList<ExplicitLocalizedString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<ExplicitLocalizedString> descriptions) {
        this.descriptions = descriptions;
    }

    public boolean isAckRequiredForDelete() {
        return ackRequiredForDelete;
    }

    public void setAckRequiredForDelete(boolean ackRequiredForDelete) {
        this.ackRequiredForDelete = ackRequiredForDelete;
    }

    public void addTitle(ExplicitLocalizedString title) {
        boolean isExist = false;
        if(this.titles == null) this.titles = new ArrayList<>();
        for(ExplicitLocalizedString localeTitle: this.titles){
            if(localeTitle.getLanguageTag().equals(title.getLanguageTag())){
                isExist = true;
                localeTitle.setValue(title.getValue());
                break;
            }
        }
        if(!isExist){
            this.titles.add(0, title);
        }
    }

    public void addDescription(ExplicitLocalizedString description){
        boolean isExist = false;
        if(this.descriptions == null) this.descriptions = new ArrayList<>();
        for(ExplicitLocalizedString localeDesc: this.descriptions){
            if(localeDesc.getLanguageTag().equals(description.getLanguageTag())){
                isExist = true;
                localeDesc.setValue(description.getValue());
                break;
            }
        }

        if(!isExist){
            this.descriptions.add(0, description);
        }
    }
}
