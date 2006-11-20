/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.mc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.util.RepositoryUriParser;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Implements the Morse Code synchronization protocol for Cosmo.
 *
 * The primary methods in this class are "handler methods" that each
 * service a single HTTP method (GET, PUT, POST, DELETE). Each handler
 * method verifies that the servlet-relative portion of the request
 * URI matches the form <code>/collection/<uid></code> and then calls
 * the appropriate <code>MorseCodeController</code> method.
 *
 * @see MorseCodeController
 *
 * See
 * http://wiki.osafoundation.org/bin/view/Projects/CosmoMorseCode
 * for the protocol specification.
 */
public class MorseCodeServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(MorseCodeServlet.class);

    private static final String BEAN_CONTROLLER =
        "morseCodeController";

    /**
     * The name of the request parameter that provides the
     * synchronization token for synchronize requests:
     * <code>token</code>.
     */
    public static final String PARAM_SYNC_TOKEN = "token";
    /**
     * The name of the request parameter that provides the
     * (optional) parent uid for publish requests:
     * <code>parent</code>.
     */
    public static final String PARAM_PARENT_UID = "parent";
    /**
     * The name of the response header that contains the new
     * synchronization token for publish, update, subscribe and
     * synchronize requests: <code>X-MorseCode-SyncToken</code>.
     */
    public static final String HEADER_SYNC_TOKEN = "X-MorseCode-SyncToken";
    /**
     * The response status code indicating that a collection is locked
     * for updates: <code>423</code>.
     */
    public static final int SC_LOCKED = 423;

    private WebApplicationContext wac;
    private MorseCodeController controller;

    // HttpServlet methods

    /**
     * Handles delete requests.
     */
    protected void doDelete(HttpServletRequest req,
                            HttpServletResponse resp)
        throws ServletException, IOException {
        if (log.isDebugEnabled())
            log.debug("handling DELETE for " + req.getPathInfo());

        String uid =
            new RepositoryUriParser(req.getPathInfo()).getCollectionUid();
        if (uid != null) {
            try {
                controller.deleteCollection(uid);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            } catch (UnknownCollectionException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                               "Unknown collection");
                return;
            } catch (NotCollectionException e) {
                resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,
                               "Item not a collection");
            } catch (MorseCodeException e) {
                log.error("Error deleting collection " + uid, e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Error deleting collection: " + e.getMessage());
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Handles subscribe and synchronize requests. If the
     * {@link PARAM_SYNC_TOKEN} request parameter provides a
     * synchronization token, the request is processed as a
     * synchronization; otherwise it is processed as a subscription.
     */
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (log.isDebugEnabled())
            log.debug("handling GET for " + req.getPathInfo());

        String uid =
            new RepositoryUriParser(req.getPathInfo()).getCollectionUid();
        if (uid != null) {
            String tokenStr = req.getHeader(HEADER_SYNC_TOKEN);
            if (StringUtils.isBlank(tokenStr))
                tokenStr = req.getParameter(PARAM_SYNC_TOKEN);
            if (StringUtils.isBlank(tokenStr))
                tokenStr = null;
            try {
                SyncToken token = tokenStr != null ?
                    SyncToken.deserialize(tokenStr) :
                    null;
                ItemStateSet states = token == null ?
                    controller.subscribeToCollection(uid) :
                    controller.synchronizeCollection(uid, token);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.addHeader(HEADER_SYNC_TOKEN,
                               states.getToken().serialize());
                // XXX: convert item states to eimml
                // XXX write eimml content
                return;
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Invalid sync token");
                return;
            } catch (UnknownCollectionException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                               "Unknown collection");
                return;
            } catch (NotCollectionException e) {
                resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,
                               "Item not a collection");
            } catch (MorseCodeException e) {
                String msg = tokenStr == null ?
                    "Error subscribing to collection" :
                    "Error synchronizing collection";
                log.error(msg + " " + uid, e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               msg + ": " + e.getMessage());
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Handles update requests.
     */
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
        throws ServletException, IOException {
        if (log.isDebugEnabled())
            log.debug("handling POST for " + req.getPathInfo());

        String uid =
            new RepositoryUriParser(req.getPathInfo()).getCollectionUid();
        if (uid != null) {
            String tokenStr = req.getHeader(HEADER_SYNC_TOKEN);
            if (StringUtils.isBlank(tokenStr)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Missing sync token");
                return;
            }
            try {
                SyncToken token = SyncToken.deserialize(tokenStr);
                // XXX: check update preconditions
                // XXX: read eimml content
                // XXX: convert eimml to model
                Set<ItemState> items = new HashSet<ItemState>();
                SyncToken newToken =
                    controller.updateCollection(uid, token, items);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                resp.addHeader(HEADER_SYNC_TOKEN, newToken.serialize());
                return;
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Invalid sync token");
                return;
            } catch (UnknownCollectionException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                               "Unknown collection");
                return;
            } catch (NotCollectionException e) {
                resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,
                               "Item not a collection");
            } catch (CollectionLockedException e) {
                resp.sendError(SC_LOCKED, "Collection is locked for update");
                return;
            } catch (StaleCollectionException e) {
                resp.sendError(HttpServletResponse.SC_RESET_CONTENT,
                               "Collection contains more recently updated items");
                return;
            } catch (MorseCodeException e) {
                log.error("Error updating collection " + uid, e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Error updating collection: " + e.getMessage());
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Handles publish requests.
     */
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (log.isDebugEnabled())
            log.debug("handling PUT for " + req.getPathInfo());

        String uid =
            new RepositoryUriParser(req.getPathInfo()).getCollectionUid();
        if (uid != null) {
            String parentUid = req.getParameter(PARAM_PARENT_UID);
            if (StringUtils.isEmpty(parentUid))
                parentUid = null;
            try {
                // XXX: check publish preconditions
                // XXX: read eimml content
                // XXX: convert eimml to item states
                Set<ItemState> items = new HashSet<ItemState>();
                SyncToken newToken =
                    controller.publishCollection(uid, parentUid, items);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.addHeader(HEADER_SYNC_TOKEN, newToken.serialize());
                return;
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Parent uid must be specified when authenticated principal is not a user");
                return;
            } catch (UidInUseException e) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Uid in use");
                return;
            } catch (UnknownCollectionException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                               "Unknown parent collection");
                return;
            } catch (NotCollectionException e) {
                resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,
                               "Parent item not a collection");
                return;
            } catch (MorseCodeException e) {
                log.error("Error publishing collection " + uid, e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Error publishing collection: " +
                               e.getMessage());
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
            if (controller == null)
                controller = (MorseCodeController)
                    getBean(BEAN_CONTROLLER, MorseCodeController.class);
        }
        
        if (controller == null)
            throw new ServletException("content service must not be null");
    }

    // our methods

    /**
     */
    public MorseCodeController getController() {
        return controller;
    }

    /**
     */
    public void setController(MorseCodeController controller) {
        this.controller = controller;
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
