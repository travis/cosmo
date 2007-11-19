/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.webcal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.icalendar.ICalendarOutputter;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.service.ContentService;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A simple servlet that provides a "webcal"-style iCalendar
 * representation of a calendar collection.
 */
public class WebcalServlet extends HttpServlet implements ICalendarConstants {
    private static final Log log = LogFactory.getLog(WebcalServlet.class);
    private static final String BEAN_CONTENT_SERVICE = "contentService";

    private WebApplicationContext wac;
    private ContentService contentService;

    // HttpServlet methods

    /**
     * Handles GET requests for calendar collections. Returns a 200
     * <code>text/calendar</code> response containing an iCalendar
     * representation of all of the calendar items within the
     * collection.
     *
     * Returns 404 if the request's path info does not specify a
     * collection path or if the identified collection is not found.
     *
     * Returns 405 if the item with the identified uid is not a
     * calendar collection.
     */
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (log.isDebugEnabled())
            log.debug("handling GET for " + req.getPathInfo());

        // requests will usually come in with the collection's display
        // name appended to the collection path so that clients will
        // save the file with that name
        CollectionPath cp = CollectionPath.parse(req.getPathInfo(), true);
        if (cp == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Item item = contentService.findItemByUid(cp.getUid());
        if (item == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (! (item instanceof CollectionItem)) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                           "Requested item not a collection");
            return;
        }

        CollectionItem collection = (CollectionItem) item;
        if (StampUtils.getCalendarCollectionStamp(collection) == null) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                           "Requested item not a calendar collection");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(ICALENDAR_MEDIA_TYPE);
        resp.setCharacterEncoding("UTF-8");

        // send Content-Disposition to provide another hint to clients
        // on how to save and name the downloaded file
        String filename =
            collection.getDisplayName() + "." + ICALENDAR_FILE_EXTENSION;
        resp.setHeader("Content-Disposition",
                       "attachment; filename=\"" + filename + "\"");

        ICalendarOutputter.output(collection, resp.getOutputStream());
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
    public void init() throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (wac != null) {
            if (contentService == null)
                contentService = (ContentService)
                    getBean(BEAN_CONTENT_SERVICE, ContentService.class);
        }
        
        if (contentService == null)
            throw new ServletException("content service must not be null");
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

    // private methods

    private Object getBean(String name, Class clazz)
        throws ServletException {
        try {
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new ServletException("Error retrieving bean " + name + " of type " + clazz + " from web application context", e);
        }
    }
}
