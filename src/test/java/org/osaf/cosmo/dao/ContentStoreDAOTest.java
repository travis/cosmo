package org.osaf.cosmo.dao;

import org.osaf.cosmo.BaseCoreTestCase;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.dao.ShareDAO;
import org.osaf.cosmo.model.User;

/**
 * DAO Test Case for {@link ContentStoreDAOJCR}.
 *
 * @author Brian Moseley
 */
public class ContentStoreDAOTest extends BaseCoreTestCase {
    private static final String DAO_BEAN = "shareDAO";
    private ShareDAO dao = null;

    /**
     */
    public ContentStoreDAOTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = (ShareDAO) getAppContext().getBean(DAO_BEAN);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dao = null;
    }

    public void testCDHomedir() throws Exception {
        User user = TestHelper.makeDummyUser();
        dao.createHomedir(user.getUsername());
        assertTrue(dao.existsHomedir(user.getUsername()));
        dao.deleteHomedir(user.getUsername());
        assertTrue(! dao.existsHomedir(user.getUsername()));
    }
}
