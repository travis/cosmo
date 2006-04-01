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
            "-sourceconfig", "sourcerepo.xml",
            "-sourcedata", "sourcerepo/",
            "-targetconfig", "targetrepo.xml",
            "-targetdata", "targetrepo/",
        };
        CommandLine cl = Migrator.processCommandLine(args);
        assertNotNull(cl);
        assertEquals(cl.getOptionValue("sourceconfig"), "sourcerepo.xml");
        assertEquals(cl.getOptionValue("sourcedata"), "sourcerepo/");
        assertEquals(cl.getOptionValue("targetconfig"), "targetrepo.xml");
        assertEquals(cl.getOptionValue("targetdata"), "targetrepo/");
    }

    /**
     */
    public void testWithoutRequiredArgs() throws Exception {
        // prints usage to stdout
        String[] args = {};
        CommandLine cl = Migrator.processCommandLine(args);
        assertNull(cl);
    }

    /**
     */
    public void testWithHelpArg() throws Exception {
        // prints usage to stdout
        String[] args = { "-help" };
        CommandLine cl = Migrator.processCommandLine(args);
        assertNull(cl);
    }

    /**
     */
    public void testWithUnrecognizedArg() throws Exception {
        // unrecognized args are ignored - since we haven't passed
        // required args, cl will be null
        String[] args = { "deadbeef" };
        CommandLine cl = Migrator.processCommandLine(args);
        assertNull(cl);
    }

    /**
     */
    public void testCreateMigrator() throws Exception {
        // set up a CommandLine
        Option sourceConfig = OptionBuilder.withArgName("file").hasArg().
            create("sourceconfig");
        Option sourceData = OptionBuilder.withArgName("dir").hasArg().
            create("sourcedata");
        Option targetConfig = OptionBuilder.withArgName("file").hasArg().
            create("targetconfig");
        Option targetData = OptionBuilder.withArgName("dir").hasArg().
            create("targetdata");

        Options options = new Options();
        options.addOption(sourceConfig);
        options.addOption(sourceData);
        options.addOption(targetConfig);
        options.addOption(targetData);

        String[] args = {
            "-sourceconfig", "sourcerepo.xml",
            "-sourcedata", "sourcerepo/",
            "-targetconfig", "targetrepo.xml",
            "-targetdata", "targetrepo/",
        };
        CommandLineParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);

        // create the migrator and test its clients
        Migrator migrator = Migrator.createMigrator(cl);

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
