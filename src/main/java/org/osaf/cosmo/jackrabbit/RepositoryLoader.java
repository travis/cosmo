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
package org.osaf.cosmo.jackrabbit;

import java.util.Date;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCRUtils;
import org.osaf.cosmo.model.User;

/**
 * Loads Cosmo data into a Jackrabbit repository.
 */
public class RepositoryLoader {
    private static final Log log = LogFactory.getLog(RepositoryLoader.class);

    private String configFilePath;
    private String repositoryHomedirPath;
    private Credentials credentials;

    /**
     * Loads the overlord (root) user.
     */
    public void loadOverlord()
        throws RepositoryException {
        Repository repository = openRepository();
        Session session = repository.login(getCredentials());

        Node node = session.getRootNode().addNode(User.USERNAME_OVERLORD,
                                                  CosmoJcrConstants.NT_BASE);
        node.setProperty(CosmoJcrConstants.NP_COSMO_USERNAME, "root");
        node.setProperty(CosmoJcrConstants.NP_COSMO_PASSWORD, "cosmo");
        node.setProperty(CosmoJcrConstants.NP_COSMO_FIRSTNAME, "Cosmo");
        node.setProperty(CosmoJcrConstants.NP_COSMO_LASTNAME, "Administrator");
        node.setProperty(CosmoJcrConstants.NP_COSMO_EMAIL, "root@localhost");
        node.setProperty(CosmoJcrConstants.NP_COSMO_ADMIN, true);
        JCRUtils.setDateValue(node, CosmoJcrConstants.NP_COSMO_DATECREATED,
                              new Date());
        JCRUtils.setDateValue(node, CosmoJcrConstants.NP_COSMO_DATEMODIFIED,
                              new Date());

        session.logout();
        closeRepository(repository);
    }

    /**
     */
    protected Repository openRepository()
        throws RepositoryException {
        RepositoryConfig config =
            RepositoryConfig.create(configFilePath, repositoryHomedirPath);
        return RepositoryImpl.create(config);
    }

    /**
     */
    protected void closeRepository(Repository repository)
        throws RepositoryException {
        ((RepositoryImpl) repository).shutdown();
    }

    /**
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     */
    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     */
    public String getRepositoryHomedirPath() {
        return repositoryHomedirPath;
    }

    /**
     */
    public void setRepositoryHomedirPath(String repositoryHomedirPath) {
        this.repositoryHomedirPath = repositoryHomedirPath;
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
    public void setCredentials(final String username, String password) {
        credentials = new SimpleCredentials(username, password.toCharArray());
    }
}
