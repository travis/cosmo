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

import java.util.List;

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

        // by id
        User user2 = mgr.getUser(user.getId().toString());
        assertTrue(user.equals(user2));

        // by username
        User user3 = mgr.getUserByUsername(user.getUsername());
        assertTrue(user.equals(user2));

        user3.setPassword("new password");
        User user4 = mgr.updateUser(user3);
        assertTrue(user4.getPassword().equals(user3.getPassword()));
        assertTrue(! user4.getPassword().equals(user.getPassword()));

        mgr.removeUser(user.getId().toString());
        try {
            mgr.getUser(user.getId().toString());
            fail("user not removed");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    public void testListUsers() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        List users = mgr.getUsers();
    }

    public void setProvisioningManager(ProvisioningManager provisioningMgr) {
        mgr = provisioningMgr;
    }
}
