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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    public static final int RC_ERR_USAGE = RC_OK + 1;
    /** */
    public static final int RC_ERR_CLI = RC_ERR_USAGE + 1;
    /** */
    public static final int RC_ERR_DB = RC_ERR_CLI + 1;
    /** */
    public static final int RC_ERR_SOURCE_DATA = RC_ERR_DB + 1;
    /** */
    public static final int RC_ERR_SOURCE_CONFIG = RC_ERR_SOURCE_DATA + 1;
    /** */
    public static final int RC_ERR_TARGET_DATA = RC_ERR_SOURCE_CONFIG + 1;
    /** */
    public static final int RC_ERR_TARGET_CONFIG = RC_ERR_TARGET_DATA + 1;
    /** */
    public static final int RC_ERR_START_SOURCE = RC_ERR_TARGET_CONFIG + 1;
    /** */
    public static final int RC_ERR_START_TARGET = RC_ERR_START_SOURCE + 1;
    /** */
    public static final int RC_ERR_MIGRATION_INIT = RC_ERR_START_TARGET + 1;
    /** */
    public static final int RC_ERR_MIGRATION_UP = RC_ERR_MIGRATION_INIT + 1;
    /** */
    public static final int RC_ERR_MIGRATION_DOWN = RC_ERR_MIGRATION_UP + 1;
    /** */
    public static final int RC_ERR_STOP_SOURCE = RC_ERR_MIGRATION_DOWN + 1;
    /** */
    public static final int RC_ERR_STOP_TARGET = RC_ERR_STOP_SOURCE + 1;

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
    private String db;

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

    /**
     */
    public String getDb() {
        return db;
    }

    /**
     */
    public void setDb(String db) {
        this.db = db;
    }

    // package protected methods - can be tested independently

    /**
     * Processes the command line arguments. Returns a
     * <code>Map</code> of parameters when all required args are
     * provided. Returns <code>null</code> if a required arg is not
     * provided or if the <code>-help</code> arg is provided. Ignores
     * unrecognized args.
     *
     * Required args:
     *
     * <ul>
     * <li> <code>-db</code>: location of database directory </li>
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
    static Map processCommandLine(String[] args)
        throws ParseException {
        Option help = new Option("help", "print this message");
        Option db = OptionBuilder.withArgName("dir").hasArg().
            withDescription("location of user database directory").
            create("db");
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
        options.addOption(db);
        options.addOption(sourceConfig);
        options.addOption(sourceData);
        options.addOption(targetConfig);
        options.addOption(targetData);

        CommandLineParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);

        if (cl.hasOption("help") ||
            cl.getOptionValue("db") == null ||
            cl.getOptionValue("sourceconfig") == null ||
            cl.getOptionValue("sourcedata") == null ||
            cl.getOptionValue("targetconfig") == null ||
            cl.getOptionValue("targetdata") == null) {
            new HelpFormatter().printHelp("java -jar cosmo-migrate-x.x.jar",
                                          options, true);
            return null;
        }

        Map params = new HashMap();

        params.put("db", addTrailingSlash(cl.getOptionValue("db")));
        params.put("sourceconfig", cl.getOptionValue("sourceconfig"));
        params.put("sourcedata",
                   addTrailingSlash(cl.getOptionValue("sourcedata")));
        params.put("targetconfig", cl.getOptionValue("targetconfig"));
        params.put("targetdata",
                   addTrailingSlash(cl.getOptionValue("targetdata")));
        return params;
    }

    /**
     */
    static int validateParams(Map params) {
        File dbf = new File((String) params.get("db"));
        if (! (dbf.exists() && dbf.isDirectory() && dbf.canRead())) {
            System.err.println("Database directory " + dbf.getPath() + " does not exist or is not a readable directory");
            return RC_ERR_DB;
        }
        File dbf2 = new File(dbf, "userdb.script");
        if (! (dbf2.exists() && dbf2.isFile() && dbf2.canRead())) {
            System.err.println("Database file " + dbf2.getPath() + " does not exist or is not a readable file");
            return RC_ERR_DB;
        }
        File sdf = new File((String) params.get("sourcedata"));
        if (! (sdf.exists() && sdf.isDirectory() && sdf.canRead())) {
            System.err.println("Source repository data directory " + sdf.getPath() + " does not exist or is not a readable directory");
            return RC_ERR_SOURCE_DATA;
        }
        File scf = new File((String) params.get("sourceconfig"));
        if (! (scf.exists() && scf.isFile() && scf.canRead())) {
            System.err.println("Source repository config file " + scf.getPath() + " does not exist or is not a readable file");
            return RC_ERR_SOURCE_CONFIG;
        }
        File tdf = new File((String) params.get("targetdata"));
        if (! tdf.exists()) {
            File tdfp = tdf.getAbsoluteFile().getParentFile();
            if (tdfp == null ||
                ! (tdfp.exists() && tdfp.isDirectory() && tdfp.canWrite())) {
                System.err.println("Target repository data directory " + tdf.getPath() + " does not exist and cannot be created");
                return RC_ERR_TARGET_DATA;
            }
        }
        File tcf = new File((String) params.get("targetconfig"));
        if (! (tcf.exists() && tcf.isFile() && tcf.canRead())) {
            System.err.println("Target repository config file " + tcf.getPath() + " does not exist or is not a file");
            return RC_ERR_TARGET_CONFIG;
        }
        return RC_OK;
    }

    /**
     * Configures source and target clients based on the parameters.
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
    static Migrator createMigrator(Map params) {
        MigratorClient sourceClient = new MigratorClient();
        sourceClient.setConfig((String) params.get("sourceconfig"));
        sourceClient.setData((String) params.get("sourcedata"));
        sourceClient.setCredentials(SOURCE_USERNAME, SOURCE_PASSWORD);

        MigratorClient targetClient = new MigratorClient();
        targetClient.setConfig((String) params.get("targetconfig"));
        targetClient.setData((String) params.get("targetdata"));
        targetClient.setCredentials(TARGET_USERNAME, TARGET_PASSWORD);

        Migrator migrator = new Migrator();
        migrator.setSourceClient(sourceClient);
        migrator.setTargetClient(targetClient);
        migrator.setDb((String) params.get("db"));

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
            migration.setDb(db);

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
     * Exits with a non-zero status code if an error occurs.
     */
    public static void main(String[] args) {
        Map params = null;
        try {
            params= processCommandLine(args);
        } catch (ParseException e) {
            System.err.println("Failure parsing command line: ");
            e.printStackTrace();
            System.exit(RC_ERR_CLI);
        }
        if (params == null) {
            System.exit(RC_OK);
        }

        int rc = validateParams(params);
        if (rc != RC_OK) {
            System.exit(rc);
        }

        Migrator migrator = createMigrator(params);
        rc = migrator.doMigration();

        System.exit(rc);
    }

    private static String addTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }
}
