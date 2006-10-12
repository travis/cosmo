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
package org.osaf.cosmo.feed;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.security.CosmoSecurityManager;

import org.springframework.beans.BeansException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet which generates syndication feeds describing Cosmo
 * items.
 */
public class FeedServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(FeedServlet.class);

    private static final String BEAN_HOME_DIRECTORY_SERVICE =
        "contentService";
    private static final String BEAN_SECURITY_MANAGER =
        "securityManager";

    // used to store the feed item between calls to getLastModified
    // and doGet
    private static ThreadLocal<Item> feedItem = new ThreadLocal<Item>();

    /** */
    public static final String INIT_HOME_PATH = "home-path";
    /** */
    public static final String INIT_BROWSE_PATH = "browse-path";
    /** */
    public static final String PARAM_PATH = "path";
    /** */
    public static final String FEED_ATOM10 = "atom_1.0";
    /** */
    public static final String FEED_ATOM10_URI = "/atom/1.0";

    private WebApplicationContext wac;
    private ContentService contentService;
    private CosmoSecurityManager securityManager;
    private String homePath;
    private String browsePath;

    /**
     * Load the servlet context's
     * {@link org.springframework.web.context.WebApplicationContext}
     * and look up support objects.
     *
     * @throws ServletException
     */
    public void init()
        throws ServletException {
        super.init();

        homePath = getServletConfig().getInitParameter(INIT_HOME_PATH);
        if (homePath == null || homePath.equals("")) {
            throw new ServletException("init parameter [" + INIT_HOME_PATH +
                                       "] empty or not found");
        }
        if (log.isDebugEnabled()) {
            log.debug("home path: " + homePath);
        }

        browsePath = getServletConfig().getInitParameter(INIT_BROWSE_PATH);
        if (browsePath == null || browsePath.equals("")) {
            throw new ServletException("init parameter [" + INIT_BROWSE_PATH +
                                       "] empty or not found");
        }
        if (log.isDebugEnabled()) {
            log.debug("browse path: " + browsePath);
        }
        wac = WebApplicationContextUtils.
            getRequiredWebApplicationContext(getServletContext());

        contentService = (ContentService)
            getBean(BEAN_HOME_DIRECTORY_SERVICE, ContentService.class);
        securityManager = (CosmoSecurityManager)
            getBean(BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
    }

    // HttpServlet methods

    /**
     */
    protected long getLastModified(HttpServletRequest req) {
        String path = req.getParameter(PARAM_PATH);
        if (path == null)
            return -1;

        Item item = contentService.findItemByPath(path);
        if (item == null)
            return -1;

        // set the item in a ThreadLocal so we don't have to look it
        // up again
        feedItem.set(item);

        if (! (item instanceof CollectionItem))
            return item.getModifiedDate().getTime();

        CollectionItem collection = (CollectionItem) item;

        Date lastMod = null;
        for (Item child : collection.getChildren()) {
            if (lastMod == null ||
                child.getModifiedDate().after(lastMod))
                lastMod = child.getModifiedDate();
        }

        return lastMod != null ? lastMod.getTime() : -1;
    }

    /**
     */
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        String path = req.getParameter(PARAM_PATH);
        if (path == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Feed URI must be of the form /atom/1.0/<path>");
            return;
        }

        // if we already found the item when calculating the last
        // modified timestamp, it's in the ThreadLocal, and we can
        // remove it since nothing else will use it
        Item item = feedItem.get();
        if (item != null) {
            feedItem.remove();
        } else {
            item = contentService.findItemByPath(path);
        }
        if (item == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // XXX: return atom item for individual item
        if (! (item instanceof CollectionItem)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                           "Feeds not supported for non-collection item");
            return;
        }

        CollectionItem collection = (CollectionItem) item;

        spoolAtom10Feed(collection, path, req, resp);
    }

    // our methods

    /**
     */
    public WebApplicationContext getWebApplicationContext() {
        return wac;
    }

    private void spoolAtom10Feed(CollectionItem collection,
                                 String path,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
        throws IOException {
        FeedGenerator generator =
            new FeedGenerator(encodeURL(req, FEED_ATOM10_URI),
                              encodeURL(req, homePath, false),
                              encodeURL(req, browsePath, false));
        Feed feed = generator.generateFeed(collection, path);

        // set headers
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/atom+xml");
        resp.setCharacterEncoding("UTF-8");
        // XXX we can't know content length unless we write to a temp
        // file and then spool that

        // spool data
        try {
            WireFeedOutput outputter = new WireFeedOutput();
            outputter.output(feed, resp.getWriter());
            resp.flushBuffer();
        } catch (FeedException e) {
            log.error("can't spool feed for collection " + path, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           e.getMessage());
            return;
        }
    }

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

    private String encodeURL(HttpServletRequest req,
                             String path) {
        return encodeURL(req, path, true);
    }

    private String encodeURL(HttpServletRequest req,
                             String path,
                             boolean withServletPath) {
        // like response.encodeURL() but is guaranteed to include the
        // scheme, host, port, context and servlet paths regardless of
        // where the session id comes from
        StringBuffer buf = new StringBuffer();
        buf.append(req.getScheme()).
            append("://").
            append(req.getServerName());
        if ((req.isSecure() && req.getServerPort() != 443) ||
            (req.getServerPort() != 80)) {
            buf.append(":").append(req.getServerPort());
        }
        if (! req.getContextPath().equals("/")) {
            buf.append(req.getContextPath());
        }
        if (withServletPath) {
            buf.append(req.getServletPath());
        }
        buf.append(path);
        return buf.toString();
    }
}
