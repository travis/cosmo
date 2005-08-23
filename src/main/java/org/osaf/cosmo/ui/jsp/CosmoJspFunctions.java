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
package org.osaf.cosmo.ui.jsp;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides static methods that implement EL functions.
 */
public class CosmoJspFunctions {
    private static final Log log = LogFactory.getLog(CosmoJspFunctions.class);

    /**
     * Indicates whether or not the given <code>User</code> is an
     * application administrator (eg in the
     * {@link CosmoSecurityManager.ROLE_ROOT} role)
     */
    public static boolean isAdmin(User user) {
        return user.isInRole(CosmoSecurityManager.ROLE_ROOT);
    }
}
