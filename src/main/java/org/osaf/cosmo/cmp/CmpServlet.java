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
package org.osaf.cosmo.cmp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.status.StatusSnapshot;

import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

/**
 * Implements RESTful HTTP-based protocol for Cosmo management
 * operations.
 *
 * See
 * http://wiki.osafoundation.org/bin/view/Projects/CosmoManagementProtocol
 * for the protocol specification.
 */
public class CmpServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(CmpServlet.class);
    private static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    private static final String BEAN_USER_SERVICE =
        "userService";
    private static final String BEAN_SECURITY_MANAGER =
        "securityManager";

    private WebApplicationContext wac;
    private UserService userService;
    private CosmoSecurityManager securityManager;

    /**
     * Loads the servlet context's <code>WebApplicationContext</code>
     * and wires up dependencies. If no
     * <code>WebApplicationContext</code> is found, dependencies must
     * be set manually (useful for testing).
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (userService == null) {
            userService = (UserService)
                getBean(BEAN_USER_SERVICE, UserService.class);
        }
        if (securityManager == null) {
            securityManager = (CosmoSecurityManager)
                getBean(BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
        }
    }

    // HttpServlet methods

    /**
     * Responds to the following operations:
     *
     * <ul>
     * <li><code>DELETE /user/&lgt;username&gt;</code></li>
     * </ul>
     */
    protected void doDelete(HttpServletRequest req,
                            HttpServletResponse resp)
        throws ServletException, IOException {
        if (req.getPathInfo().startsWith("/user/")) {
            processUserDelete(req, resp);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Responds to the following operations:
     *
     * <ul>
     * <li><code>GET /account</code></li>
     * <li><code>GET /users</code></li>
     * <li><code>GET /user/&lgt;username&gt;</code></li>
     * </ul>
     */
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (req.getPathInfo().equals("/account")) {
            processAccountGet(req, resp);
            return;
        }
        if (req.getPathInfo().equals("/users")) {
            processUsersGet(req, resp);
            return;
        }
        if (req.getPathInfo().startsWith("/user/")) {
            processUserGet(req, resp);
            return;
        }
        if (req.getPathInfo().equals("/server/status")) {
            processServerStatus(req, resp);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * Responds to the following operations:
     *
     * <ul>
     * <li><code>POST /server/gc</li>
     * </ul>
     *
     * Delegates all other operations to
     * {@link #doPut(HttpServletRequest, HttpServletResponse)}.
     */
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
        throws ServletException, IOException {
        if (req.getPathInfo().equals("/server/gc")) {
            processServerGc(req, resp);
            return;
        }
        doPut(req, resp);
    }

    /**
     * Responds to the following operations:
     *
     * <ul>
     * <li><code>PUT /signup</code></li>
     * <li><code>PUT /account</code></li>
     * <li><code>PUT /user/&lgt;username&gt;</code></li>
     * </ul>
     */
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (! checkPutPreconditions(req, resp)) {
            return;
        }

        if (req.getPathInfo().equals("/signup")) {
            processSignup(req, resp);
            return;
        }
        if (req.getPathInfo().equals("/account")) {
            processAccountUpdate(req, resp);
            return;
        }
        if (req.getPathInfo().startsWith("/user/")) {
            String urlUsername = usernameFromPathInfo(req.getPathInfo());
            if (urlUsername == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            User user = userService.getUser(urlUsername);
            if (user != null)
                processUserUpdate(req, resp, user);
            else
                processUserCreate(req, resp);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    // our methods

    /**
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    // private methods

    /* Enforces preconditions on all PUT requests, including content
     * length and content type checks. Returns <code>true</code> if
     * all preconditions are met, otherwise sets the appropriate
     * error response code and returns <code>false</code>.
     */
    private boolean checkPutPreconditions(HttpServletRequest req,
                                          HttpServletResponse resp) {
        if (req.getContentLength() <= 0) {
            resp.setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
            return false;
        }
        if (req.getContentType() == null ||
            ! req.getContentType().startsWith("text/xml")) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return false;
        }
        if (req.getHeader("Content-Transfer-Encoding") != null ||
            req.getHeader("Content-Encoding") != null ||
            req.getHeader("Content-Base") != null ||
            req.getHeader("Content-Location") != null ||
            req.getHeader("Content-MD5") != null ||
            req.getHeader("Content-Range") != null) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return false;
        }
        return true;
    }

    /*
     * Delegated to by {@link #doDelete} to handle user DELETE
     * requests, removing the user and setting the response status and
     * headers.
     */
    private void processUserDelete(HttpServletRequest req,
                                   HttpServletResponse resp)
        throws ServletException, IOException {
        String username = usernameFromPathInfo(req.getPathInfo());
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (username.equals(User.USERNAME_OVERLORD)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        userService.removeUser(username);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /*
     * Delegated to by {@link #doGet} to handle account GET
     * requests, retrieving the account for the currently logged in
     * user, setting the response status and headers, and writing the
     * response content.
     */
    private void processAccountGet(HttpServletRequest req,
                                   HttpServletResponse resp)
        throws ServletException, IOException {
        User user = getLoggedInUser();
        UserResource resource = new UserResource(user, getUrlBase(req));
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("ETag", resource.getEntityTag());
        sendXmlResponse(resp, resource);
    }

    /*
     * Delegated to by {@link #doGet} to handle users GET
     * requests, retrieving all user accounts, setting the response
     * status and headers, and writing the response content.
     */
    private void processUsersGet(HttpServletRequest req,
                                 HttpServletResponse resp)
        throws ServletException, IOException {
        Set users = userService.getUsers();
        resp.setStatus(HttpServletResponse.SC_OK);
        sendXmlResponse(resp, new UsersResource(users, getUrlBase(req)));
    }

    /*
     * Delegated to by {@link #doGet} to handle user GET
     * requests, retrieving the user account, setting the response
     * status and headers, and writing the response content.
     */
    private void processUserGet(HttpServletRequest req,
                                HttpServletResponse resp)
        throws ServletException, IOException {
        String username = usernameFromPathInfo(req.getPathInfo());
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        User user = userService.getUser(username);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UserResource resource = new UserResource(user, getUrlBase(req));
        resp.setHeader("ETag", resource.getEntityTag());
        sendXmlResponse(resp, resource);
    }

    /*
     * Delegated to by {@link #doGet} to handle server status GET
     * requests, taking a status snapshot, setting the response
     * status and headers, and writing the response content.
     */
    private void processServerStatus(HttpServletRequest req,
                                     HttpServletResponse resp)
        throws ServletException, IOException {
        byte[] snap = new StatusSnapshot().toBytes();
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(snap.length);
        resp.getOutputStream().write(snap);
    }

    /*
     * Delegated to by {@link #doPost} to handle server gc POST
     * requests, initiating garbage collection, and setting the
     * response status.
     */
    private void processServerGc(HttpServletRequest req,
                                 HttpServletResponse resp)
        throws ServletException, IOException {
        System.gc();
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /*
     * Delegated to by {@link #doPut} to handle signup
     * requests, creating the user account and setting the response
     * status and headers.
     */
    private void processSignup(HttpServletRequest req,
                               HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            Document xmldoc = readXmlRequest(req);
            UserResource resource = new UserResource(getUrlBase(req), xmldoc);
            userService.createUser(resource.getUser());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Content-Location", resource.getHomedirUrl()); 
            resp.setHeader("ETag", resource.getEntityTag());
        } catch (SAXException e) {
            log.warn("error parsing request body: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Error parsing request body: " + e.getMessage());
            return;
        } catch (CmpException e) {
            log.warn("bad request for signup: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           e.getMessage());
        } catch (ModelValidationException e) {
            handleModelValidationError(resp, e);
        }
    }

    /*
     * Delegated to by {@link #doPut} to handle account update
     * requests for the currently logged in user, saving the modified
     * account and setting the response status and headers.
     */
    private void processAccountUpdate(HttpServletRequest req,
                                      HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            Document xmldoc = readXmlRequest(req);
            String urlUsername = usernameFromPathInfo(req.getPathInfo());
            User user = getLoggedInUser();
            String oldUsername = user.getUsername();
            UserResource resource =
                new UserResource(user, getUrlBase(req), xmldoc);
            if (user.isUsernameChanged()) {
                // reset logged in user's username
                user.setUsername(oldUsername);
                log.warn("bad request for account update: " +
                         "username may not be changed");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Username may not be changed");
                return;
            }
            userService.updateUser(user);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.setHeader("ETag", resource.getEntityTag());
        } catch (SAXException e) {
            log.warn("error parsing request body: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Error parsing request body: " + e.getMessage());
            return;
        } catch (CmpException e) {
            log.warn("bad request for account update: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           e.getMessage());
        } catch (ModelValidationException e) {
            handleModelValidationError(resp, e);
        }
    }

    /*
     * Delegated to by {@link #doPut} to handle account creation
     * requests, creating the user account and setting the response
     * status and headers.
     */
    private void processUserCreate(HttpServletRequest req,
                                   HttpServletResponse resp)
        throws ServletException, IOException {
        try {
            Document xmldoc = readXmlRequest(req);
            String urlUsername = usernameFromPathInfo(req.getPathInfo());
            UserResource resource = new UserResource(getUrlBase(req), xmldoc);
            User user = resource.getUser();
            if (user.getUsername() != null &&
                ! user.getUsername().equals(urlUsername)) {
                log.warn("bad request for user create: " +
                         "username does not match request URI");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Username does not match request URI");
                return;
            }
            userService.createUser(user);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("ETag", resource.getEntityTag());
        } catch (SAXException e) {
            log.warn("error parsing request body: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Error parsing request body: " + e.getMessage());
            return;
        } catch (CmpException e) {
            log.warn("bad request for user create: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           e.getMessage());
        } catch (ModelValidationException e) {
            handleModelValidationError(resp, e);
        }
    }

    /*
     * Delegated to by {@link #doPut} to handle account update
     * requests, saving the modified account and setting the response
     * status and headers.
     */
    private void processUserUpdate(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   User user)
        throws ServletException, IOException {
        try {
            Document xmldoc = readXmlRequest(req);
            String urlUsername = usernameFromPathInfo(req.getPathInfo());
            UserResource resource =
                new UserResource(user, getUrlBase(req), xmldoc);
            userService.updateUser(user);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.setHeader("ETag", resource.getEntityTag());
            if (! user.getUsername().equals(urlUsername)) {
                resp.setHeader("Content-Location", resource.getUserUrl());
            }
        } catch (SAXException e) {
            log.warn("error parsing request body: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Error parsing request body: " + e.getMessage());
            return;
        } catch (CmpException e) {
            log.warn("bad request for user update: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           e.getMessage());
        } catch (ModelValidationException e) {
            handleModelValidationError(resp, e);
        }
    }

    private void handleModelValidationError(HttpServletResponse resp,
                                            ModelValidationException e)
        throws IOException {
        if (e instanceof DuplicateUsernameException) {
            sendApiError(resp, CmpConstants.SC_USERNAME_IN_USE);
            return;
        }
        if (e instanceof DuplicateEmailException) {
            sendApiError(resp, CmpConstants.SC_EMAIL_IN_USE);
            return;
        }
        log.warn("model validation error: " + e.getMessage());
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                       e.getMessage());
    }

    private void sendApiError(HttpServletResponse resp,
                              int errorCode)
        throws IOException {
        resp.sendError(errorCode, CmpConstants.getReasonPhrase(errorCode));
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

    private User getLoggedInUser() {
        return securityManager.getSecurityContext().getUser();
    }

    private String usernameFromPathInfo(String pathInfo) {
        if (pathInfo.startsWith("/user/")) {
            String username = pathInfo.substring(6);
            if (! (username.equals("") ||
                   username.indexOf("/") >= 0)) {
                return username;
            }
        }
        return null;
    }

    private Document readXmlRequest(HttpServletRequest req)
        throws SAXException, IOException {
        if (req.getContentLength() == 0) {
            return null;
        }
        InputStream in = req.getInputStream();
        if (in == null) {
            return null;
        }

        // check to see if there's any data to read
        PushbackInputStream filtered =
            new PushbackInputStream(in, 1);
        int read = filtered.read();
        if (read == -1) {
            return null;
        }
        filtered.unread(read);

        // there is data, so read the stream
        try {
            BUILDER_FACTORY.setNamespaceAware(true);
            DocumentBuilder docBuilder = BUILDER_FACTORY.newDocumentBuilder();
            return docBuilder.parse(filtered);
        } catch (ParserConfigurationException e) {
            throw new CmpException("error configuring xml builder", e);
        }
    }

    private void sendXmlResponse(HttpServletResponse resp,
                                 CmpResource resource)
        throws ServletException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
 
        try {
            Document doc =
                BUILDER_FACTORY.newDocumentBuilder().newDocument();
            doc.appendChild(resource.toXml(doc));
            OutputFormat format = new OutputFormat("xml", "UTF-8", true);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.setNamespaces(true);
            serializer.asDOMSerializer().serialize(doc);
        } catch (ParserConfigurationException e) {
            throw new CmpException("error configuring xml builder", e);
        }
 
        byte[] bytes = out.toByteArray();
        resp.setContentType("text/xml");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }

    private String getUrlBase(HttpServletRequest req) {
        // like response.encodeUrl() except does not include servlet
        // path or session id
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
        return buf.toString();
    }
}
