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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.BasicParser;

import org.apache.log4j.Logger;

/**
 * The entry point of the repository migratoin tool.
 */
public class Migrator {
    private static Logger log = Logger.getLogger(Migrator.class);

    /** */
    public static final int RC_OK = 0;
    /** */
    public static final int RC_ERR_USAGE = 1;
    /** */
    public static final int RC_ERR_CLI = 2;
    /** */
    public static final int RC_ERR_START_SOURCE = 3;
    /** */
    public static final int RC_ERR_START_TARGET = 4;
    /** */
    public static final int RC_ERR_MIGRATION_INIT = 5;
    /** */
    public static final int RC_ERR_MIGRATION_UP = 6;
    /** */
    public static final int RC_ERR_MIGRATION_DOWN = 7;
    /** */
    public static final int RC_ERR_STOP_SOURCE = 8;
    /** */
    public static final int RC_ERR_STOP_TARGET = 9;

    /** */
    public static final String SOURCE_USERNAME = "cosmo_repository";
    /** */
    public static final String SOURCE_PASSWORD = "";
    /** */
    public static final String TARGET_USERNAME = SOURCE_USERNAME;
    /** */
    public static final String TARGET_PASSWORD = SOURCE_PASSWORD;

    private MigratorClient sourceClient;
    private MigratorClient targetClient;

    /**
     */
    public Migrator() {
    }

    /**
     */
    public MigratorClient getSourceClient() {
        return sourceClient;
    }

    /**
     */
    public void setSourceClient(MigratorClient client) {
        sourceClient = client;
    }

    /**
     */
    public MigratorClient getTargetClient() {
        return targetClient;
    }

    /**
     */
    public void setTargetClient(MigratorClient client) {
        targetClient = client;
    }

    // package protected methods - can be tested independently

    /**
     * Processes the command line arguments. Returns an instance of
     * <code>CommandLine</code> when all required args are
     * provided. Returns <code>null</code> if a required arg is not
     * provided or if the <code>-help</code> arg is provided. Ignores
     * unrecognized args.
     *
     * Required args:
     *
     * <ul>
     * <li> <code>-sourceconfig</code>: location of source repository
     * config file </li>
     * <li> <code>-sourcedata</code>: location of source repository
     * data directory </li>
     * <li> <code>-targetconfig</code>: location of target repository
     * config file </li>
     * <li> <code>-targetdata</code>: location of target repository
     * data directory </li>
     * </ul>
     */
    static CommandLine processCommandLine(String[] args)
        throws ParseException {
        Option help = new Option("help", "print this message");
        Option sourceConfig = OptionBuilder.withArgName("file").hasArg().
            withDescription("location of source repository config file").
            create("sourceconfig");
        Option sourceData = OptionBuilder.withArgName("dir").hasArg().
            withDescription("location of source repository data directory").
            create("sourcedata");
        Option targetConfig = OptionBuilder.withArgName("file").hasArg().
            withDescription("location of target repository config file").
            create("targetconfig");
        Option targetData = OptionBuilder.withArgName("dir").hasArg().
            withDescription("location of target repository data directory").
            create("targetdata");

        Options options = new Options();
        options.addOption(help);
        options.addOption(sourceConfig);
        options.addOption(sourceData);
        options.addOption(targetConfig);
        options.addOption(targetData);

        CommandLineParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);

        if (cl.hasOption("help") ||
            cl.getOptionValue("sourceconfig") == null ||
            cl.getOptionValue("sourcedata") == null ||
            cl.getOptionValue("targetconfig") == null ||
            cl.getOptionValue("targetdata") == null) {
            new HelpFormatter().printHelp("migrator", options, true);
            return null;
        }

        return cl;
    }

    /**
     * Configures source and target clients based on the provided
     * command line.
     *
     * Currently only refers to the command line for config file and
     * data directory locations. Does not set workspace names (thereby
     * using the repositories' default workspaces)  and uses
     * {@link SimpleCredentials} with constant usernames and
     * passwords.
     *
     * @see #SOURCE_USERNAME
     * @see #SOURCE_PASSWORD
     * @see #TARGET_USERNAME
     * @see #TARGET_PASSWORD
     */
    static Migrator createMigrator(CommandLine cl) {
        MigratorClient sourceClient = new MigratorClient();
        sourceClient.setConfig(cl.getOptionValue("sourceconfig"));
        sourceClient.setData(cl.getOptionValue("sourcedata"));
        sourceClient.setCredentials(SOURCE_USERNAME, SOURCE_PASSWORD);

        MigratorClient targetClient = new MigratorClient();
        targetClient.setConfig(cl.getOptionValue("targetconfig"));
        targetClient.setData(cl.getOptionValue("targetdata"));
        targetClient.setCredentials(TARGET_USERNAME, TARGET_PASSWORD);

        Migrator migrator = new Migrator();
        migrator.setSourceClient(sourceClient);
        migrator.setTargetClient(targetClient);

        return migrator;
    }

    int doMigration() {
        int rc = RC_OK;

        try {
            sourceClient.start();
        } catch (MigrationException e) {
            System.err.println("Could not start source repository: ");
            e.printStackTrace();
            rc = RC_ERR_START_SOURCE;
        }

        if (rc == RC_OK) {
            try {
                targetClient.start();
            } catch (MigrationException e) {
                System.err.println("Could not start target repository: ");
                e.printStackTrace();
                rc = RC_ERR_START_TARGET;
            }
        }

        if (rc == RC_OK) {
            Migration03 migration = new Migration03();

            try {
                migration.init();
            } catch (MigrationException e) {
                System.err.println("Could not initialize migration:");
                e.printStackTrace();
                rc = RC_ERR_MIGRATION_INIT;
            }

            // down migration not supported by Migration03

            if (rc == RC_OK) {
                try {
                    migration.up(sourceClient.getSession(),
                                 targetClient.getSession());
                } catch (MigrationException e) {
                    System.err.println("Could not perform up migration:");
                    e.printStackTrace();
                    rc = RC_ERR_MIGRATION_UP;
                }
            }
        }

        if (targetClient.isStarted()) {
            try {
                targetClient.stop();
            } catch (MigrationException e) {
                System.err.println("Could not stop target repository:");
                e.printStackTrace();
            }
        }

        if (sourceClient.isStarted()) {
            try {
                sourceClient.stop();
            } catch (MigrationException e) {
                System.err.println("Could not stop source repository:");
                e.printStackTrace();
            }
        }

        return rc;
    }

    /**
     * Processes command line args, access the source and target
     * repositories, finds all migrations and executes them.
     *
     * Exists with the following codes:
     *
     * <ul>
     * <li> {@link #RC_OK} when all migrations complete
     * successfully. </li>
     * <li> {@link #RC_ERR_CLI} if the command line cannot be
     * parsed. </li>
     * </ul>
     */
    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            cl = processCommandLine(args);
        } catch (ParseException e) {
            System.err.println("Failure parsing command line: ");
            e.printStackTrace();
            System.exit(RC_ERR_CLI);
        }
        if (cl == null) {
            System.exit(RC_OK);
        }

        Migrator migrator = createMigrator(cl);
        int rc = migrator.doMigration();

        System.exit(rc);
    }
}
