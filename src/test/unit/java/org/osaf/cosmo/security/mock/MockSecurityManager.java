/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.security.mock;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.PermissionDeniedException;

/**
 * A mock implementation of the {@link CosmoSecurityManager}
 * interface that provides a dummy {@link CosmoSecurityContext} for
 * unit mocks.
 */
public class MockSecurityManager implements CosmoSecurityManager {
    private static final Log log =
        LogFactory.getLog(MockSecurityManager.class);

    private static ThreadLocal contexts = new ThreadLocal();

    private TestHelper testHelper;

    /**
     */
    public MockSecurityManager() {
        testHelper = new TestHelper();
    }

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
        Principal principal = testHelper.makeDummyUserPrincipal(username,
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
    public CosmoSecurityContext setUpMockSecurityContext(Principal principal) {
        CosmoSecurityContext context = createSecurityContext(principal);
        contexts.set(context);
        if (log.isDebugEnabled()) {
            log.debug("setting up security context for " + context.getName());
        }
        return context;
    }

    /**
     */
    public void tearDownMockSecurityContext() {
        CosmoSecurityContext context = (CosmoSecurityContext) contexts.get();
        if (context == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("tearing down security context for " + context.getName());
        }
        contexts.set(null);
    }
    
    public void checkPermission(Item item, int permission) throws PermissionDeniedException {
        return; //TODO does this Mock need more?
    }

    /**
     */
    protected CosmoSecurityContext createSecurityContext(Principal principal) {
        return new MockSecurityContext(principal);
    }


}
