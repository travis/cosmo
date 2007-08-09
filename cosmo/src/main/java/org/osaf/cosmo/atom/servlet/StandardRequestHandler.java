/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.atom.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.provider.EmptyResponseContext;
import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.ProviderManager;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.protocol.server.provider.TargetType;
import org.apache.abdera.protocol.server.servlet.DefaultRequestHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.provider.AuditableTarget;
import org.osaf.cosmo.atom.provider.ExtendedProvider;
import org.osaf.cosmo.http.IfMatch;
import org.osaf.cosmo.http.IfNoneMatch;

/**
 * Extends {@link DefaultRequestHandler} to provide Cosmo-specific
 * behaviors.
 */
public class StandardRequestHandler extends DefaultRequestHandler
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardRequestHandler.class);

    /**
     * Override to not swallow RuntimeExceptions 
     */
    @Override
    public void process(ServiceContext context, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        ProviderManager manager = context.getProviderManager();
        RequestContext requestContext = getRequestContext(context, request);
        Provider provider = manager.getProvider(requestContext);

        try {
            if (preconditions(provider, requestContext, response)) {
                try {
                    output(request, response, process(provider, requestContext));
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable t) {
                    log.error("Error producing output", t);
                    try {
                        output(request, response, new EmptyResponseContext(500));
                    } catch (Exception ex) {
                        log.error("Error outputting error", ex);
                        response.sendError(500);
                    }
                }
            }
        } finally {
            manager.release(provider);
        }
    }
    
    /**
     * <p>
     * Extends the superclass method to implement the following APP
     * extensions:
     * </p>
     * <ul>
     * <li> When <code>PUT</code>ing to a collection, the request is
     * interpreted as an update of the collection itself.
     * </ul>
     */
    protected ResponseContext process(Provider provider,
                                      RequestContext request) {
        String method = request.getMethod();
        TargetType type = request.getTarget().getType();

        if (method.equals("PUT")) {
            if (type == TargetType.TYPE_COLLECTION)
                return ((ExtendedProvider) provider).updateCollection(request);
        } else if (method.equals("POST")) {
            if (type == TargetType.TYPE_COLLECTION)
                return ((ExtendedProvider) provider).createCollection(request);
        }

        return super.process(provider, request);
    }

    /**
     * Extends the superclass method to implement conditional request
     * methods by honoring conditional method headers for
     * <code>AuditableTarget</code>s.
     */
    protected boolean preconditions(Provider provider, 
                                    RequestContext request, 
                                    HttpServletResponse response)
        throws IOException {
        if (! super.preconditions(provider, request, response))
            return false;

        if (! (request.getTarget() instanceof AuditableTarget))
            return true;

        AuditableTarget target = (AuditableTarget) request.getTarget();

        if (! ifMatch(request.getIfMatch(), target, request, response))
            return false;

        if (! ifNoneMatch(request.getIfNoneMatch(), target, request, response))
            return false;

        if (! ifModifiedSince(request.getIfModifiedSince(), target, request,
                              response))
            return false;

        if (! ifUnmodifiedSince(request.getIfUnmodifiedSince(), target, request,
                                response))
            return false;

        return true;
    }

    protected String[] getAllowedMethods(TargetType type) {
        if (type != null && type == TargetType.TYPE_COLLECTION)
            return new String[] { "GET", "POST", "PUT", "HEAD", "OPTIONS" };
        return super.getAllowedMethods(type);
    }

    private boolean ifMatch(String header,
                            AuditableTarget target,
                            RequestContext request,
                            HttpServletResponse response)
        throws IOException {
        try {
            if (IfMatch.allowMethod(header, target.getEntityTag()))
                return true;
        } catch (ParseException e) {
            response.sendError(400, e.getMessage());
            return false;
        }

        response.sendError(412, "If-Match disallows conditional request");
        if (target.getEntityTag() != null)
            response.addHeader("ETag", target.getEntityTag().toString());

        return false;
    }

    private boolean ifNoneMatch(String header,
                                AuditableTarget target,
                                RequestContext request,
                                HttpServletResponse response)
        throws IOException {
        try {
            if (IfNoneMatch.allowMethod(header, target.getEntityTag()))
                return true;
        } catch (ParseException e) {
            response.sendError(400, e.getMessage());
            return false;
        }

        if (deservesNotModified(request))
            response.sendError(304, "Not Modified");
        else
            response.sendError(412, "If-None-Match disallows conditional request");

        if (target.getEntityTag() != null)
            response.addHeader("ETag", target.getEntityTag().toString());

        return false;
    }

    private boolean ifModifiedSince(Date date,
                                    AuditableTarget target,
                                    RequestContext request,
                                    HttpServletResponse response)
        throws IOException {
        if (date == null)
            return true;
        if (target.getLastModified().after(date))
            return true;
        response.sendError(304, "Not Modified");
        return false;
    }

    private boolean ifUnmodifiedSince(Date date,
                                      AuditableTarget target,
                                      RequestContext request,
                                      HttpServletResponse response)
        throws IOException {
        if (date == null)
            return true;
        if (target.getLastModified().before(date))
            return true;
        response.sendError(412, "If-Unmodified-Since disallows conditional request");
        return false;
    }

    private boolean deservesNotModified(RequestContext request) {
        return (request.getMethod().equals("GET") ||
                request.getMethod().equals("HEAD"));
    }
}
