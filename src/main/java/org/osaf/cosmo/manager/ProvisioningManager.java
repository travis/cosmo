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
package org.osaf.cosmo.manager;

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;

import java.util.List;

/**
 * Manager interface for the provisioning subsystem.
 *
 * @author Brian Moseley
 */
public interface ProvisioningManager extends Manager {

    /**
     */
    public List getRoles();

    /**
     */
    public Role getRole(String id);

    /**
     */
    public Role getRoleByName(String role);

    /**
     */
    public List getUsers();

    /**
     */
    public User getUser(String id);

    /**
     */
    public User getUserByUsername(String username);

    /**
     */
    public User saveUser(User user);

    /**
     */
    public User updateUser(User user);

    /**
     */
    public void removeUser(String id);
}
