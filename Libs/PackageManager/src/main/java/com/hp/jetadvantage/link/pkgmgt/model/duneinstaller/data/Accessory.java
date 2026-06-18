package com.hp.jetadvantage.link.pkgmgt.model.duneinstaller.data;

import com.hp.ext.service.usbAccessories.RegistrationKind;
import com.hp.ext.service.usbAccessories.UsbRegistrationIdentification;

public class Accessory {

    String productId;
    String vendorId;
    String registrationType;
    String serialNumber;

    public Accessory(UsbRegistrationIdentification accessoryProvider) {
        this.productId = accessoryProvider.getProductId().toString();
        this.vendorId = accessoryProvider.getVendorId().toString();
        if (RegistrationKind.RkOwned.equals(accessoryProvider.getRegistrationKind())) {
            this.registrationType = "OWNED";
        } else if (RegistrationKind.RkShared.equals(accessoryProvider.getRegistrationKind())) {
            this.registrationType = "SHARED";
        }
        if (accessoryProvider.getSerialNumber() != null) {
            this.serialNumber = accessoryProvider.getSerialNumber().toString();
        }
    }
}
