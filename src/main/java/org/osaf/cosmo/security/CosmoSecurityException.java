package org.osaf.cosmo.security;

/**
 * An instance of {@link java.lang.RuntimeException} that signifies an
 * exception within the Cosmo security system.
 */
public class CosmoSecurityException extends RuntimeException {

    /**
     */
    public CosmoSecurityException(String message) {
        super(message);
    }

    /**
     */
    public CosmoSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}

