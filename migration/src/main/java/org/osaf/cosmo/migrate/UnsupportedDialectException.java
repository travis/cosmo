package org.osaf.cosmo.migrate;

public class UnsupportedDialectException extends RuntimeException {
    
    public UnsupportedDialectException(String message) {
        super(message);
    }

    public UnsupportedDialectException(String message, Throwable cause) {
        super(message, cause);
    }
}
