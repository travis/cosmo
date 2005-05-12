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

import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.impl.CosmoSecurityManagerImpl;

import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.ItemId;
import org.apache.jackrabbit.core.MalformedPathException;
import org.apache.jackrabbit.core.Path;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.AMContext;

/**
 * An implementation of Jackrabbit's
 * {@link org.apache.jackrabbit.core.security.AccessManager} which
 * uses {@link CosmoSecuritymanager} to perform access control for the
 * Cosmo repository.
 *
 * The only supported workspace is the user home directory workspace,
 * which must be named by the <code>homedirWorkspaceName</code>
 * parameter in the repository configuration.
 */
public class CosmoAccessManager implements AccessManager {
    private static final Log log = LogFactory.getLog(CosmoAccessManager.class);

    private HierarchyManager hierarchyManager;
    private String homedirWorkspaceName;
    private boolean initialized;
    private boolean root;
    private CosmoSecurityContext securityContext;
    private CosmoSecurityManager securityManager;

    /**
     */
    public CosmoAccessManager() {
        initialized = false;
        root = false;
    }

    /* ----- AccessManager methods ----- */

    /**
     * Initialize this <code>AccessManager</code>.
     *
     * @param context access manager context
     * @throws AccessDeniedException for any workspace other than the
     * homedir workspace
     * @throws Exception
     */
    public void init(AMContext context)
            throws AccessDeniedException, Exception {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        if (homedirWorkspaceName == null) {
            throw new IllegalStateException("homedirWorkspaceName not defined");
        }

        if (! canAccess(context.getWorkspaceName())) {
            throw new AccessDeniedException("access denied to workspace " +
                                            context.getWorkspaceName());
        }

        securityManager = createSecurityManager();
        securityContext =
            securityManager.getSecurityContext(context.getSubject());

        hierarchyManager = context.getHierarchyManager();

        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void close() throws Exception {
        if (! initialized) {
            throw new IllegalStateException("not initialized");
        }

        initialized = false;
    }

    /**
     * Determines whether the specified <code>permissions</code> are
     * granted on the item with the specified <code>id</code>
     * according to the following rules:
     *
     * <ol>
     * <li>Users in the root role have all permissions on all items.
     * <li>Users who own a home directory have all permissions on the
     * item representing the user's home directory and all
     * descendents.
     * </ol>
     */
    public void checkPermission(ItemId id, int permissions)
            throws AccessDeniedException, ItemNotFoundException,
            RepositoryException {
        if (! initialized) {
            throw new IllegalStateException("not initialized");
        }

        // root users can do anything
        if (isRoot()) {
            return;
        }

        Path path = null;
        try {
            path = id2path(id);
        } catch (ItemNotFoundException e) {
            // not quite sure why jackrabbit tries to check
            // permissions on items that are being deleted, but it
            // does, so just humor it.
            return;
        }

        // Jackrabbit seems to require read privilege on the root node
        // when deleting a resource in a home directory, so allow
        // anybody to read it but nothing else
        if (path.denotesRoot()) {
            if ((permissions & READ) == READ) {
                return;
            }
            log.error("write access not supported for root node");
            throw new AccessDeniedException();
        }

        // Jackrabbit does not use an AccessManager to enforce
        // security for version storage (yet), but it does check read
        // permissions for initializing a versionable node, so we need
        // to allow it to do that.
        // XXX remove when Jackrabbit gives us the ability to look up
        // the versionable node from a version storage item
        if (isVersionStorageItem(path)) {
            if ((permissions & WRITE) == WRITE ||
                (permissions & REMOVE) == REMOVE) {
                log.error("write access not supported for version storage");
                throw new AccessDeniedException();
            }
            return;
        }

        if (isOwner(path)) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("permissions " + permissions + " check failed for " +
                      " item at path " + id2path(id));
        }
        throw new AccessDeniedException("access denied to item " + id);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGranted(ItemId id, int permissions)
            throws ItemNotFoundException, RepositoryException {
        if (! initialized) {
            throw new IllegalStateException("not initialized");
        }

        // root users can do anything
        if (isRoot()) {
            return true;
        }

        Path path = null;
        try {
            path = id2path(id);
        } catch (ItemNotFoundException e) {
            // not quite sure why jackrabbit tries to check
            // permissions on items that are being deleted, but it
            // does, so just humor it.
            return true;
        }

        // Jackrabbit seems to require read privilege on the root node
        // when deleting a resource in a home directory, so allow
        // anybody to read it but nothing else
        if (path.denotesRoot()) {
            if ((permissions & READ) == READ) {
                return true;
            }
            log.error("write access not supported for root node");
            return false;
        }

        // Jackrabbit does not use an AccessManager to enforce
        // security for version storage (yet), but it does check read
        // permissions for initializing a versionable node, so we need
        // to allow it to do that.
        // XXX remove when Jackrabbit gives us the ability to look up
        // the versionable node from a version storage item
        if (isVersionStorageItem(path)) {
            if ((permissions & WRITE) == WRITE ||
                (permissions & REMOVE) == REMOVE) {
                log.error("write access not supported for version storage");
                return false;
            }

            return true;
        }

        if (! isOwner(path)) {
            if (log.isDebugEnabled()) {
                log.debug("permissions " + permissions + " not granted for " +
                          " item at path " + id2path(id));
            }
            return false;
        }

        return true;
    }

    /**
     * The subject is granted access to the homedir workspace by
     * definition (all authenticated users may access the
     * workspace). All other workspaces are denied.
     */
    public boolean canAccess(String workspaceName)
        throws NoSuchWorkspaceException, RepositoryException {
        return workspaceName.equals(homedirWorkspaceName);
    }

    /* ----- our methods ----- */

    /**
     * Determines whether or not the <code>subject</code> represents a
     * user with root privileges.
     */
    public boolean isRoot() {
        return securityContext.inRootRole();
    }

    /**
     * Determines whether or not the <code>subject</code> represents
     * the owner of the home directory item for the item identified by
     * <code>id</code>.
     *
     * Assumes that the name of a home directory node is exactly the
     * same as the username of the home directory's owner.
     *
     * @throws Exception
     */
    public boolean isOwner(Path path)
        throws RepositoryException {
        // if the item is a version history node, then find the
        // versionable node it represents and use that path instead
        // it will be of type nt:versionHistory with a 
        // jcr:versionableUuid property or of type nt:version whose
        // parent is the nt:versionHistory
        // XXX: can't do this until Jackrabbit gives us a system
        // session to look up the versionable node

        // find the subject's username
        String username = securityContext.getUser().getUsername();

        // find the item representing the home dir
        Path homedirPath = path.getAncestorCount() > 1 ?
            path.getAncestor(path.getAncestorCount() - 1) :
            path;
        String homedirName =
            homedirPath.getNameElement().getName().getLocalName();

        return homedirName.equals(username);
    }

    /**
     */
    protected CosmoSecurityManager createSecurityManager() {
        return new CosmoSecurityManagerImpl();
    }

    /**
     */
    public HierarchyManager getHierarchyManager() {
        return hierarchyManager;
    }

    /**
     */
    public String getHomedirWorkspaceName() {
        return homedirWorkspaceName;
    }

    /**
     */
    public void setHomedirWorkspaceName(String homedirWorkspaceName) {
        this.homedirWorkspaceName = homedirWorkspaceName;
    }

    /**
     */
    public CosmoSecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    private boolean isVersionStorageItem(Path path)
        throws RepositoryException {
        Path.PathElement[] pathElements = path.getElements();
        if (pathElements.length < 3) {
            return false;
        }
        if (! pathElements[0].denotesRoot()) {
            return false;
        }
        // XXX: check namespace?
        if (! pathElements[1].getName().getLocalName().
            equals("system")) {
            return false;
        }
        if (! pathElements[2].getName().getLocalName().
            equals("versionStorage")) {
            return false;
        }

        return true;
    }

    private Path id2path(ItemId id)
        throws RepositoryException {
        try {
            return getHierarchyManager().getPath(id).getCanonicalPath();
        } catch (MalformedPathException e) {
            throw new RepositoryException("malformed path for id " + id, e);
        }
    }
}
