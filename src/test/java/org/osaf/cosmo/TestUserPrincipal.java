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

import java.security.Principal;

import org.osaf.cosmo.model.User;

/**
 */
public class TestUserPrincipal implements Principal {
    private User user;

    public TestUserPrincipal(User user) {
        this.user = user;
    }

    public boolean equals(Object another) {
        if (!(another instanceof TestUserPrincipal)) {
            return false;
        }
        return user.equals(((TestUserPrincipal)another).getUser());
    }

    public String toString() {
        return user.toString();
    }

    public int hashCode() {
        return user.hashCode();
    }

    public String getName() {
        return user.getUsername();
    }

    public User getUser() {
        return user;
    }
}
