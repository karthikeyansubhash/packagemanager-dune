package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root (name = "LocalizedString")
public class ExplicitLocalizedString {

    @Element
    private String languageTag;

    @Element
    private String value;

    public ExplicitLocalizedString(){
    }

    public ExplicitLocalizedString(String languageTag, String value){
        this.languageTag = languageTag;
        this.value = value;
    }

    public String getLanguageTag() { return languageTag; }

    public void setLanguageTag(String languageTag) { this.languageTag = languageTag; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }
}
