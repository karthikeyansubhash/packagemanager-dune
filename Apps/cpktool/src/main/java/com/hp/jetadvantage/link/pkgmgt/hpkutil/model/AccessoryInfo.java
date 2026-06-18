package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public class AccessoryInfo {
    RegistrationType registrationType;
    String vendorId;
    String productId;
    String serialNumber;

    public RegistrationType getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(RegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public void setRegistrationType(String registrationType) {
        if (registrationType != null) {
            if (RegistrationType.OWNED.name().equalsIgnoreCase(registrationType)) {
                this.registrationType = RegistrationType.OWNED;
            } else if (RegistrationType.SHARED.name().equalsIgnoreCase(registrationType)) {
                this.registrationType = RegistrationType.SHARED;
            }
        }
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public enum RegistrationType{
        OWNED,
        SHARED
    }
}
