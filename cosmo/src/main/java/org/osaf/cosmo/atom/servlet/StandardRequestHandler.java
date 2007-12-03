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
import java.util.Date;

import org.apache.abdera.protocol.ItemManager;
import org.apache.abdera.protocol.server.HttpResponse;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.EmptyResponseContext;
import org.apache.abdera.protocol.server.impl.DefaultRequestHandler;
import org.apache.abdera.util.EntityTag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.provider.AuditableTarget;
import org.osaf.cosmo.atom.provider.ExtendedProvider;

/**
 * Extends {@link DefaultRequestHandler} to provide Cosmo-specific
 * behaviors.
 */
public class StandardRequestHandler extends DefaultRequestHandler
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardRequestHandler.class);

    /**
     * Extends the superclass method to implement conditional request
     * methods by honoring conditional method headers for
     * <code>AuditableTarget</code>s.
     */
    protected boolean preconditions(Provider provider, 
                                    RequestContext request, 
                                    HttpResponse response)
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

    protected void noprovider(RequestContext request,
                              HttpResponse response)
        throws IOException {
        notfound(request, response);
    }

    private boolean ifMatch(EntityTag[] etags,
                            AuditableTarget target,
                            RequestContext request,
                            HttpResponse response)
        throws IOException {
        if (etags.length == 0)
            return true;

        if (EntityTag.matchesAny(target.getEntityTag(), etags))
            return true;

        response.sendError(412, "If-Match disallows conditional request");
        if (target.getEntityTag() != null)
            response.setHeader("ETag", target.getEntityTag().toString());

        return false;
    }

    private boolean ifNoneMatch(EntityTag[] etags,
                                AuditableTarget target,
                                RequestContext request,
                                HttpResponse response)
        throws IOException {
        if (etags.length == 0)
            return true;

        if (! EntityTag.matchesAny(target.getEntityTag(), etags))
            return true;

        if (deservesNotModified(request))
            response.sendError(304, "Not Modified");
        else
            response.sendError(412, "If-None-Match disallows conditional request");

        if (target.getEntityTag() != null)
            response.setHeader("ETag", target.getEntityTag().toString());

        return false;
    }

    private boolean ifModifiedSince(Date date,
                                    AuditableTarget target,
                                    RequestContext request,
                                    HttpResponse response)
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
                                      HttpResponse response)
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
