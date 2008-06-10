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

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserCollectionAdapter extends BaseCollectionAdapter {
    private static final Log log = LogFactory.getLog(UserCollectionAdapter.class);

    // Provider methods

    public ResponseContext postEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext deleteEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext putEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getFeed(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    // ExtendedProvider methods

    public ResponseContext putCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext postCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext deleteCollection(RequestContext request) {
        throw new UnsupportedOperationException();
    }
}
