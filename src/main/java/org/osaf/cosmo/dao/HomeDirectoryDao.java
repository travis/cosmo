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
package org.osaf.cosmo.dao;

import org.osaf.cosmo.model.Resource;

/**
 * Interface for DAOs that manage access to user home directories.
 */
public interface HomeDirectoryDao extends Dao {

    /**
     * Returns the resource at the specified path within the
     * repository.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public Resource getResource(String path);

    /**
     * Removes the resource at the specified path within the
     * repository.
     */
    public void removeResource(String path);
}
