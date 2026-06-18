package com.hp.jetadvantage.link.pkgmgt.hpkutil.deviceconfig;

import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.DeviceConfiguration;
import com.hp.jetadvantage.link.pkgmgt.hpkutil.model.PackageInstallerState;

public interface DeviceConfigurationListener {
    void status(PackageInstallerState status, String cause);
    void complete(DeviceConfiguration deviceConfiguration);
    void finished();
}
