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

import net.sf.acegisecurity.UserDetails;

/**
 * An interface that extends Acegi Security's {@link UserDetails}
 * interface to provide additional Cosmo-specific information for use
 * by security components.
 *
 * For now this interface simply allows access to the Cosmo
 * {@link User}. It is anticipated that additional details will be
 * added in the future.
 */
public interface CosmoUserDetails extends UserDetails {

    /**
     * Returns the underlying Cosmo {@link User}.
     *
     * @returns the user
     */
    public User getUser();
}
