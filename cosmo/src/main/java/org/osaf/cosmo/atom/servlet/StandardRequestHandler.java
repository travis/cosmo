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

import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.protocol.server.provider.TargetType;
import org.apache.abdera.protocol.server.servlet.DefaultRequestHandler;
import org.apache.abdera.protocol.server.servlet.RequestHandler;
import org.apache.abdera.util.EntityTag;

import org.apache.commons.lang.StringUtils;
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
     * <p>
     * Extends the superclass method to implement the following APP
     * extensions:
     * </p>
     * <ul>
     * <li> When <code>POST</code>ing to a collection, if the content
     * type of the request is
     * <code>application/x-www-form-urlencoded</code>, the request is
     * interpreted as a collection details update.
     * </ul>
     */
    protected ResponseContext process(Provider provider,
                                      RequestContext request) {
        String method = request.getMethod();
        TargetType type = request.getTarget().getType();

        if (method.equals("PUT")) {
            if (type == TargetType.TYPE_COLLECTION)
                return ((ExtendedProvider) provider).updateCollection(request);
        }

        return super.process(provider, request);
    }

    /**
     * Extends the superclass method to implement conditional request
     * methods by honoring the <code>If-Match</code> request header
     * and friends.
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
