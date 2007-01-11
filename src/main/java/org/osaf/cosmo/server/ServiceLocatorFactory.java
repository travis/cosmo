/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.server;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.security.CosmoSecurityManager;

/**
 * This class produces instances of <code>ServiceLocator</code> that
 * can build URLs for services and collections as described in the
 * documentation for that class.
 *
 * @see ServiceLocator
 */
public class ServiceLocatorFactory {
    private static final Log log =
        LogFactory.getLog(ServiceLocatorFactory.class);

    private String davPrefix;
    private String atomPrefix;
    private String morseCodePrefix;
    private String webPrefix;
    private String webcalPrefix;
    private CosmoSecurityManager securityManager;

    /**
     * Returns a <code>ServiceLocator</code> instance that returns
     * URLs based on the application mount URL calculated from
     * information in the given request.
     */
    public ServiceLocator createServiceLocator(HttpServletRequest request) {
        Ticket ticket = securityManager.getSecurityContext().getTicket();
        return createServiceLocator(request, ticket);
    }

    /**
     * Returns a <code>ServiceLocator</code> instance that returns
     * URLs based on the application mount URL calculated from
     * information in the given request and including the given ticket.
     */
    public ServiceLocator createServiceLocator(HttpServletRequest request,
                                               Ticket ticket) {
        String appMountUrl = calculateAppMountUrl(request);

        String ticketKey = ticket != null ? ticket.getKey() : null;

        return new ServiceLocator(appMountUrl, ticketKey, this);
    }

    /** */
    public String getDavPrefix() {
        return davPrefix;
    }

    /** */
    public void setDavPrefix(String prefix) {
        davPrefix = prefix;
    }

    /** */
    public String getAtomPrefix() {
        return atomPrefix;
    }

    /** */
    public void setAtomPrefix(String prefix) {
        atomPrefix = prefix;
    }

    /** */
    public String getMorseCodePrefix() {
        return morseCodePrefix;
    }

    /** */
    public void setMorseCodePrefix(String prefix) {
        morseCodePrefix = prefix;
    }

    /** */
    public String getWebPrefix() {
        return webPrefix;
    }

    /** */
    public void setWebPrefix(String prefix) {
        webPrefix = prefix;
    }

    /** */
    public String getWebcalPrefix() {
        return webcalPrefix;
    }

    /** */
    public void setWebcalPrefix(String prefix) {
        webcalPrefix = prefix;
    }

    /** */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /** */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Initializes the factory, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (davPrefix == null)
            throw new IllegalStateException("davPrefix must not be null");
        if (atomPrefix == null)
            throw new IllegalStateException("atomPrefix must not be null");
        if (morseCodePrefix == null)
            throw new IllegalStateException("morseCodePrefix must not be null");
        if (webPrefix == null)
            throw new IllegalStateException("webPrefix must not be null");
        if (webcalPrefix == null)
            throw new IllegalStateException("webcalPrefix must not be null");
        if (securityManager == null)
            throw new IllegalStateException("securityManager must not be null");
    }

    private String calculateAppMountUrl(HttpServletRequest request) {
        StringBuffer buf = new StringBuffer();
        buf.append(request.getScheme()).
            append("://").
            append(request.getServerName());
        if ((request.isSecure() && request.getServerPort() != 443) ||
            (request.getServerPort() != 80)) {
            buf.append(":").append(request.getServerPort());
        }
        if (! request.getContextPath().equals("/")) {
            buf.append(request.getContextPath());
        }
        return buf.toString();
    }
}
