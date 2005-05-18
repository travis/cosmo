/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.security;

import org.osaf.cosmo.model.User;

import javax.security.auth.Subject;

/**
 * An interface that represents a user-specific context for Cosmo
 * security operations. It provides a facade for the Acegi Security
 * system and applies Cosmo-specific security rules.
 */
public interface CosmoSecurityContext {

    /**
     * Determines whether or not the context represents a Cosmo user
     * account or an anonymous user.
     */
    public boolean isAnonymous();

    /**
     * Returns an instance of {@link User} describing the user
     * represented by the security context, or <code>null</code> if
     * the context represents an anonymous user.
     */
    public User getUser();

    /**
     * Returns an instance of {@link javax.security.auth.Subject}
     * describing the user represented by the security context.
     */
    public Subject getSubject();

    /**
     * Determines whether or not the security context represents a
     * user in the root role.
     */
    public boolean inRootRole();
}
