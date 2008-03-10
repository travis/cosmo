package org.osaf.cosmo.acegisecurity;

import org.acegisecurity.AccessDecisionManager;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.InsufficientAuthenticationException;

public class DummyAccessDecisionManager implements AccessDecisionManager {

    public void decide(Authentication arg0, Object arg1,
            ConfigAttributeDefinition arg2) throws AccessDeniedException,
            InsufficientAuthenticationException {
        // do nothing
    }

    /**
     * Always returns true, as this manager does not support any
     * config attributes.
     */
    public boolean supports(ConfigAttribute attribute) { return true; }

    /**
     * Always return true;
     */
    public boolean supports(Class clazz) {
        return true;
    }

}
