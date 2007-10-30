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

import java.sql.Connection;
import java.util.Set;

/**
 * Migration implementation that relies on SQL scripts that
 * are run as specified in @code AbstractMigration.
 * 
 * Provides an implementation of migrationDate() that
 * does nothing.  This allows migrations based on SQL
 * scripts to be configured easily in Spring.
 * 
 * @see AbstractMigration
 *
 */
public class BasicSqlScriptMigration extends AbstractMigration {

    
    Set<String> supportedDialects = null;
    
    @Override
    public Set<String> getSupportedDialects() {
        return supportedDialects;
    }

    @Override
    protected void migrateData(Connection conn, String dialect)
            throws Exception {
        // Do nothing
    }

    public void setSupportedDialects(Set<String> supportedDialects) {
        this.supportedDialects = supportedDialects;
    }

}
