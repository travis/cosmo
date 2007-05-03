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

import org.osaf.cosmo.model.HomeCollectionItem;

/**
 * Constructs an Atom introspection document describing the
 * collections owned by a user.
 *
 * @see Service
 * @see CollectionItem
 * @see User
 */
public interface ServiceGenerator {

    public static final String WORKSPACE_HOME = "home";

    /**
     * Generates a Service with a single workspace containing a
     * collection for each of the collections in a home collection
     * (not including any subcollections).
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    public Service generateService(HomeCollectionItem home)
        throws GeneratorException;
}
