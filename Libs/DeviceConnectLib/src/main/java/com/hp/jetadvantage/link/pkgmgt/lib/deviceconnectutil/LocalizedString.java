
package com.hp.jetadvantage.link.pkgmgt.lib.deviceconnectutil;

public class LocalizedString {
    public String code;
    public String value;

    public LocalizedString() {
    }

    public LocalizedString(final String code, final String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public String toString() {
        return "LocalizedString{" +
                "code='" + code + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
