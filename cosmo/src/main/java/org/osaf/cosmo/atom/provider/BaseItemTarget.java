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
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.SimpleTarget;


public abstract class BaseItemTarget extends SimpleTarget
    implements AuditableTarget {

    private String projection;
    private String format;

    public BaseItemTarget(TargetType type,
                          RequestContext request) {
        this(type, request, null, null);
    }

    public BaseItemTarget(TargetType type,
                          RequestContext request,
                          String projection,
                          String format) {
        super(type, request);
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
