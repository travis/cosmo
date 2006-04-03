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
package org.osaf.cosmo.migrate;

import javax.jcr.Session;

/**
 * A base class for a tool that performs a migration for a specific
 * version of the application.
 *
 * As JCR 1.0 does not provide a standard schema management API,
 * migrations can not alter a 1.0-compliant repository's schema
 * in-place.
 *
 * A copy-based migration operates by copying data from a source
 * repository to a target repository, converting the data into the
 * format required by the target repository.
 *
 * An "up" migration process converts the data from the previous
 * version of the application schema to the migration's version.
 *
 * A "down" migration process converts the data from the migration's
 * version of the application schema to the previous version.
 */
public abstract class CopyBasedMigration {

    /**
     * Allows the migration to initialize resources before the
     * conversion process starts.
     */
    public abstract void init()
        throws MigrationException;
    

    /**
     * Copies data from the source session to the target session,
     * transforming as necessary to migrate from the previous
     * version's schema to this version's.
     */
    public abstract void up(Session previous,
                            Session current)
        throws MigrationException;

    /**
     * Copies data from the source session to the target session,
     * transforming as necessary to migrate from this version's schema
     * to the previous version's.
     */
    public abstract void down(Session current,
                              Session previous)
        throws MigrationException;

    /**
     * Allows the migration to release resources once the conversion
     * process has finished. Will be called even if a
     * <code>MigrationException</code> is thrown during the
     * conversion.
     */
    public abstract void release()
        throws MigrationException;

    /**
     * Returns the Cosmo version number of this migration.
     */
    public abstract String getVersion();
}
