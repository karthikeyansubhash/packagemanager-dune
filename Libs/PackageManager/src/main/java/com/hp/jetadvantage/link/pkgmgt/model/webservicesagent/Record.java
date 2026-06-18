package com.hp.jetadvantage.link.pkgmgt.model.webservicesagent;

import java.util.List;

public class Record {
    private String pkgName;
    private List<WebServicesAgentRecord> webServicesAgentRecordList;

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public List<WebServicesAgentRecord> getWebServicesAgentRecordList() {
        return webServicesAgentRecordList;
    }

    public void setWebServicesAgentRecordList(List<WebServicesAgentRecord> webServicesAgentRecordList) {
        this.webServicesAgentRecordList = webServicesAgentRecordList;
    }
}
