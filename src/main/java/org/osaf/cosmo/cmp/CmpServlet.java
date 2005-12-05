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
package org.osaf.cosmo.cmp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

import org.springframework.beans.BeansException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet which implements a RESTful HTTP-based protocol for Cosmo
 * management operations.
 *
 * CMP defines the following resources:
 *
 * <dl>
 * <dt><code>Users</code></dt>
 * <dd>A resource representing a collection of user resources</dd>
 * <dt><code>User</code></dt>
 * <dd>A resource representing an individual user</dd>
 * </dl>
 *
 * CMP defines the following admin-only operations:
 *
 * <dl>
 * <dt><code>GET /users</code></dt>
 * <dd>Returns an XML representation of the <code>Users</code> resource collecting all Cosmo users as per {@link UsersResource}.</dd>
 * <dt><code>GET /user/&lgt;username&gt;</code></dt>
 * <dd>Returns an XML representation of a user as per {@link UserResource}.</dd>
 * <dt><code>PUT /user/&lgt;username&gt;</code></dt>
 * <dd>Includes an XML representation of a user as per {@link UserResource}, creating or modifying the user's properties within Cosmo, with all associated side effects including home directory creation.</dd>
 * <dt><code>DELETE /user/&lgt;username&gt;</code></dt>
 * <dd>Causes a user to be removed, with all associated side effects including home directory removal.</dd>
 * </dl>
 *
 * CMP defines the following anonymous (unauthenticated) operations:
 *
 * <dl>
 * <dt><code>PUT /signup</code></dt>
 * <dd>Includes an XML representation of a user, creating a user
 * account and home directory within Cosmo.
 * </dl>
 */
public class CmpServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(CmpServlet.class);

    private static final String BEAN_USER_SERVICE =
        "userService";
    private static final String BEAN_SECURITY_MANAGER =
        "securityManager";

    private WebApplicationContext wac;
    private UserService userService;
    private CosmoSecurityManager securityManager;

    /**
     * Load the servlet context's
     * {@link org.springframework.web.context.WebApplicationContext}
     * and look up support objects.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getRequiredWebApplicationContext(getServletContext());

        userService = (UserService)
            getBean(BEAN_USER_SERVICE, UserService.class);
        securityManager = (CosmoSecurityManager)
            getBean(BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
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

    /**
     * Responds to the following operations:
     *
     * <ul>
     * <li><code>GET /users</code></li>
     * <li><code>GET /user/&lgt;username&gt;</code></li>
     * </ul>
     */
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (req.getPathInfo().equals("/users")) {
            Set users = userService.getUsers();
            resp.setStatus(HttpServletResponse.SC_OK);
            sendXmlResponse(resp, new UsersResource(users, getUrlBase(req)));
            return;
        }
        String username = usernameFromPathInfo(req.getPathInfo());
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        User user = null;
        try {
            user = userService.getUser(username);
        } catch (DataRetrievalFailureException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        sendXmlResponse(resp, new UserResource(user, getUrlBase(req)));
    }

    /**
     * Responds to the following operations:
     *
     * <ul>
     * <li><code>PUT /user/&lgt;username&gt;</code></li>
     * <li><code>PUT /signup</code></li>
     * </ul>
     */
    protected void doPut(HttpServletRequest req,
                         HttpServletResponse resp)
        throws ServletException, IOException {
        if (! checkPutPreconditions(req, resp)) {
            return;
        }

        Document xmldoc = null;
        try {
            xmldoc = readXmlRequest(req);
        } catch (JDOMException e) {
            log.error("Error parsing request body: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (req.getPathInfo().equals("/signup")) {
            processSignup(req, resp, xmldoc);
            return;
        }

        String urlUsername = usernameFromPathInfo(req.getPathInfo());
        if (urlUsername == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            User user = userService.getUser(urlUsername);
            processUserUpdate(req, resp, user, xmldoc);
        } catch (DataRetrievalFailureException e) {
            processUserCreate(req, resp, xmldoc);
        }
    }

    // our methods

    /**
     * Enforces preconditions on all PUT requests, including content
     * length and content type checks. Returns <code>true</code> if
     * all preconditions are met, otherwise sets the appropriate
     * error response code and returns <code>false</code>.
     */
    protected boolean checkPutPreconditions(HttpServletRequest req,
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

    /**
     * Delegated to by {@link #doPut} to handle signup
     * requests, creating the user account and setting the response
     * status and headers.
     */
    protected void processSignup(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 Document xmldoc)
        throws ServletException, IOException {
        try {
            UserResource resource = new UserResource(getUrlBase(req), xmldoc);
            userService.createUser(resource.getUser());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Content-Location", resource.getHomedirUrl()); 
        } catch (DuplicateUsernameException e) {
            log.error("Chosen username is already in use");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (DuplicateEmailException e) {
            log.error("Chosen email is already in use");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (CmpException e) {
            log.error("Error validating request body: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Delegated to by {@link #doPut} to handle account creation
     * requests, creating the user account and setting the response
     * status and headers.
     */
    protected void processUserCreate(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     Document xmldoc)
        throws ServletException, IOException {
        try {
             UserResource resource = new UserResource(getUrlBase(req), xmldoc);
             User user = resource.getUser();
             if (! user.getUsername().
                 equals(usernameFromPathInfo(req.getPathInfo()))) {
                 log.error("Username does not match request URI");
                 resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 return;
             }
             userService.createUser(user);
             resp.setStatus(HttpServletResponse.SC_CREATED);
         } catch (DuplicateUsernameException e) {
             log.error("Chosen username is already in use");
             resp.setStatus(HttpServletResponse.SC_CONFLICT);
         } catch (DuplicateEmailException e) {
             log.error("Chosen email is already in use");
             resp.setStatus(HttpServletResponse.SC_CONFLICT);
         } catch (CmpException e) {
             log.error("Error validating request body: " + e.getMessage());
             resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         } catch (ModelValidationException e) {
             log.error("Error validating user: " + e.getMessage());
             resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Delegated to by {@link #doPut} to handle account update
     * requests, saving the modified account and setting the response
     * status and headers.
     */
    protected void processUserUpdate(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     User user,
                                     Document xmldoc)
        throws ServletException, IOException {
        try {
            UserResource resource =
                new UserResource(user, getUrlBase(req), xmldoc);
            userService.updateUser(user);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            if (! user.getUsername().
                equals(usernameFromPathInfo(req.getPathInfo()))) {
                resp.setHeader("Content-Location", resource.getUserUrl());
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }
        } catch (DuplicateUsernameException e) {
            log.error("Chosen username is already in use");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (DuplicateEmailException e) {
            log.error("Chosen email is already in use");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (CmpException e) {
            log.error("Error validating request body: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (ModelValidationException e) {
            log.error("Error validating user: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Looks up the bean with given name and class in the web
     * application context.
     *
     * @param name the bean's name
     * @param clazz the bean's class
     */
    protected Object getBean(String name, Class clazz)
        throws ServletException {
        try {
            return wac.getBean(name, clazz);
        } catch (BeansException e) {
            throw new ServletException("Error retrieving bean " + name +
                                       " of type " + clazz +
                                       " from web application context", e);
        }
    }

    /**
     */
    public WebApplicationContext getWebApplicationContext() {
        return wac;
    }

    // private methods

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
        throws JDOMException, IOException {
        InputStream in = req.getInputStream();
        if (in == null) {
            return null;
        }
        SAXBuilder builder = new SAXBuilder(false);
        return builder.build(in);
    }

    private void sendXmlResponse(HttpServletResponse resp,
                                 CmpResource resource)
        throws IOException {
        // pretty format is easier for CMP scripters to read
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        // write xml into a byte array so we can calculate length
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        outputter.output(resource.toXml(), buf);
        byte[] bytes = buf.toByteArray();
        resp.setContentType("text/xml");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }

    // like response.encodeUrl() except does not include servlet path
    // or session id
    private String getUrlBase(HttpServletRequest req) {
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
