/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.migrate;

import java.util.ArrayList;
import java.util.List;

import org.osaf.cosmo.migrate.mock.MockDataSource;
import org.osaf.cosmo.migrate.mock.MockMigration;
import org.osaf.cosmo.migrate.mock.MockMigrationManager;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test MigrationManager
 */
public class MigrationManagerTest extends TestCase {

    MigrationManager manager = null;
    MockMigration mig1 = null;
    MockMigration mig2 = null;
    MockMigration mig3 = null;
    
    @Override
    protected void setUp() throws Exception {
        manager = new MockMigrationManager();
        ((MockMigrationManager) manager).setCurrentVersion("1");
        manager.setDatasource(new MockDataSource());
        manager.setDialect("cosmo");
        
        mig1 = new MockMigration();
        mig2 = new MockMigration();
        mig3 = new MockMigration();
        
        List<Migration> migrations = new ArrayList<Migration>();
        migrations.add(mig1);
        migrations.add(mig2);
        migrations.add(mig3);
        
        manager.setMigrations(migrations);
    }
    
    public void testMigrationManager() throws Exception {
        
        mig1.setFromVersion("1");
        mig1.setToVersion("2");
        mig2.setFromVersion("2");
        mig2.setToVersion("3");
        mig3.setFromVersion("3");
        mig3.setToVersion("4");
        
        Assert.assertFalse(mig1.migrateCalled);
        Assert.assertFalse(mig2.migrateCalled);
        Assert.assertFalse(mig3.migrateCalled);
        
        manager.migrate();
        
        Assert.assertTrue(mig1.migrateCalled);
        Assert.assertTrue(mig2.migrateCalled);
        Assert.assertTrue(mig3.migrateCalled);
        
        mig3.setFromVersion("6");
        mig3.setToVersion("7");
        
        mig1.migrateCalled = false;
        mig2.migrateCalled = false;
        mig3.migrateCalled = false;
        
        manager.migrate();
        
        Assert.assertTrue(mig1.migrateCalled);
        Assert.assertTrue(mig2.migrateCalled);
        Assert.assertFalse(mig3.migrateCalled); 
    }
    
}
