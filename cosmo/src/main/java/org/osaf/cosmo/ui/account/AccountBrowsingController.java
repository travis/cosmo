/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.NoSuchResourceException;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.Permission;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ItemPath;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.ui.bean.CalendarBean;
import org.osaf.cosmo.ui.bean.EventBean;
import org.osaf.cosmo.util.PathUtil;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Controller for browsing the contents of a user's account.
 * <p>
 * Each controller method acts on the item specified by the
 * <code>path</code> request parameter.
 * <p>
 * Each controller method adds the item's display name (and optionally
 * other pieces of data) to a <code>List</code> in the
 * <code>TitleParam</code> request attribute which can be used by
 * views to render dynamic page titles.
 */
public class AccountBrowsingController extends MultiActionController
    implements ICalendarConstants {
    private static final Log log =
        LogFactory.getLog(AccountBrowsingController.class);

    private ContentService contentService;
    private UserService userService;
    private CosmoSecurityManager securityManager;
    private String calendarView;
    private String eventView;
    private String browseCollectionView;
    private String browseItemView;
    private String browseModificationView;
    private String removeViewBase;
    private String revokeTicketBaseView;
    
    private static final String collectionLocked = "HomeDirectory.Collection.Error.CollectionLocked";
    private static final String concurrencyFailure = "HomeDirectory.Collection.Error.ConcurrencyFailure";    

    /**
     * Retrieves the requested item so that its attributes, properties
     * and children may be browsed. Sets its path into the
     * <code>Path</code> request attribute.
     * <p>
     * If the item is a collection, sets it into the
     * <code>Collection</code> request attribute and returns the
     * {@link #browseCollectionView}. Otherwise, sets it into the
     * <code>Item</code> request attribute and returns the
     * {@link #browseItemView} or {@link #browseModificationView}.
     *
     * @throws ResourceNotFoundException if an item is not found at
     * the given path
     * @throws PermissionException if the logged-in user does not have
     * read permission on the item
     */
    public ModelAndView browse(HttpServletRequest request,
                               HttpServletResponse response)
        throws Exception {
        String path = getPath(request);
        Item item = getItem(path);
        securityManager.checkPermission(item, Permission.READ);

        if (log.isDebugEnabled())
            log.debug("browsing item " + item.getUid() + " at " + path);

        addTitleParam(request, item.getDisplayName());
        request.setAttribute("Path", path);

        if (item instanceof CollectionItem) {
            CollectionItem collection = (CollectionItem) item;
            request.setAttribute("Collection", item);
            
            /* Because this code is collection oriented, we need to 
             * use a little bit of a hack to get a user when
             * we want to display subscriptions */
            
            // If we are browsing a user's root collection, 
            // the path will be only one section long
            String[] pathParts = path.split("/");
            if (pathParts.length == 2){
                request.setAttribute("User", userService.getUser(pathParts[1]));
            }
            
            return new ModelAndView(browseCollectionView);
        }

        request.setAttribute("Item", item);

        if (item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if (note.getModifies() != null)
                return new ModelAndView(browseModificationView);
        }

        return new ModelAndView(browseItemView);
    }

    /**
     * Removes the requested item. Returns the
     * {@link #removeViewBase} with the item path appended.
     *
     * @throws ResourceNotFoundException if an item is not found at
     * the given path
     * @throws PermissionException if the logged-in user does not have
     * write permission on the item
     */
    public ModelAndView remove(HttpServletRequest request,
                               HttpServletResponse response)
        throws Exception {
        String path = getPath(request);
        Item item = getItem(path);
        securityManager.checkPermission(item, Permission.WRITE);

        
        Map<String, Object[]> model = new HashMap<String, Object[]>();
        if (log.isDebugEnabled())
            log.debug("removing item " + item.getUid() + " from " + path);
        try {
            contentService.removeItem(item);
        } catch (CollectionLockedException e){
            model.put("messages", new Object[]{"HomeDirectory.Collection.Error.CollectionLocked"});
            return new ModelAndView("error_general", model);
        } catch (ConcurrencyFailureException e){
            model.put("message", new Object[]{"HomeDirectory.Collection.Error.ConcurrencyFailure"});
            return new ModelAndView("error_general", model);
        }
        String parentPath = PathUtil.getParentPath(path);
        return new ModelAndView(removeViewBase + parentPath);
    }

    /**
     * Removes the requested subscription. Returns the
     * {@link #removeViewBase} with the item path appended.
     *
     * @throws ResourceNotFoundException if an item is not found at
     * the given path
     * @throws PermissionException if the logged-in user does not have
     * write permission on the item
     */
    public ModelAndView removeSubscription(HttpServletRequest request,
                               HttpServletResponse response)
        throws Exception {
        String path = getPath(request);
        String[] pathParts = path.split("/");
        String username = pathParts[1];
        String subName = pathParts[2];
        User user = userService.getUser(username);

        if (log.isDebugEnabled())
            log.debug("removing subscription " + subName + " from " + username + "'s account");

        user.removeSubscription(subName);
        userService.updateUser(user);
        
        String parentPath = PathUtil.getParentPath(path);
        return new ModelAndView(removeViewBase + parentPath);
    }

    /**
     * Retrieves the requested item so that its full contents may be
     * viewed inline in the UI. Sets its path into the
     * <code>Path</code> request attribute.
     * <p>
     * If the item has a calendar collection stamp, sets the item into
     * the <code>Collection</code> request attribute, sets its
     * aggregate calendar content as a <code>CalendarBean</code> into
     * the <code>Calendar</code> request attribute, and returns the
     * {@link #calendarView}.
     * <p>
     * If the item has an event stamp, sets the item into the
     * <code>Item</code> request attribute, sets its calendar content
     * as an <code>EventBean</code> into the <code>Event</code> and
     * returns the  {@link #eventView}.
     * <p>
     *
     * @throws ResourceNotFoundException if an item is not found at
     * the given path
     * @throws PermissionException if the logged-in user does not have
     * read permission on the item
     * @throws ServletException if the item does not have a calendar
     * collection stamp or an event stamp
     */
    public ModelAndView view(HttpServletRequest request,
                             HttpServletResponse response)
        throws Exception {
        String path = getPath(request);
        Item item = getItem(path);
        securityManager.checkPermission(item, Permission.READ);

        if (log.isDebugEnabled())
            log.debug("viewing item " + item.getUid() + " at " + path);

        addTitleParam(request, item.getDisplayName());
        request.setAttribute("Path", path);

        if (item.getStamp(EventStamp.class)!=null) {
            EventStamp eventStamp = StampUtils.getEventStamp(item);
            request.setAttribute("Item", item);
            request.setAttribute("Event", new EventBean(eventStamp));

            return new ModelAndView(eventView);
        } else if (item.getStamp(CalendarCollectionStamp.class)!=null) {
            CalendarCollectionStamp calendar = 
                StampUtils.getCalendarCollectionStamp(item);
            request.setAttribute("Collection", item);
            request.setAttribute("Calendar", new CalendarBean(calendar));

            return new ModelAndView(calendarView);
        }

        throw new ServletException("item of type " + item.getClass().getName()
                + " at " + path + " cannot be viewed");
    }

    /**
     * Retrieves the requested item so that its content may be
     * downloaded.
     * <p>
     * If the item has a calendar collection stamp, sends a
     * <code>text/calendar</code> response containing the aggregate
     * calendar content and returns <code>null</code>.
     * <p>
     * If the item has an event stamp, sends a
     * <code>text/calendar</code> response containing the event
     * content and returns <code>null</code>.
     * <p>
     * If the item is a content item, sends a response according to
     * the item's content type containing the item's content.
     *
     * @throws ResourceNotFoundException if an item is not found at
     * the given path
     * @throws PermissionException if the logged-in user does not have
     * read permission on the item
     * @throws ServletException if the item does not have a calendar
     * collection stamp or an event stamp and is not a content item
     */
    public ModelAndView download(HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        String path = getPath(request);
        Item item = getItem(path);
        securityManager.checkPermission(item, Permission.READ);

        if (log.isDebugEnabled())
            log.debug("downloading item " + item.getUid() + " at " + path);

        if (item.getStamp(EventStamp.class) != null) {
            spoolEvent(StampUtils.getEventStamp(item), request, response);
            return null;
        } else if (item instanceof FileItem) {
            spoolItem((FileItem) item, request, response);
            return null;
        } else if (item.getStamp(CalendarCollectionStamp.class) != null) {
            spoolCalendar(StampUtils.getCalendarCollectionStamp(item), request,
                          response);
            return null;
        }
        throw new ServletException("item of type " + item.getClass().getName()
                + " at " + path + " cannot be downloaded");
    }

    /**
     * Revokes the requested ticket from the requested item. Returns
     * the {@link #revokeTicketBaseView} with the item path appended.
     *
     * @throws ResourceNotFoundException if an item is not found at
     * the given path
     * @throws PermissionException if the logged-in user does not have
     * write permission on the item
     */
    public ModelAndView revokeTicket(HttpServletRequest request,
                                     HttpServletResponse response)
        throws Exception {
        String key = request.getParameter("ticket");
        String path = getPath(request);
        Item item = getItem(path);
        securityManager.checkPermission(item, Permission.WRITE);

        if (log.isDebugEnabled())
            log.debug("revoking ticket " + key + " from item " +
                      item.getUid() + " at " + path);

        contentService.removeTicket(path, key);

        return new ModelAndView(this.revokeTicketBaseView + path);
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService service) {
        this.contentService = service;
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
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

    public void setBrowseModificationView(String browseModificationView) {
        this.browseModificationView = browseModificationView;
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
        String path = request.getParameter("path");
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
    }

    private Item getItem(String path) {
        // download paths look like /collection/<uid>/<filename>
        // other paths look like /foo/bar/baz

        CollectionPath cp = CollectionPath.parse(path, true);
        if (cp != null)
            return contentService.findItemByUid(cp.getUid());

        ItemPath ip = ItemPath.parse(path, true);
        if (ip != null)
            return contentService.findItemByUid(ip.getUid());

        Item item = contentService.findItemByPath(path);
        if (item != null)
            return item;

        throw new NoSuchResourceException(path);
    }

    private void addTitleParam(HttpServletRequest request, String param) {
        List<String> params = (List) request.getAttribute("TitleParam");
        if (params == null) {
            params = new ArrayList<String>();
            request.setAttribute("TitleParam", params);
        }
        params.add(param);
    }

    private void spoolItem(FileItem item,
                           HttpServletRequest request,
                           HttpServletResponse response)
        throws IOException {
        if (log.isDebugEnabled())
            log.debug("spooling item " + item.getUid());

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        if (item.getContentType() != null)
            response.setContentType(item.getContentType());
        else
            response.setContentType("application/octet-stream");
        if (item.getContentLength() != null)
            response.setContentLength(item.getContentLength().intValue());
        if (item.getContentEncoding() != null)
            response.setCharacterEncoding(item.getContentEncoding());
        if (item.getContentLanguage() != null)
            response.setHeader("Content-Language", item.getContentLanguage());
        response.setHeader("Content-Disposition",
                           makeContentDisposition(item));

        // spool data
        if (item.getContentLength() > 0)
            IOUtils.copy(item.getContentInputStream(),
                         response.getOutputStream());
        response.flushBuffer();
    }

    private void spoolEvent(EventStamp stamp,
                            HttpServletRequest request,
                            HttpServletResponse response)
        throws IOException, ParserException, ValidationException {
        if (log.isDebugEnabled())
            log.debug("spooling event " + stamp.getItem().getUid());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(ICALENDAR_MEDIA_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                           makeCalendarContentDisposition(stamp.getItem()));

        // spool data
        CalendarOutputter outputter = new CalendarOutputter();
             
        // since the content was validated when the event item was
        // imported, there's no need to do it here
        outputter.setValidating(false);
        outputter.output(stamp.getCalendar(), response.getOutputStream());
        response.flushBuffer();
    }
    
    private void spoolCalendar(CalendarCollectionStamp calendar,
                               HttpServletRequest request,
                               HttpServletResponse response)
        throws IOException, ParserException, ValidationException {
        if (log.isDebugEnabled())
            log.debug("spooling calendar " + calendar.getItem().getUid());

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(ICALENDAR_MEDIA_TYPE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                           makeCalendarContentDisposition(calendar.getItem()));

        // spool data
        CalendarOutputter outputter = new CalendarOutputter();
        
        // since the content was validated when the event item was
        // imported, there's no need to do it here
        outputter.setValidating(false);
        outputter.output(calendar.getCalendar(), response.getOutputStream());
        response.flushBuffer();
    }

    private String makeContentDisposition(String filename) {
        return "attachment; filename=\"" + filename + "\"";
    }

    private String makeContentDisposition(Item item) {
        return makeContentDisposition(item.getDisplayName());
    }

    private String makeCalendarContentDisposition(Item item) {
        return makeContentDisposition(item.getDisplayName() + "." +
                                      ICALENDAR_FILE_EXTENSION);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
