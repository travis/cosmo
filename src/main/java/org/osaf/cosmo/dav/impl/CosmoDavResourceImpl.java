package org.osaf.cosmo.dav.impl;

import java. io.IOException;
import java. io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.server.simple.dav.DavResourceImpl;

/**
 * A subclass of
 * {@link org.apache.jackrabbit.server.simple.dav.DavResourceImpl}
 * that provides Cosmo-specific WebDAV behaviors.
 *
 * For now, this class is a placeholder.
 */
public class CosmoDavResourceImpl extends DavResourceImpl {

    /**
     * Create a new {@link DavResource}.
     *
     * @param locator
     * @param factory
     * @param session
     */
    public CosmoDavResourceImpl(DavResourceLocator locator,
                                DavResourceFactory factory,
                                DavSession session)
        throws RepositoryException {
        super(locator, factory, session);
    }
}
