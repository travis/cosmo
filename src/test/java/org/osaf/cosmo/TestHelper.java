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
package org.osaf.cosmo;

import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;

/**
 */
public class TestHelper {
    static int rseq = 0;
    static int useq = 0;

    private TestHelper() {
    }

    /**
     */
    public static Role makeDummyRole() {
        String serial = new Integer(++rseq).toString();

        Role role = new Role();
        role.setName("dummy" + serial);

        return role;
    }

    /**
     */
    public static User makeDummyUser() {
        String serial = new Integer(++useq).toString();

        User user = new User();
        user.setUsername("dummy" + serial);
        user.setEmail(user.getUsername() + "@osafoundation.org");
        user.setPassword(user.getUsername());

        return user;
    }
}
