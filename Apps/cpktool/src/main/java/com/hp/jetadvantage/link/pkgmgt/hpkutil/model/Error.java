package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

public class Error {
    public enum Code {
        ec_no_error, ec_invalid_package, ec_invalid_cert, ec_expired_cert,
        ec_not_enough_space, ec_file_io, ec_internal_err, ec_invalid_request,
        ec_authorization_error
    }

    public enum Entity {
        install, uninstall, permissions, config
    }

    private final Code code;
    private final String cause;
    private final Entity entity;

    public Error(Code code, String cause, Entity entity) {
        this.code = code;
        this.cause = cause;
        this.entity = entity;
    }

    public Code getCode() {
        return code;
    }

    public String getCause() {
        return cause;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", cause='" + cause + '\'' +
                ", entity='" + entity + '\'' +
                '}';
    }
}
