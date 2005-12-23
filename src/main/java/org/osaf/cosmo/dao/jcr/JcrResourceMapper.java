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
package org.osaf.cosmo.dao.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.DavCollection;
import org.osaf.cosmo.model.DavResource;

/**
 * Utility class that converts between {@link DavResource}s and
 * {@link javax.jcr.Node}s.
 */
public class JcrResourceMapper implements JcrConstants {
    private static final Log log = LogFactory.getLog(JcrResourceMapper.class);

    /**
     * Returns a new instance of <code>DavResource</code> populated from a
     * resource node.
     */
    public static DavResource nodeToResource(Node node)
        throws RepositoryException {
        if (node.getPath().equals("/") ||
            node.isNodeType(NT_DAV_COLLECTION)) {
            return nodeToCollection(node);
        }

        DavResource resource = new DavResource();
        resource.setDisplayName(node.getProperty(NP_DAV_DISPLAYNAME).
                                getString());
        resource.setPath(JcrEscapist.hexUnescapeJcrPath(node.getPath()));
        // XXX: all other properties
        return resource;
    }

    /**
     * Copies the properties of a <code>DavResource</code> into a resource
     * node.
     */
    public static void resourceToNode(DavResource resource,
                                      Node node)
        throws RepositoryException {
        node.setProperty(NP_DAV_DISPLAYNAME, resource.getDisplayName());
        // XXX: all other properties
    }

    private static DavCollection nodeToCollection(Node node)
        throws RepositoryException {
        DavCollection resource = new DavCollection();

        if (node.getPath().equals("/")) {
            resource.setDisplayName("/");
            resource.setPath("/");
            // XXX: all other properties
            return resource;
        }

        resource.setDisplayName(node.getProperty(NP_DAV_DISPLAYNAME).
                                getString());
        resource.setPath(JcrEscapist.hexUnescapeJcrPath(node.getPath()));
        // XXX: all other properties
        return resource;
    }
}
