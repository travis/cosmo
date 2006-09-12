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
package org.osaf.cosmo.ui.home;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.dao.NoSuchResourceException;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CalendarEventItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.ui.CosmoAction;
import org.osaf.cosmo.ui.UIConstants;
import org.osaf.cosmo.ui.bean.CalendarBean;
import org.osaf.cosmo.ui.bean.EventBean;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Action for browsing a user's home directory.
 */
public class HomeDirectoryBrowserAction extends CosmoAction {
    private static final Log log =
        LogFactory.getLog(HomeDirectoryBrowserAction.class);

    private ContentService contentService;

    /** */
    public static final String PARAM_PATH = "path";
    /** */
    public static final String PARAM_TICKET = "ticket";
    /** */
    public static final String ATTR_PATH = "Path";
    /** */
    public static final String ATTR_COLLECTION = "Collection";
    /** */
    public static final String ATTR_ITEM = "Item";
    /** */
    public static final String ATTR_CALENDAR = "Calendar";
    /** */
    public static final String ATTR_EVENT = "Event";
    /** */
    public static final String FWD_COLLECTION = "collection";
    /** */
    public static final String FWD_ITEM = "item";
    /** */
    public static final String FWD_EVENT = "event";
    /** */
    public static final String FWD_CALENDAR = "calendar";

    /**
     * Retrieves the item or collection specified by the
     * {@link #PARAM_PATH} parameter and sets it into the
     * {@link #ATTR_COLLECTION} or {@link #ATTR_ITEM} request
     * attribute as appropriate.
     */
    public ActionForward browse(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        String path = request.getParameter(PARAM_PATH);

        Item item = getItem(path);

        addTitleParam(request, item.getName());
        request.setAttribute(ATTR_PATH, path);

        if (item instanceof CollectionItem) {
            CollectionItem collection = (CollectionItem) item;
            request.setAttribute(ATTR_COLLECTION, item);
            return mapping.findForward(FWD_COLLECTION);
        }

        request.setAttribute(ATTR_ITEM, item);
        return mapping.findForward(FWD_ITEM);
    }

    /**
     * Removes the item or collection specified by the
     * {@link #PARAM_PATH} parameter.
     */
    public ActionForward remove(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        String path = request.getParameter(PARAM_PATH);

        if (log.isDebugEnabled()) {
            log.debug("removing item at " + path);
        }

        contentService.removeItem(path);

        String parentPath = path.substring(0, path.lastIndexOf('/'));

        return copyForward(mapping.findForward(UIConstants.FWD_SUCCESS),
                           parentPath);
    }

    /**
     * Retrieves the event item or calendar collection specified
     * by the {@link #PARAM_PATH} parameter, extracts the calendar
     * content and sets it into the {@link #ATTR_CALENDAR} or
     * {@link #ATTR_EVENT} request attribute, and sets the collection
     * or item into the {@link #ATTR_COLLECTION} or
     * {@link #ATTR_ITEM} request attribute as appropriate.
     */
    public ActionForward view(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        String path = request.getParameter(PARAM_PATH);

        Item item = getItem(path);
        addTitleParam(request, item.getName());

        request.setAttribute(ATTR_PATH, path);

        if (item instanceof CalendarEventItem) {
            CalendarEventItem event = (CalendarEventItem) item;
            request.setAttribute(ATTR_ITEM, item);
            request.setAttribute(ATTR_EVENT, new EventBean(event));
            return mapping.findForward(FWD_EVENT);
        }
        else if (item instanceof CalendarCollectionItem) {
            CalendarCollectionItem calendar =
                (CalendarCollectionItem) item;
            request.setAttribute(ATTR_COLLECTION, item);
            request.setAttribute(ATTR_CALENDAR, new CalendarBean(calendar));
            return mapping.findForward(FWD_CALENDAR);
        }

        throw new ServletException("item of type " +
                                   item.getClass().getName() + " at " +
                                   path + " cannot be viewed" );
    }

    /**
     * Downloads the contents of the item specified by the
     * {@link #PARAM_PATH} parameter.
     */
    public ActionForward download(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
        throws Exception {
        String path = request.getParameter(PARAM_PATH);

        Item item = getItem(path);

        if (item instanceof ContentItem) {
            spoolItem((ContentItem) item, request, response);
            return null;
        }
        else if (item instanceof CalendarCollectionItem) {
            spoolCalendar((CalendarCollectionItem) item, request,
                          response);
            return null;
        }
        throw new ServletException("item of type " +
                                   item.getClass().getName() +
                                   " at " + path + " cannot be downloaded");
    }

    /**
     * Grants the ticket specified by the input form to the item
     * specified by the form.
     */
    public ActionForward grantTicket(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
        throws Exception {
        TicketForm ticketForm = (TicketForm) form;

        if (isCancelled(request)) {
            String path = ticketForm.getPath();
            ticketForm.reset(mapping, request);
            return copyForward(mapping.findForward(UIConstants.FWD_CANCEL),
                               path);
        }

        if (log.isDebugEnabled()) {
            log.debug("granting ticket to item at " +
                      ticketForm.getPath());
        }

        Ticket ticket = ticketForm.getTicket();
        ticket.setOwner(getSecurityManager().getSecurityContext().getUser());

        contentService.createTicket(ticketForm.getPath(), ticket);

        return copyForward(mapping.findForward(UIConstants.FWD_SUCCESS),
                           ticketForm.getPath());
    }

    /**
     * Revokes the ticket specified by the {@link #PARAM_TICKET}
     * parameter from the item specified by the
     * {@link #PARAM_PATH} parameter.
     */
    public ActionForward revokeTicket(ActionMapping mapping,
                                      ActionForm form,
                                      HttpServletRequest request,
                                      HttpServletResponse response)
        throws Exception {
        String path = request.getParameter(PARAM_PATH);
        String id = request.getParameter(PARAM_TICKET);

        if (log.isDebugEnabled()) {
            log.debug("revoking ticket " + id + " from item at " + path);
        }

        contentService.removeTicket(path, id);

        return copyForward(mapping.findForward(UIConstants.FWD_SUCCESS),
                           path);
    }

    /**
     */
    public ContentService getContentService() {
        return contentService;
    }

    /**
     */
    public void setContentService(ContentService service) {
        this.contentService = service;
    }

    private Item getItem(String path) {
        Item item = contentService.findItemByPath(path);
        if (item == null) {
            throw new NoSuchResourceException(path);
        }
        return item;
    }

    private void spoolItem(ContentItem item,
                           HttpServletRequest request,
                           HttpServletResponse response)
        throws IOException {
        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        if (item.getContentType() != null) {
            response.setContentType(item.getContentType());
        }
        else {
            response.setContentType("application/octet-stream");
        }
        if (item.getContentLength() != null) {
            response.setContentLength(item.getContentLength().intValue());
        }
        if (item.getContentEncoding() != null) {
            response.setCharacterEncoding(item.getContentEncoding());
        }
        if (item.getContentLanguage() != null) {
            response.setHeader("Content-Language",
                               item.getContentLanguage());
        }

        // spool data
        OutputStream out = response.getOutputStream();
        // XXX no stram api on ContentItem
        out.write(item.getContent());
//         InputStream in = item.getContent();
//         try {
//             byte[] buffer = new byte[8192];
//             int read;
//             while ((read = in.read(buffer)) >= 0) {
//                 out.write(buffer, 0, read);
//             }
//         } finally {
//             in.close();
//         }
        response.flushBuffer();
    }

    private void spoolCalendar(CalendarCollectionItem collection,
                               HttpServletRequest request,
                               HttpServletResponse response)
        throws IOException, ParserException {
        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        // XXX we can't know content length unless we write to a temp
        // item and then spool that

        // spool data
        CalendarOutputter outputter = new CalendarOutputter();
        // since the content was validated when the event item was
        // imported, there's no need to do it here
        outputter.setValidating(false);
        try {
            outputter.output(collection.getCalendar(),
                             response.getOutputStream());
        } catch (ValidationException e) {
            log.error("invalid output calendar?!", e);
        }
        response.flushBuffer();
    }
}
