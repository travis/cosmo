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

import org.osaf.cosmo.model.User;

import java.util.List;

/**
 * DAO interface for Users.
 *
 * @author Brian Moseley
 */
public interface UserDAO extends DAO {

    /**
     */
    public List getUsers();

    /**
     */
    public User getUser(Long id);

    /**
     */
    public User getUserByUsername(String username);

    /**
     */
    public User getUserByEmail(String email);

    /**
     */
    public void saveUser(User user);

    /**
     */
    public void updateUser(User user);

    /**
     */
    public void removeUser(Long id);

    /**
     */
    public void removeUser(User user);
}
