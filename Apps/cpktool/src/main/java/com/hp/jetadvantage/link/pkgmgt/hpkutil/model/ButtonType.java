package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public enum ButtonType {
    DETAIL("detail"),
    REMOVE("remove"),
    UNINSTALL("uninstall");

    private String string;

    ButtonType(String name) {
        string = name;
    }

    @Override
    public String toString() {
        return string;
    }
}
