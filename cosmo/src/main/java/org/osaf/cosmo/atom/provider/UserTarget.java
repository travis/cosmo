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
import org.osaf.cosmo.model.User;

/**
 * <p>
 * A target that identifies a particular user.
 * </p>
 */
public class UserTarget extends SimpleTarget {

    private User user;

    /**
     * Constructs a <code>UserTarget</code> of type
     * {@link TargetType.TYPE_SERVICE}.
     */
    public UserTarget(RequestContext request,
                      User user) {
        this(TargetType.TYPE_SERVICE, request, user);
    }

    public UserTarget(TargetType type,
                      RequestContext request,
                      User user) {
        super(type, request);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
