package org.osaf.cosmo.dav.impl;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.spi.JcrDavException;

/**
 * An implementation of 
 * {@link org.apache.jackrabbit.webdav.DavResourceFactory} that
 * provides instances of {@link CosmoDavResourceImpl}.
 */
public class CosmoDavResourceFactoryImpl implements DavResourceFactory {

    private LockManager lockManager;

    /**
     */
    public DavResource createResource(DavResourceLocator locator,
                                      DavServletRequest request,
                                      DavServletResponse response)
        throws DavException {
        return createResource(locator, request.getDavSession());
    }

    /**
     */
    public DavResource createResource(DavResourceLocator locator,
                                      DavSession session)
        throws DavException {
        try {
            DavResource resource = new CosmoDavResourceImpl(locator, this,
                                                            session);
            resource.addLockManager(lockManager);
            return resource;
        } catch (RepositoryException e) {
            throw new JcrDavException(e);
        }
    }

    /**
     */
    public LockManager getLockManager() {
        return lockManager;
    }

    /**
     */
    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }
}
