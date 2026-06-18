package com.hp.jetadvantage.link.pkgmgt.exception;

import com.hp.jetadvantage.link.pkgmgt.model.Error;

public class PacManException extends RuntimeException {
    private final Error error;

    public PacManException(Error.Code code, String causeStr, Error.Entity entity) {
        this(code, causeStr, entity, null);
    }

    public PacManException(Error.Code code, String causeStr, Error.Entity entity, Throwable throwable) {
        super(causeStr, throwable);
        this.error = new Error(code, causeStr, entity);
    }

    public Error getError() {
        return error;
    }
}
