package com.hp.jetadvantage.link.pkgmgt.model.statisticsagent;

public class Record {
    private String typeGUN;
    private  StatisticsAgentRecord value;

    public String getTypeGUN() {
        return typeGUN;
    }

    public void setTypeGUN(String typeGUN) {
        this.typeGUN = typeGUN;
    }

    public StatisticsAgentRecord getValue() {
        return value;
    }

    public void setValue(StatisticsAgentRecord value) {
        this.value = value;
    }
}
