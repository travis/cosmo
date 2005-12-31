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
package org.osaf.cosmo.feed;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.NoSuchResourceException;
import org.osaf.cosmo.model.CollectionResource;
import org.osaf.cosmo.model.Resource;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.security.CosmoSecurityManager;

import org.springframework.beans.BeansException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet which generates syndication feeds describing Cosmo
 * resources.
 */
public class FeedServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(FeedServlet.class);

    private static final String BEAN_HOME_DIRECTORY_SERVICE =
        "homeDirectoryService";
    private static final String BEAN_SECURITY_MANAGER =
        "securityManager";

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
    private HomeDirectoryService homeDirectoryService;
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

        homeDirectoryService = (HomeDirectoryService)
            getBean(BEAN_HOME_DIRECTORY_SERVICE, HomeDirectoryService.class);
        securityManager = (CosmoSecurityManager)
            getBean(BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
    }

    // HttpServlet methods

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

        Resource resource = null;
        try {
            resource = homeDirectoryService.getResource(path);
        } catch (NoSuchResourceException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (InvalidDataAccessResourceUsageException e) {
            // this happens when an item exists in the repository at
            // that path, but it does not represent a dav resource or
            // collection, so pretend that there's no resource there
            // at all
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (! (resource instanceof CollectionResource)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                           "Feeds not supported for non-collection resource");
            return;
        }

        spoolAtom10Feed((CollectionResource) resource, req, resp);
    }

    // our methods

    /**
     */
    public WebApplicationContext getWebApplicationContext() {
        return wac;
    }

    private void spoolAtom10Feed(CollectionResource collection,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
        throws IOException {
        // set headers
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/atom+xml");
        resp.setCharacterEncoding("UTF-8");
        // XXX we can't know content length unless we write to a temp
        // file and then spool that

        Feed feed = collection.getAtomFeed();

        // make ids and link hrefs absolute
        feed.setId(encodeURL(req, homePath + feed.getId(), false));
        for (Iterator i=feed.getAlternateLinks().iterator(); i.hasNext();) {
            Link link = (Link) i.next();
            if (link.getRel().equals("self")) {
                link.setHref(encodeURL(req, FEED_ATOM10_URI + link.getHref()));
            }
            else if (link.getRel().equals("alternate")) {
                link.setHref(encodeURL(req, browsePath + link.getHref(),
                                       false));
            }
        }
        for (Iterator i=feed.getEntries().iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            entry.setId(encodeURL(req, homePath + entry.getId(), false));
            for (Iterator j=entry.getAlternateLinks().iterator();
                 j.hasNext();) {
                Link link = (Link) j.next();
                if (link.getRel().equals("alternate")) {
                    link.setHref(encodeURL(req, browsePath + link.getHref(),
                                           false));
                }
            }
        }

        // spool data
        try {
            WireFeedOutput outputter = new WireFeedOutput();
            outputter.output(feed, resp.getWriter());
        } catch (FeedException e) {
            log.error("can't get Atom feed for collection " +
                      collection.getPath(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           e.getMessage());
            return;
        }

        resp.flushBuffer();
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
