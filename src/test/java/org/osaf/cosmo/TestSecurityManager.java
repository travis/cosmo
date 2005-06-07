/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo;


import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A test implementation of the {@link CosmoSecurityManager}
 * interface that provides a dummy {@link CosmoSecurityContext} for
 * unit tests.
 */
public class TestSecurityManager implements CosmoSecurityManager {
    private static final Log log =
        LogFactory.getLog(TestSecurityManager.class);

    private static ThreadLocal contexts = new ThreadLocal();

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a
     * Cosmo user previously authenticated by the Cosmo security
     * system.
     */
    public CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            throw new CosmoSecurityException("security context not set up");
        }
        if (log.isDebugEnabled()) {
            log.debug("getting security context for " + context.getName());
        }
        return context;
    }

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a
     * previously authenticated Cosmo user previously authenticated by
     * JAAS.
     */
    public CosmoSecurityContext getSecurityContext(Subject subject)
        throws CosmoSecurityException {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            throw new CosmoSecurityException("security context not set up");
        }
        if (log.isDebugEnabled()) {
            log.debug("getting security context by subject for " +
                      context.getName());
        }
        if (subject.equals(context.getSubject())) {
            return context;
        }
        throw new CosmoSecurityException("subject does not match security " +
                                         "context");
    }

    /**
     * Authenticate the given Cosmo credentials and register a
     * <code>CosmoSecurityContext</code> for them. This method is used
     * when Cosmo components need to programatically log in a user
     * rather than relying on a security context already being in
     * place.
     */
    public CosmoSecurityContext initiateSecurityContext(String username,
                                                        String password)
        throws CosmoSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("initiating security context for " + username);
        }
        Principal principal = TestHelper.makeDummyUserPrincipal(username,
                                                                password);
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        return context;
    }

    /**
     * Overwrite the existing <code>CosmoSecurityContext</code>. This 
     * method is used when Cosmo components need to replace the
     * existing security context with a different one (useful when
     * executing multiple operations which require different security
     * contexts).
     */
    public void refreshSecurityContext(CosmoSecurityContext securityContext) {
        contexts.set(securityContext);
        if (log.isDebugEnabled()) {
            log.debug("refreshing security context for " +
                      securityContext.getName());
        }
    }

    /* ----- our methods ----- */

    /**
     */
    public CosmoSecurityContext setUpTestSecurityContext(Principal principal) {
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        if (log.isDebugEnabled()) {
            log.debug("setting up security context for " + context.getName());
        }
        return context;
    }

    /**
     */
    public void tearDownTestSecurityContext() {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("tearing down security context for " + context.getName());
        }
        contexts.set(null);
    }

    /**
     */
    protected CosmoSecurityContext createSecurityContext(Principal principal) {
        return new TestSecurityContext(principal);
    }

    /**
     */
    protected CosmoSecurityContext
        createSecurityContext(Principal principal, Subject subject) {
        return new TestSecurityContext(principal, subject);
    }
}
