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
 * @see CollectionItem
 * @see User
 */
public interface ServiceGenerator {

    public static final String WORKSPACE_HOME = "home";
    public static final String WORKSPACE_LOCAL = "local";

    /**
     * <p>
     * Generates a service containing the following workspaces
     * describing the collections accessible for a user:
     * </p>
     * <dl>
     * <dt>{@link #WORKSPACE_HOME}</dt>
     * <dd>Contains an Atom collection for each of the collections in
     * the user's home collection (not including any
     * sub-collections).</dd>
     * <dt>{@link @WORKSPACE_LOCAL}</dt>
     * <dd>Contains an Atom collection for each of the local
     * collections (those on the same server) to which the user is
     * subscribed.
     * </dl>
     *
     * @param the user
     * @throws GeneratorException
     */
    public Service generateService(User user)
        throws GeneratorException;
}
