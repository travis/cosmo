package org.osaf.cosmo.dav.impl;

import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityManager;

import java.security.PrivilegedAction;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import org.apache.jackrabbit.server.simple.dav.DavSessionImpl;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;

import org.apache.log4j.Logger;

/**
 * Implementation of the jcr-server {@link DavSessionProvider}
 * interface that uses wired-in instances of
 * {@link Repository} and {@link CosmoSecurityManager} to
 * log into the repository and provide a
 * {@link org.apache.jackrabbit.webdav.DavSession} to the request.
 */
public class CosmoDavSessionProviderImpl implements DavSessionProvider {
    private static final Logger log =
        Logger.getLogger(CosmoDavSessionProviderImpl.class);

    private Repository repository;
    private CosmoSecurityManager securityManager;
    private String workspaceName;

    /**
     * Acquires a DavSession. Upon success, the WebdavRequest will
     * reference that session.
     *
     * @param request
     * @throws DavException if a problem occurred while obtaining the
     * session
     */
    public void acquireSession(WebdavRequest request) throws DavException {
        // XXX cache dav session in web session?
        try {
            CosmoSecurityContext securityContext =
                securityManager.getSecurityContext();
            PrivilegedAction action = new PrivilegedAction() {
                    public Object run() {
                        try {
                            return repository.login(workspaceName);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            if (log.isDebugEnabled()) {
                log.debug("Logging into repository workspace " +
                          workspaceName + " as " +
                          securityContext.getUser().getUsername());
            }
            Session rs = (Session)
                Subject.doAs(securityContext.getSubject(), action);
            DavSession ds = new DavSessionImpl(rs);
            request.setDavSession(ds);
        } catch (Exception e) {
            log.error("error logging into repository", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                                   e.getMessage());
        }
    }

    /**
     * Releases the reference from the request to the session.
     *
     * @param request
     */
    public void releaseSession(WebdavRequest request) {
        request.setDavSession(null);
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
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
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
