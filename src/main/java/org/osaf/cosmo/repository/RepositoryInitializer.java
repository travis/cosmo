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
package org.osaf.cosmo.repository;

import javax.jcr.Repository;

/**
 * A helper class that initializes the Cosmo repository, registering
 * namespaces and node types and loading seed data.
 */
public interface RepositoryInitializer {

    /**
     * Registers Cosmo-specific namespaces and node types and loads
     * seed data. The repository is only initialized if no root user
     * is found in the repository. The method returns
     * <code>true</code> if an initialization actually occurred.
     */
    public boolean initialize()
        throws RepositoryInitializationException;
}
