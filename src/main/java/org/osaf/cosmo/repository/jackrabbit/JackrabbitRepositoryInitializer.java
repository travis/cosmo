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
package org.osaf.cosmo.repository.jackrabbit;

import java.io.InputStream;
import java.util.Date;

import javax.jcr.Credentials;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.repository.PathTranslator;
import org.osaf.cosmo.repository.RepositoryInitializer;
import org.osaf.cosmo.repository.RepositoryInitializationException;
import org.osaf.cosmo.repository.SchemaConstants;
import org.osaf.cosmo.repository.UserMapper;

/**
 * An implementation of {@link RepositoryInitializer} that operates
 * against a Jackrabbit repository.
 */
public class JackrabbitRepositoryInitializer
    implements RepositoryInitializer, SchemaConstants {
    private static final Log log =
        LogFactory.getLog(JackrabbitRepositoryInitializer.class);

    /** */
    public static final String RESOURCE_SCHEMA = "cosmo-schema.cnd";

    private Credentials credentials;
    private Repository repository;
    private String workspaceName;

    // RepositoryInitializer methods

    /**
     * Registers Cosmo-specific namespaces and node types and loads
     * seed data. The repository is only initialized if no root user
     * is found in the repository. The method returns
     * <code>true</code> if an initialization actually occurred.
     */
    public boolean initialize()
        throws RepositoryInitializationException {
        Session session = null;
        try {
            session = repository.login(credentials, workspaceName);
        } catch (RepositoryException e) {
            throw new RepositoryInitializationException("cannot log into repository", e);
        }

        if (isAlreadyInitialized(session)) {
            return false;
        }

        log.info("Initializing repository");

        registerNamespaces(session);
        registerNodeTypes(session);
        addOverlordNode(session);
        addSystemNodes(session);

        return true;
    }

    // package protected for test purposes

    boolean isAlreadyInitialized(Session session)
        throws RepositoryInitializationException {
        try {
            String overlordPath =
                PathTranslator.toRepositoryPath("/" + User.USERNAME_OVERLORD);
            if (! session.itemExists(overlordPath)) {
                return false;
            }
            return true;
        } catch (RepositoryException e) {
            throw new RepositoryInitializationException("cannot check for overlord existence", e);
        }
    }

    void registerNamespaces(Session session)
        throws RepositoryInitializationException {
        if (log.isDebugEnabled()) {
            log.debug("registering namespaces");
        }

        try {
            NamespaceRegistry registry =
                session.getWorkspace().getNamespaceRegistry();
            registry.registerNamespace(PREFIX_DAV, NS_DAV);
            registry.registerNamespace(PREFIX_CALENDAR, NS_CALENDAR);
            registry.registerNamespace(PREFIX_TICKET, NS_TICKET);
            registry.registerNamespace(PREFIX_COSMO, NS_COSMO);
        } catch (RepositoryException e) {
            throw new RepositoryInitializationException("cannot register namespaces", e);
        }
    }

    void registerNodeTypes(Session session)
        throws RepositoryInitializationException {
        if (log.isDebugEnabled()) {
            log.debug("registering node types");
        }

        InputStream schema = getClass().getClassLoader().
            getResourceAsStream(RESOURCE_SCHEMA);
        if (schema == null) {
            throw new RepositoryInitializationException("can't find schema resource " + RESOURCE_SCHEMA);
        }

        try {
            JackrabbitNodeTypeManager ntmgr = (JackrabbitNodeTypeManager)
                session.getWorkspace().getNodeTypeManager();
            ntmgr.registerNodeTypes(schema,
                                    JackrabbitNodeTypeManager.TEXT_X_JCR_CND);
        } catch (Exception e) {
            throw new RepositoryInitializationException("cannot register node types", e);
        }
    }

    void addOverlordNode(Session session)
        throws RepositoryInitializationException {
        if (log.isDebugEnabled()) {
            log.debug("adding overlord node");
        }

        try {
            String name = User.USERNAME_OVERLORD;

            String n1 = PathTranslator.toRepositoryPath(name.substring(0, 1));
            Node l1 = session.getRootNode().addNode(n1, NT_UNSTRUCTURED);

            String n2 = PathTranslator.toRepositoryPath(name.substring(0, 2));
            Node l2 = l1.addNode(n2, NT_UNSTRUCTURED);

            String n3 = PathTranslator.toRepositoryPath(name);
            Node l3 = l2.addNode(n3, NT_BASE);

            User overlord = new User();
            overlord.setUsername(User.USERNAME_OVERLORD);
            overlord.setFirstName("Cosmo");
            overlord.setLastName("Administrator");
            overlord.setPassword("32a8bd4d676f4fef0920c7da8db2bad7");
            overlord.setEmail("root@localhost");
            overlord.setAdmin(Boolean.TRUE);
            overlord.setDateCreated(new Date());
            overlord.setDateModified(overlord.getDateCreated());
            UserMapper.userToNode(overlord, l3);

            session.save();
        } catch (RepositoryException e) {
            throw new RepositoryInitializationException("cannot add overlord node", e);
        }
    }

    void addSystemNodes(Session session)
        throws RepositoryInitializationException {
        if (log.isDebugEnabled()) {
            log.debug("adding system nodes");
        }

        try {
            Node system = session.getRootNode().
                addNode(NN_COSMO_SYSTEM, NT_UNSTRUCTURED);
            Node schema = system.addNode(NN_COSMO_SCHEMA, NT_UNSTRUCTURED);
            schema.setProperty(NP_COSMO_SCHEMA_VERSION,
                               CosmoConstants.PRODUCT_VERSION);

            session.save();
        } catch (RepositoryException e) {
            throw new RepositoryInitializationException("cannot add system nodes", e);
        }
    }

    // our methods

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
    public Repository getRepository() {
        return repository;
    }

    /**
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     */
    public String getWorkspaceName() {
        return workspaceName;
    }

    /**
     */
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }
}
