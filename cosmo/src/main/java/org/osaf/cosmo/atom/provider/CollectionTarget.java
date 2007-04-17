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

import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.TargetType;

public class CollectionTarget extends ItemTarget {

    private String projection;
    private String format;

    public CollectionTarget(RequestContext request,
                            String uid) {
        this(request, uid, null, null);
    }

    public CollectionTarget(RequestContext request,
                            String uid,
                            String projection,
                            String format) {
        super(TargetType.TYPE_COLLECTION, request, uid);
        this.projection = projection;
        this.format = format;
    }

    public String getProjection() {
        return projection;
    }

    public String getFormat() {
        return format;
    }
}
