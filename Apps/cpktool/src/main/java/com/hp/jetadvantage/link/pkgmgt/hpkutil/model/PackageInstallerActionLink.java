package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public class PackageInstallerActionLink {
    private final PackageInstallerActionType rel;
    private final ActionMethod method;
    private final String href;

    public PackageInstallerActionLink(PackageInstallerActionType rel, ActionMethod method, String href) {
        this.rel = rel;
        this.method = method;
        this.href = href;
    }

    public PackageInstallerActionType getRel() {
        return rel;
    }

    public ActionMethod getMethod() {
        return method;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "PackageInstallerActionLink{" +
                "rel=" + rel +
                ", method=" + method +
                ", href='" + href + '\'' +
                '}';
    }
}
