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
package org.osaf.cosmo.spring.mvc.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.NoSuchResourceException;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.ui.bean.CalendarBean;
import org.osaf.cosmo.ui.bean.EventBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Action for browsing a user's home directory.
 */
public class HomeDirectoryBrowsingController extends MultiActionController {
    private static final Log log = LogFactory
            .getLog(HomeDirectoryBrowsingController.class);

    private ContentService contentService;

    private String calendarView;

    private String eventView;

    private String browseCollectionView;

    private String browseItemView;

    private String removeViewBase;

    private String revokeTicketBaseView;

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
     * The request parameter in which view title parameters are stored:
     * <code>TitleParam</code>
     */
    public static final String ATTR_TITLE_PARAM = "TitleParam";

    /**
     * Retrieves the item or collection specified by the {@link #PARAM_PATH}
     * parameter and sets it into the {@link #ATTR_COLLECTION} or
     * {@link #ATTR_ITEM} request attribute as appropriate.
     */
    public ModelAndView browse(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String path = getPath(request);
        Item item = getItem(path);

        addTitleParam(request, item.getDisplayName());
        request.setAttribute(ATTR_PATH, path);

        if (item instanceof CollectionItem) {
            CollectionItem collection = (CollectionItem) item;
            request.setAttribute(ATTR_COLLECTION, item);
            return new ModelAndView(browseCollectionView);
        }

        request.setAttribute(ATTR_ITEM, item);
        return new ModelAndView(browseItemView);
    }

    /**
     * Removes the item or collection specified by the {@link #PARAM_PATH}
     * parameter.
     */
    public ModelAndView remove(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String path = getPath(request);

        if (log.isDebugEnabled()) {
            log.debug("removing item at " + path);
        }

        contentService.removeItem(path);

        String parentPath = path.substring(0, path.lastIndexOf('/'));

        return new ModelAndView(removeViewBase + parentPath);

    }

    /**
     * Retrieves the event item or calendar collection specified by the
     * {@link #PARAM_PATH} parameter, extracts the calendar content and sets it
     * into the {@link #ATTR_CALENDAR} or {@link #ATTR_EVENT} request attribute,
     * and sets the collection or item into the {@link #ATTR_COLLECTION} or
     * {@link #ATTR_ITEM} request attribute as appropriate.
     */
    public ModelAndView view(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String path = getPath(request);

        Item item = getItem(path);
        addTitleParam(request, item.getDisplayName());

        request.setAttribute(ATTR_PATH, path);

        if (item.getStamp(EventStamp.class)!=null) {
            EventStamp eventStamp = EventStamp.getStamp(item);
            request.setAttribute(ATTR_ITEM, item);
            request.setAttribute(ATTR_EVENT, new EventBean(eventStamp));

            return new ModelAndView(eventView);
        } else if (item.getStamp(CalendarCollectionStamp.class)!=null) {
            CalendarCollectionStamp calendar = 
                CalendarCollectionStamp.getStamp(item);
            request.setAttribute(ATTR_COLLECTION, item);
            request.setAttribute(ATTR_CALENDAR, new CalendarBean(calendar));

            return new ModelAndView(calendarView);
        }

        throw new ServletException("item of type " + item.getClass().getName()
                + " at " + path + " cannot be viewed");
    }

    /**
     * Downloads the contents of the item specified by the {@link #PARAM_PATH}
     * parameter.
     */
    public ModelAndView download(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String path = getPath(request);

        Item item = getItem(path);

        if (item.getStamp(EventStamp.class) != null) {
            spoolEvent(EventStamp.getStamp(item), request,
                    response);
        } else if (item instanceof ContentItem) {
            spoolItem((ContentItem) item, request, response);
            return null;
        } else if (item.getStamp(CalendarCollectionStamp.class) != null) {
            spoolCalendar(CalendarCollectionStamp.getStamp(item), request, response);
            return null;
        }
        throw new ServletException("item of type " + item.getClass().getName()
                + " at " + path + " cannot be downloaded");
    }

    /**
     * Revokes the ticket specified by the {@link #PARAM_TICKET} parameter from
     * the item specified by the {@link #PARAM_PATH} parameter.
     */
    public ModelAndView revokeTicket(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String path = getPath(request);
        String id = request.getParameter(PARAM_TICKET);

        if (log.isDebugEnabled()) {
            log.debug("revoking ticket " + id + " from item at " + path);
        }

        contentService.removeTicket(path, id);

        return new ModelAndView(this.revokeTicketBaseView + path);
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

    private void spoolItem(ContentItem item, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        if (item.getContentType() != null) {
            response.setContentType(item.getContentType());
        } else {
            response.setContentType("application/octet-stream");
        }
        if (item.getContentLength() != null) {
            response.setContentLength(item.getContentLength().intValue());
        }
        if (item.getContentEncoding() != null) {
            response.setCharacterEncoding(item.getContentEncoding());
        }
        if (item.getContentLanguage() != null) {
            response.setHeader("Content-Language", item.getContentLanguage());
        }

        // spool data
        OutputStream out = response.getOutputStream();
        // XXX no stram api on ContentItem
        out.write(item.getContent());
        // InputStream in = item.getContent();
        // try {
        // byte[] buffer = new byte[8192];
        // int read;
        // while ((read = in.read(buffer)) >= 0) {
        // out.write(buffer, 0, read);
        // }
        // } finally {
        // in.close();
        // }
        response.flushBuffer();
    }

    private void spoolEvent(EventStamp event, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ParserException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/calendar");
        response.setCharacterEncoding("UTF-8");
        
        // spool data
        CalendarOutputter outputter = new CalendarOutputter();
             
        // since the content was validated when the event item was
        // imported, there's no need to do it here
        outputter.setValidating(false);
        try {
            outputter.output(event.getCalendar(), response
                    .getOutputStream());
        } catch (ValidationException e) {
            log.error("invalid output calendar?!", e);
        }
        response.flushBuffer();
    }
    
    private void spoolCalendar(CalendarCollectionStamp calendar,
            HttpServletRequest request, HttpServletResponse response)
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
            outputter.output(calendar.getCalendar(), response
                    .getOutputStream());
        } catch (ValidationException e) {
            log.error("invalid output calendar?!", e);
        }
        response.flushBuffer();
    }

    /**
     * Store a title parameter in the <code>ATTR_TITLE_PARAM</code> request
     * attribute.
     * 
     * Title parameters can be used by a view to build a page title including
     * dynamic elements that are set by the action but not known by the view at
     * rendering time. An example is using a layout JSP to include the name of
     * an object being managed via an administrative console in the title of an
     * HTML page without the layout knowing the type of object it is displaying.
     */
    public void addTitleParam(HttpServletRequest request, String param) {
        List<String> params = (List) request.getAttribute(ATTR_TITLE_PARAM);
        if (params == null) {
            params = new ArrayList<String>();
            request.setAttribute(ATTR_TITLE_PARAM, params);
        }
        params.add(param);
    }

    public void setCalendarView(String calendarView) {
        this.calendarView = calendarView;
    }

    public void setEventView(String eventView) {
        this.eventView = eventView;
    }

    public void setBrowseCollectionView(String browseCollectionView) {
        this.browseCollectionView = browseCollectionView;
    }

    public void setBrowseItemView(String browseItemView) {
        this.browseItemView = browseItemView;
    }

    public void setRemoveViewBase(String removeViewBase) {
        this.removeViewBase = removeViewBase;
    }

    public String getRevokeTicketBaseView() {
        return revokeTicketBaseView;
    }

    public void setRevokeTicketBaseView(String revokeTicketBaseView) {
        this.revokeTicketBaseView = revokeTicketBaseView;
    }
    
    private String getPath(HttpServletRequest request){
        String path = request.getParameter(PARAM_PATH);
        if (path.endsWith("/")){
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}
