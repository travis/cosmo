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

import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.protocol.EntityTag;
import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.servlet.DefaultRequestHandler;
import org.apache.abdera.protocol.server.servlet.RequestHandler;

import org.osaf.cosmo.atom.provider.BaseItemTarget;
import org.osaf.cosmo.http.IfMatch;
import org.osaf.cosmo.http.IfNoneMatch;

public class StandardRequestHandler extends DefaultRequestHandler {

    protected boolean preconditions(Provider provider, 
                                    RequestContext request, 
                                    HttpServletResponse response)
        throws IOException {
        if (! super.preconditions(provider, request, response))
            return false;

        BaseItemTarget target = (BaseItemTarget) request.getTarget();

        if (! ifMatch(request.getIfMatch(), target, response))
            return false;

        if (! ifNoneMatch(request.getIfNoneMatch(), target, response))
            return false;

        return true;
    }

    private boolean ifMatch(String header,
                            BaseItemTarget target,
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
                                BaseItemTarget target,
                                HttpServletResponse response)
        throws IOException {
        try {
            if (IfNoneMatch.allowMethod(header, target.getEntityTag()))
                return true;
        } catch (ParseException e) {
            response.sendError(400, e.getMessage());
            return false;
        }
        response.sendError(412, "If-None-Match disallows conditional request");
        if (target.getEntityTag() != null)
            response.addHeader("ETag", target.getEntityTag().toString());
        return false;
    }
}
