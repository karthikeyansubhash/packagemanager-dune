package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WebServiceInfo {
    UUID uuid;
    ArrayList<LocalizedString> titles;
    ArrayList<LocalizedString> descriptions;
    List<WebServiceEndPoint> webServiceEndPointList = new ArrayList<>();

    public WebServiceInfo() {
        uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ArrayList<LocalizedString> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<LocalizedString> titles) {
        this.titles = titles;
    }

    public ArrayList<LocalizedString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<LocalizedString> descriptions) {
        this.descriptions = descriptions;
    }

    public List<WebServiceEndPoint> getWebServiceEndPointList() {
        return webServiceEndPointList;
    }

    public void setWebServiceEndPointList(List<WebServiceEndPoint> webServiceEndPointList) {
        this.webServiceEndPointList = webServiceEndPointList;
    }

    public void addTitle(LocalizedString title) {
        boolean isExist = false;
        if(this.titles == null) this.titles = new ArrayList<>();
        for(LocalizedString localeTitle: this.titles){
            if(localeTitle.getCode().equals(title.getCode())){
                isExist = true;
                localeTitle.setValue(title.getValue());
                break;
            }
        }
        if(!isExist){
            this.titles.add(0, title);
        }
    }

    public void addDescription(LocalizedString description){
        boolean isExist = false;
        if(this.descriptions == null) this.descriptions = new ArrayList<>();
        for(LocalizedString localeDesc: this.descriptions){
            if(localeDesc.getCode().equals(description.getCode())){
                isExist = true;
                localeDesc.setValue(description.getValue());
                break;
            }
        }

        if(!isExist){
            this.descriptions.add(0, description);
        }
    }

    public void addWebServiceEndPoint(WebServiceEndPoint webServiceEndPoint) {
        boolean isExist = false;
        for(WebServiceEndPoint endPoint : this.webServiceEndPointList){
            if (endPoint.equals(webServiceEndPoint)) {
                isExist = true;
                break;
            }
        }
        if(!isExist) {
            this.webServiceEndPointList.add(webServiceEndPoint);
        }
    }
}
