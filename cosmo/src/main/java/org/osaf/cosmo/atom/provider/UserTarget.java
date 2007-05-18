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

import org.apache.abdera.protocol.EntityTag;
import org.apache.abdera.protocol.server.provider.AbstractTarget;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.TargetType;

import org.osaf.cosmo.model.User;

public class UserTarget extends AbstractTarget {

    private User user;

    public UserTarget(RequestContext request,
                      User user) {
        super(TargetType.TYPE_SERVICE, request);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
