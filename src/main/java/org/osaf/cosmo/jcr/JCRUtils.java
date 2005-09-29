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
package org.osaf.cosmo.jcr;

import java.util.Calendar;
import java.util.Date;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Utilities for working with JCR in Cosmo.
 */
public class JCRUtils {
    private static final Log log = LogFactory.getLog(JCRUtils.class);

    /**
     * Return the node at the given path.
     */
    public static Node findNode(Session session, String path)
        throws RepositoryException {
        Item item = session.getItem(path);
        if (! item.isNode()) {
            throw new InvalidDataAccessResourceUsageException("item at path " + path + " is not a node");
        }
        return (Node) item;
    }

    /**
     * Find the deepest existing node on the given path.
     */
    public static Node findDeepestExistingNode(Session session, String path)
        throws RepositoryException {
        // try for the deepest node first
        try {
            return findNode(session, path);
        } catch (PathNotFoundException e) {
            // not there, so we'll look for an ancestor
        }

        // walk the path to find the deepest existing node from the
        // top down
        Node node = session.getRootNode();
        if (path.equals("/")) {
            return node;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] names = path.split("/");
        Node parentNode = null;
        for (int i=0; i<names.length; i++) {
            try {
                parentNode = node;
                node = parentNode.getNode(names[i]);
            } catch (PathNotFoundException e) {
                // previous one was the last existing
                node = parentNode;
                break;
            }
        }

        return node;
    }

    /**
     */
    public static Date getCreated(Node node)
        throws RepositoryException {
        return getDateValue(node, CosmoJcrConstants.NP_JCR_CREATED);
    }

    /**
     */
    public static void setCreated(Node node, Date date)
        throws RepositoryException {
        setDateValue(node, CosmoJcrConstants.NP_JCR_CREATED, null);
    }

    /**
     */
    public static Date getLastModified(Node node)
        throws RepositoryException {
        return getDateValue(node, CosmoJcrConstants.NP_JCR_LASTMODIFIED);
    }

    /**
     */
    public static void setLastModified(Node node, Date date)
        throws RepositoryException {
        setDateValue(node, CosmoJcrConstants.NP_JCR_LASTMODIFIED, null);
    }

    // low level accessors and mutators for specifically-typed JCR
    // property values

    /**
     */
    public static Date getDateValue(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getDate().getTime();
    }

    /**
     */
    public static void setDateValue(Node node, String property, Date value)
        throws RepositoryException {
        Calendar calendar = Calendar.getInstance();
        if (value != null) {
            calendar.setTime(value);
        }
        node.setProperty(property, calendar);
    }

    /**
     */
    public static String getStringValue(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getString();
    }

    /**
     */
    public static Boolean getBooleanValue(Node node, String property)
        throws RepositoryException {
        return new Boolean(node.getProperty(property).getBoolean());
    }

    /**
     */
    public static Value[] getValues(Node node, String property)
        throws RepositoryException {
        return node.getProperty(property).getValues();
    }
}
