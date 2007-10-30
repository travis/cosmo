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
 * Interface that describes a Cosmo database migration.
 *
 */
public interface Migration {
    
    /**
     * Return the schema version that the migration requires.
     * @return schema version that migration starts from.
     */
    public String getFromVersion();
    
    /**
     * Return the schema version that the migration results in.
     * @return schema version that migration results in.
     */
    public String getToVersion();
    
    
    /**
     * @return set of supported db dialects
     */
    public Set<String> getSupportedDialects();
    
    
    /**
     * Perform migration.
     * @param conn connection to database
     * @param dialect dialect to use
     * @throws Exception
     */
    public void migrate(Connection conn, String dialect) throws Exception;
}
