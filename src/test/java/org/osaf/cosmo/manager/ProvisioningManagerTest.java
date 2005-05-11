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
    private static final String MANAGER_BEAN = "provisioningManager";
    private ProvisioningManager mgr = null;

    /**
     */
    public ProvisioningManagerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        mgr = (ProvisioningManager) getAppContext().getBean(MANAGER_BEAN);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        mgr = null;
    }

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
}
