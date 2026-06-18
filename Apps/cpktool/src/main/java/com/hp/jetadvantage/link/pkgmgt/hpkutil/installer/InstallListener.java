package com.hp.jetadvantage.link.pkgmgt.hpkutil.installer;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;

public interface InstallListener {
    void status(PackageInstallerState status, String cause);
    void finished();
}
