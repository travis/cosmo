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
package org.osaf.cosmo.migrate.mock;

import java.sql.Connection;

import org.osaf.cosmo.migrate.Migration;

/**
 * Mock Migration implementation that allows from and to
 * version to be set programatically, and keeps track
 * of when migrate() is called.
 *
 */
public class MockMigration implements Migration {

    private String fromVersion = null;
    private String toVersion = null;
    public boolean migrateCalled = false;
    
    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public void setToVersion(String toVersion) {
        this.toVersion = toVersion;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public String getToVersion() {
       return toVersion;
    }

    public void migrate(Connection conn, String dialect) throws Exception {
        migrateCalled = true;
    }

}
