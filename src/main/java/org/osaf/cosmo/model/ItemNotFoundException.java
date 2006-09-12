/**
 * 
 */
package org.osaf.cosmo.model;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author rletness
 *
 */
public class ItemNotFoundException extends DataRetrievalFailureException {
    /**
     */
    public ItemNotFoundException(String message) {
        super(message);
    }

    /**
     */
    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
