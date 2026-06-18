package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root (name = "LocalizedString")
public class LocalizedString {

    @Element(required = false)
    public String languageTag;

    @Element(required = false)
    private String code;

    @Element
    private String value;

    public LocalizedString(){
    }

    public LocalizedString(String code, String value){
        this.code = code;
        this.value = value;
    }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public String getLanguageTag() {
        return languageTag;
    }

    public void setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
    }
}
