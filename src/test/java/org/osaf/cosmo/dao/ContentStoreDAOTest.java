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
