package org.osaf.cosmo.acegisecurity;

import org.springframework.security.AccessDecisionManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.InsufficientAuthenticationException;

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
