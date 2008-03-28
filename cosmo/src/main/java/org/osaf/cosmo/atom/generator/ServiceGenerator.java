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
package org.osaf.cosmo.atom.generator;

import org.apache.abdera.model.Service;

import org.osaf.cosmo.model.User;

/**
 * Constructs an Atom introspection document describing the
 * collections owned by and subscribed to by a user.
 *
 * @see Service
 * @see MockCollectionItem
 * @see User
 */
public interface ServiceGenerator {

    public static final String WORKSPACE_HOME = "home";
    public static final String WORKSPACE_ACCOUNT = "account";
    public static final String COLLECTION_SUBSCRIPTIONS = "subscriptions";
    public static final String COLLECTION_PREFERENCES = "preferences";

    /**
     * <p>
     * Generates a service containing the following workspaces
     * describing the collections accessible for a user:
     * </p>
     * <h2><code>home</code> Workspace</h2>
     * <p>
     * This workspace contains an Atom collection for each of the
     * collections published by the user into his home collection (not
     * including any sub-collections).
     * </p>
     * <h2><code>meta</code> Workspace</h2>
     * <p>
     * This workspace contains Atom collections providing access to
     * various "meta" information about the user, including his
     * subscriptions and preferences. These collections include:
     * </p>
     * <dl>
     * <dt><code>subscriptions</code></dt>
     * <dd>Lists the user's collection subscriptions.</dd>
     * <dt><code>preferences</code></dt>
     * <dd>Lists the user's preferences.</dd>
     * </dl>
     *
     * @param the user
     * @throws GeneratorException
     */
    public Service generateService(User user)
        throws GeneratorException;
}
