/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.atom;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.writer.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;

import org.springframework.beans.BeansException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet which generates syndication feeds describing Cosmo
 * items.
 */
public class AtomServlet extends HttpServlet implements AtomConstants {
    private static final Log log = LogFactory.getLog(AtomServlet.class);

    private static final String BEAN_CONTENT_SERVICE =
        "contentService";
    private static final String BEAN_SERVICE_LOCATOR_FACTORY =
        "serviceLocatorFactory";

    // used to store the feed item between calls to getLastModified
    // and doGet
    private static ThreadLocal<Item> feedItem = new ThreadLocal<Item>();

    private WebApplicationContext wac;
    private ContentService contentService;
    private ServiceLocatorFactory serviceLocatorFactory;
    private String homePath;
    private String browsePath;

    // HttpServlet methods

    /**
     * Calculates the aggregate last modified time for a
     * collection. This is defined as the most recent modification
     * time of the collection or any content item within the
     * collection.
     *
     * Returns <code>-1</code> if the request's path info does not
     * represent a collection path, if no item is found for the
     * specified uid, or if the requested item is not a collection.
     */
    protected long getLastModified(HttpServletRequest req) {
        CollectionPath cp = CollectionPath.parse(req.getPathInfo());
        if (cp == null)
            return -1;

        Item item = contentService.findItemByUid(cp.getUid());
        if (item == null)
            return -1;

        // set the item in a ThreadLocal so we don't have to look it
        // up again
        feedItem.set(item);

        if (! (item instanceof CollectionItem))
            return -1;

        CollectionItem collection = (CollectionItem) item;

        Date lastMod = collection.getModifiedDate();
        for (Item child : collection.getChildren()) {
            if (child instanceof CollectionItem)
                continue;
            if (lastMod == null || child.getModifiedDate().after(lastMod))
                lastMod = child.getModifiedDate();
        }

        return lastMod.getTime();
    }

    /**
     * Handles GET requests for collections. Returns a 200
     * <code>application/atom+xml</code> response containing an Atom
     * 1.0 feed representing all of the content items within the
     * collection.
     *
     * Returns 404 if the request's path info does not specify a
     * collection path or if the identified collection is not found.
     *
     * Returns 405 if the item with the identified uid is not a
     * collection.
     */
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (log.isDebugEnabled())
            log.debug("handling GET for " + req.getPathInfo());

        CollectionPath cp = CollectionPath.parse(req.getPathInfo());
        if (cp == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // if we already found the item when calculating the last
        // modified timestamp, it's in the ThreadLocal, and we can
        // remove it since nothing else will use it
        Item item = feedItem.get();
        if (item != null)
            feedItem.remove();
        else
            item = contentService.findItemByUid(cp.getUid());

        if (item == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (! (item instanceof CollectionItem)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                           "Requested item not a collection");
            return;
        }

        CollectionItem collection = (CollectionItem) item;

        FeedGenerator generator =
            new FeedGenerator(serviceLocatorFactory.createServiceLocator(req));
        Feed feed = generator.generateFeed(collection);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(MEDIA_TYPE_ATOM);
        resp.setCharacterEncoding("UTF-8");

        Writer writer = Abdera.getNewWriterFactory().getWriter("PrettyXML");
        writer.writeTo(feed, resp.getOutputStream());
    }

    // GenericServlet methods

    /**
     * Loads the servlet context's <code>WebApplicationContext</code>
     * and wires up dependencies. If no
     * <code>WebApplicationContext</code> is found, dependencies must
     * be set manually (useful for testing).
     *
     * @throws ServletException if required dependencies are not found
     */
    public void init()
        throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (wac != null) {
            contentService = (ContentService)
                getBean(BEAN_CONTENT_SERVICE, ContentService.class);
            serviceLocatorFactory = (ServiceLocatorFactory)
                getBean(BEAN_SERVICE_LOCATOR_FACTORY,
                        ServiceLocatorFactory.class);
        }

        if (contentService == null)
            throw new ServletException("content service must not be null");
        if (serviceLocatorFactory == null)
            throw new ServletException("service locator must not be null");
    }

    // our methods

    /** */
    public ContentService getContentService() {
        return contentService;
    }

    /** */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /** */
    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    /** */
    public void setServiceLocatorFactory(ServiceLocatorFactory factory) {
        serviceLocatorFactory = factory;
    }

    // private methods

    private Object getBean(String name, Class clazz)
        throws ServletException {
        try {
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new ServletException("Error retrieving bean " + name +
                                       " of type " + clazz +
                                       " from web application context", e);
        }
    }
}
