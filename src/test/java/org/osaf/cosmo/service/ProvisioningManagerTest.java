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

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.manager.ProvisioningManager;
import org.osaf.cosmo.model.User;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Test Case for ProvisioningManager.
 *
 * @author Brian Moseley
 */
public class ProvisioningManagerTest extends BaseCoreTestCase {
    private static final Log log =
        LogFactory.getLog(ProvisioningManagerTest.class);

    private ProvisioningManager mgr;

    public void testCRUDUser() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        User user = TestHelper.makeDummyUser();
        mgr.saveUser(user);

        User user2 = mgr.getUser(user.getUsername());
        assertTrue(user2.equals(user));

        user2.setPassword("new password");
        User user3 = mgr.updateUser(user2);
        assertTrue(user3.getPassword().equals(user2.getPassword()));
        assertTrue(! user3.getPassword().equals(user.getPassword()));

        mgr.removeUser(user.getUsername());
        try {
            mgr.getUser(user.getUsername());
            fail("user not removed");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    public void testListUsers() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        Set users = mgr.getUsers();
    }

    public void setProvisioningManager(ProvisioningManager provisioningMgr) {
        mgr = provisioningMgr;
    }
}
