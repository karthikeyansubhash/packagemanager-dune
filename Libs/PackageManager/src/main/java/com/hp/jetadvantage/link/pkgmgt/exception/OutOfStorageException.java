package com.hp.jetadvantage.link.pkgmgt.exception;

import java.io.IOException;

public class OutOfStorageException extends RuntimeException {
    /**
     * Constructs a new {@code OutOfStorageException} with its stack trace
     * filled in.
     */
    public OutOfStorageException() {
    }

    /**
     * Constructs a new {@code OutOfStorageException} with its stack trace
     * filled in.
     */
    public OutOfStorageException(Throwable e) {
        super(e);
    }

    /**
     * Constructs a new {@code OutOfStorageException} with its stack trace and
     * detail message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public OutOfStorageException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs an <code>OutOfStorageException</code> with the specified
     * detail message and nested exception.
     *
     * @param detailMessage the detail message
     * @param exception the nested exception
     */
    public OutOfStorageException(String detailMessage, Throwable exception) {
        super(detailMessage, exception);
    }

    public static void accept(IOException e) throws IOException {
        if (e.getMessage().toLowerCase().contains("no space left on device")) {
            throw new OutOfStorageException(e);
        }
    }
}
