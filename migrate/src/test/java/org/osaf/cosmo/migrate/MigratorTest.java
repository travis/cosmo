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

import java.util.HashMap;
import java.util.Map;

import javax.jcr.SimpleCredentials;

import junit.framework.TestCase;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;

import org.apache.log4j.Logger;

/**
 */
public class MigratorTest extends TestCase {
    private static Logger log = Logger.getLogger(MigratorTest.class);

    /**
     */
    public void testWithRequiredArgs() throws Exception {
        // cl is not null and contains values for all options
        String[] args = {
            "-db", "sourcedb/",
            "-sourceconfig", "sourcerepo.xml",
            "-sourcedata", "sourcerepo/",
            "-targetconfig", "targetrepo.xml",
            "-targetdata", "targetrepo/",
        };
        Map params = Migrator.processCommandLine(args);
        assertNotNull(params);
        assertEquals((String) params.get("db"), "sourcedb/");
        assertEquals((String) params.get("sourceconfig"), "sourcerepo.xml");
        assertEquals((String) params.get("sourcedata"), "sourcerepo/");
        assertEquals((String) params.get("targetconfig"), "targetrepo.xml");
        assertEquals((String) params.get("targetdata"), "targetrepo/");
    }

    /**
     */
    public void testWithoutRequiredArgs() throws Exception {
        // prints usage to stdout
        String[] args = {};
        Map params = Migrator.processCommandLine(args);
        assertNull(params);
    }

    /**
     */
    public void testWithHelpArg() throws Exception {
        // prints usage to stdout
        String[] args = { "-help" };
        Map params = Migrator.processCommandLine(args);
        assertNull(params);
    }

    /**
     */
    public void testWithUnrecognizedArg() throws Exception {
        // unrecognized args are ignored - since we haven't passed
        // required args, cl will be null
        String[] args = { "deadbeef" };
        Map params = Migrator.processCommandLine(args);
        assertNull(params);
    }

    /**
     */
    public void testCreateMigrator() throws Exception {
        Map params = new HashMap();
        params.put("db", "sourcedb/");
        params.put("sourceconfig", "sourcerepo.xml");
        params.put("sourcedata", "sourcerepo/");
        params.put("targetconfig", "targetrepo.xml");
        params.put("targetdata", "targetrepo/");

        // create the migrator and test its clients
        Migrator migrator = Migrator.createMigrator(params);

        assertNotNull("source client null", migrator.getSourceClient());
        assertEquals(migrator.getSourceClient().getConfig(), "sourcerepo.xml");
        assertEquals(migrator.getSourceClient().getData(), "sourcerepo/");
        // Migrator does not explicitly set a workspace
        assertNull("source client has workspace name",
                   migrator.getSourceClient().getWorkspace());
        // Migrator uses default credentials
        SimpleCredentials creds =
            (SimpleCredentials) migrator.getSourceClient().getCredentials();
        assertEquals("source client has wrong username", creds.getUserID(),
                     Migrator.SOURCE_USERNAME);
        assertEquals("source client has wrong password",
                     new String(creds.getPassword()), Migrator.TARGET_PASSWORD);

        assertNotNull("target client null", migrator.getTargetClient());
        assertEquals(migrator.getTargetClient().getConfig(), "targetrepo.xml");
        assertEquals(migrator.getTargetClient().getData(), "targetrepo/");
        // Migrator does not explicitly set a workspace
        assertNull("target client has workspace name",
                   migrator.getTargetClient().getWorkspace());
        // Migrator uses default credentials
        creds =
            (SimpleCredentials) migrator.getTargetClient().getCredentials();
        assertEquals("target client has wrong username", creds.getUserID(),
                     Migrator.TARGET_USERNAME);
        assertEquals("target client has wrong password",
                     new String(creds.getPassword()), Migrator.TARGET_PASSWORD);
    }
}
