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
package org.osaf.cosmo.acegisecurity.providers.anonymous;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.Authentication;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousProcessingFilter;
import org.acegisecurity.ui.WebAuthenticationDetails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;

/**
 * An <code>AnonymousProcessingFilter</code>
 * that does not begin an <code>HttpSession</code>.
 * <p>
 * The usual behavior of <code>AnonymousProcessingFilter</code> is to add
 * a <code>WebAuthenticationDetails</code> to the authentication token
 * it creates which, by default, begins an <code>HttpSession</code>.
 *
 * @see AnonymousProcessingFilter
 * @see WebAuthenticationDetails
 * @see javax.servlet.http.HttpSession
 */
public class NoSessionAnonymousProcessingFilter
    extends AnonymousProcessingFilter {
    private static final Log log =
        LogFactory.getLog(NoSessionAnonymousProcessingFilter.class);

    /**
     * Overrides the superclass method to create an
     * <code>AnonymousAuthenticationToken</code> and set a
     * <code>WebAuthenticationDetails</code> on it without beginning
     * an <code>HttpSession</code>.
     *
     * @param request the servlet request
     * @return the newly created <code>Authentication</code>
     * @see AnonymousAuthenticationToken
     */
    protected Authentication createAuthentication(ServletRequest request) {
        Assert.isInstanceOf(HttpServletRequest.class, request,
            "ServletRequest must be an instance of HttpServletRequest");

        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(getKey(), getUserAttribute().getPassword(), getUserAttribute().getAuthorities());

        auth.setDetails(new WebAuthenticationDetails(
                (HttpServletRequest) request, false));

        return auth;
    }
}
