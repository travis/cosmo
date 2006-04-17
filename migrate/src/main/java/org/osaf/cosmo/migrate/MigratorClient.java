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

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import org.apache.log4j.Logger;

/**
 * Provides a very simple facade for accessing a Jackrabit repository
 * involved in a migration.
 *
 * A migrator client manages a single migration session for a
 * repository. No other sessions may be opened on the repository while
 * the migration session is in progress.
 */
public class MigratorClient {
    private static Logger log = Logger.getLogger(MigratorClient.class);

    private String config;
    private String data;
    private String workspace;
    private Credentials credentials;
    private Session session;
    private boolean started;

    /**
     */
    public MigratorClient() {
        started = false;
    }

    /**
     * Opens the repository and returns a {@link javax.jcr.Session}
     * for the workspace name and credentials set on this
     * object.
     *
     * Once the migrator client has been started and the migration
     * session established, no additional sessions may be opened.
     */
    public Session start()
        throws MigrationException {
        if (started) {
            throw new IllegalStateException("migrator client already started");
        }
        Repository repository = openRepository();
        login(repository);
        started = true;
        return session;
    }

    /**
     * Finishes the migration session and closes the repository.
     */
    public void stop()
        throws MigrationException {
        if (! started) {
            throw new IllegalStateException("migrator client not started");
        }
        logout();
        closeRepository(session.getRepository());
        session = null;
        started = false;
    }

    /**
     */
    public boolean isStarted() {
        return started;
    }

    /**
     */
    public String getConfig() {
        return config;
    }

    /**
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     */
    public String getData() {
        return data;
    }

    /**
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     */
    public void setCredentials(String username,
                               String password) {
        credentials = new SimpleCredentials(username, password.toCharArray());
    }

    /**
     */
    public Session getSession() {
        return session;
    }

    private void login(Repository repository)
        throws MigrationException {
        try {
            session = repository.login(credentials, workspace);
        } catch (RepositoryException e) {
            throw new MigrationException("could not log into " + workspace +
                                         " workspace");
        }
    }

    private void logout()
        throws MigrationException {
        session.logout();
    }

    private Repository openRepository()
        throws MigrationException {
        try {
            RepositoryConfig rc = RepositoryConfig.create(config, data);
            return RepositoryImpl.create(rc);
        } catch (RepositoryException e) {
            throw new MigrationException("could not open repository", e);
        }
    }

    private void closeRepository(Repository repository)
        throws MigrationException {
        ((RepositoryImpl) repository).shutdown();
    }
}
