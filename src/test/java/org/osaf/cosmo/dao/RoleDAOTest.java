package org.osaf.cosmo.dao;

import java.util.List;

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.RoleDAO;
import org.osaf.cosmo.dao.UserDAO;
import org.osaf.cosmo.model.Role;
import org.osaf.cosmo.model.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * DAO Test Case for Roles.
 *
 * @author Brian Moseley
 */
public class RoleDAOTest extends BaseCoreTestCase {
    private static final Log log = LogFactory.getLog(RoleDAOTest.class);
    private static final String DAO_BEAN = "roleDAO";
    private static final String USER_DAO_BEAN = "userDAO";
    private RoleDAO dao = null;
    private UserDAO userDao = null;

    /**
     */
    public RoleDAOTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = (RoleDAO) getAppContext().getBean(DAO_BEAN);
        userDao = (UserDAO) getAppContext().getBean(USER_DAO_BEAN);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dao = null;
        userDao = null;
    }

    public void testCRUDRole() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        Role role = TestHelper.makeDummyRole();
        dao.saveRole(role);
        assertNotNull(role.getId());
        assertNotNull(role.getDateCreated());
        assertNotNull(role.getDateModified());

        // get by id
        Role role2 = dao.getRole(role.getId());
        assertTrue(role2.equals(role));
        assertEquals(role2.hashCode(), role.hashCode());

        // get by name
        Role role3 = dao.getRole(role.getName());
        assertTrue(role3.equals(role));
        assertEquals(role3.hashCode(), role.hashCode());

        // update doesn't do anything

        dao.removeRole(role);
        try {
            dao.getRole(role.getId());
            fail("role not removed");
        } catch (DataRetrievalFailureException e) {
            // expected
        }
    }

    public void testListRoles() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        List roles = dao.getRoles();
    }

    public void testAddRemoveUser() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN");
        }

        Role role = TestHelper.makeDummyRole();
        dao.saveRole(role);

        User user = TestHelper.makeDummyUser();
        userDao.saveUser(user);

        role.getUsers().add(user);
        dao.updateRole(role);

        Role role2 = dao.getRole(role.getId());
        assertTrue(role.getUsers().size() == 1);

        role.getUsers().remove(user);
        dao.updateRole(role);

        Role role3 = dao.getRole(role.getId());
        assertTrue(role.getUsers().isEmpty());

        dao.removeRole(role);
        userDao.removeUser(user);
    }
}
