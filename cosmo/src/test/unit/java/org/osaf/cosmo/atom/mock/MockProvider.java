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
package org.osaf.cosmo.atom.mock;

import java.util.HashSet;
import java.util.Set;

import org.apache.abdera.protocol.server.provider.EmptyResponseContext;
import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.provider.CollectionTarget;

public class MockProvider implements Provider {
    private static final Log log = LogFactory.getLog(MockProvider.class);

    private Set collections = new HashSet();
    private boolean failureMode;

    public ResponseContext createEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext deleteEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        return null;
    }
  
    public ResponseContext updateEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext updateMedia(RequestContext request) {
        return null;
    }
  
    public ResponseContext getService(RequestContext request) {
        return null;
    }

    /**
     * Returns a response representing a collection feed.
     *
     * Returns 200 if the requested collection is known. The response
     * has no content.
     *
     * Returns 404 if the requested collection is known or if the
     * request URI does not identify a collection.
     */
    public ResponseContext getFeed(RequestContext request) {
        if (failureMode)
            throw new RuntimeException("failure mode engaged");

        if (! (request.getTarget() instanceof CollectionTarget))
            return new EmptyResponseContext(404);
        CollectionTarget target = (CollectionTarget) request.getTarget();

        if (! collections.contains(target.getUid()))
            return new EmptyResponseContext(404);

        return new EmptyResponseContext(200);
    }
  
    public ResponseContext getEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext getMedia(RequestContext request) {
        return null;
    }
  
    public ResponseContext getCategories(RequestContext request) {
        return null;
    }
  
    public ResponseContext entryPost(RequestContext request) {
        return null;
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        return null;
    }

    public void addCollection(String uid) {
        collections.add(uid);
    }

    public Set getCollections() {
        return collections;
    }

    public boolean isFailureMode() {
        return failureMode;
    }

    public void setFailureMode(boolean mode) {
        failureMode = mode;
    }
}
