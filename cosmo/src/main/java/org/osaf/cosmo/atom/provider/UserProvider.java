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
package org.osaf.cosmo.atom.provider;

import org.apache.abdera.model.Service;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.ServiceGenerator;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServiceLocator;

public class UserProvider extends BaseProvider {
    private static final Log log = LogFactory.getLog(UserProvider.class);

    // Provider methods

    public ResponseContext createEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext deleteEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext deleteMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext updateEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext updateMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getService(RequestContext request) {
        UserTarget target = (UserTarget) request.getTarget();
        User user = target.getUser();
        if (log.isDebugEnabled())
            log.debug("getting service for user " + user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ServiceGenerator generator = createServiceGenerator(locator);
            Service service =
                generator.generateService(target.getUser());

            return createResponseContext(service.getDocument());
        } catch (GeneratorException e) {
            String reason = "Unknown service generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }

    public ResponseContext getFeed(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext entryPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    // ExtendedProvider methods

    public ResponseContext createCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext updateCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext deleteCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    // our methods

    protected ServiceGenerator createServiceGenerator(ServiceLocator locator) {
        return getGeneratorFactory().createServiceGenerator(locator);
    }
}
