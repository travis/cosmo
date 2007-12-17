/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.hibernate.validator.InvalidStateException;

import org.osaf.cosmo.model.DuplicateEmailException;
import org.osaf.cosmo.model.DuplicateUsernameException;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.PasswordRecovery;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.OverlordDeletionException;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.ServiceListener;
import org.osaf.cosmo.service.account.AccountActivator;
import org.osaf.cosmo.service.account.ActivationContext;
import org.osaf.cosmo.service.account.ActivationListener;
import org.osaf.cosmo.service.account.OutOfTheBoxContext;
import org.osaf.cosmo.service.account.OutOfTheBoxHelper;
import org.osaf.cosmo.service.account.OutOfTheBoxListener;
import org.osaf.cosmo.service.account.PasswordRecoveryMessageContext;
import org.osaf.cosmo.service.account.PasswordRecoverer;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.server.SpaceUsageReport;
import org.osaf.cosmo.server.StatusSnapshot;
import org.osaf.cosmo.server.UserPath;
import org.osaf.cosmo.util.PageCriteria;

import org.springframework.beans.BeansException;
import org.springframework.dao.DataRetrievalFailureException;
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

    private static final Pattern PATTERN_SPACE_USAGE =
        Pattern.compile("^/server/usage/space(/[^/]+)?(/xml)?$");
    
    private static final String URL_PASSWORD_RESET = "/account/password/reset/";
    private static final String URL_ACTIVATE = "/activate/";

    private static final String BEAN_CONTENT_SERVICE = "contentService";
    private static final String BEAN_USER_SERVICE = "userService";
    private static final String BEAN_PASSWORD_RECOVERER = "passwordRecoverer";
    private static final String BEAN_SERVICE_LOCATOR_FACTORY =
        "serviceLocatorFactory";
    private static final String BEAN_SECURITY_MANAGER = "securityManager";
    private static final String BEAN_ACCOUNT_ACTIVATOR = "accountActivator";
    private static final String BEAN_OOTB_HELPER = "ootbHelper";
    private static final String BEAN_ENTITY_FACTORY = "cosmoEntityFactory";

    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = PageCriteria.VIEW_ALL;
    private static final boolean DEFAULT_SORT_ASCENDING = true;
    private static final User.SortType DEFAULT_SORT_TYPE = 
        User.SortType.USERNAME;

    private WebApplicationContext wac;
    private ContentService contentService;
    private UserService userService;
    private EntityFactory entityFactory;
    private ServiceLocatorFactory serviceLocatorFactory;
    private CosmoSecurityManager securityManager;
    private PasswordRecoverer passwordRecoverer;
    private AccountActivator accountActivator;
    private OutOfTheBoxHelper ootbHelper;

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

        if (wac != null) {
            if (contentService == null)
                contentService = (ContentService)
                    getBean(BEAN_CONTENT_SERVICE, ContentService.class);
            if (userService == null)
                userService = (UserService)
                    getBean(BEAN_USER_SERVICE, UserService.class);
            if (securityManager == null)
                securityManager = (CosmoSecurityManager)
                    getBean(BEAN_SECURITY_MANAGER, CosmoSecurityManager.class);
            if (serviceLocatorFactory == null)
                serviceLocatorFactory = (ServiceLocatorFactory)
                    getBean(BEAN_SERVICE_LOCATOR_FACTORY,
                            ServiceLocatorFactory.class);
            if (accountActivator == null)
                accountActivator = (AccountActivator)
                    getBean(BEAN_ACCOUNT_ACTIVATOR, AccountActivator.class);
            if (passwordRecoverer == null)
                passwordRecoverer = (PasswordRecoverer)
                    getBean(BEAN_PASSWORD_RECOVERER, PasswordRecoverer.class);
            if (ootbHelper == null)
                ootbHelper = (OutOfTheBoxHelper)
                    getBean(BEAN_OOTB_HELPER, OutOfTheBoxHelper.class);
            if (entityFactory == null)
                entityFactory = (EntityFactory)
                    getBean(BEAN_ENTITY_FACTORY, EntityFactory.class);
        }

        if (contentService == null)
            throw new ServletException("content service must not be null");
        if (userService == null)
            throw new ServletException("user service must not be null");
        if (securityManager == null)
            throw new ServletException("security manager must not be null");
        if (accountActivator == null)
            throw new ServletException("account activator must not be null");
        if (passwordRecoverer == null)
            throw new ServletException("password recoverer must not be null");
        if (ootbHelper == null)
            throw new ServletException("ootb helper must not be null");
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
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
        Matcher m = null;

        // /account
        if (req.getPathInfo().equals("/account")) {
            processAccountGet(req, resp);
            return;
        }

        // /users
        if (req.getPathInfo().equals("/users")) {
            processUsersGet(req, resp);
            return;
        }
        if (req.getPathInfo().equals("/users/count")){
            processGetUserCount(req, resp);
            return;
        }

        // /user
        UserPath up = UserPath.parse(req.getPathInfo(), true);
        if (up != null) {
            User user = userService.getUser(up.getUsername());
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (up.getPathInfo() != null) {
                // /user/<username>/something-else
                if (! up.getPathInfo().equals("/service")) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                // /user/<username>/service
                if (! (securityManager.getSecurityContext().isAdmin() ||
                       securityManager.getSecurityContext().getUser().equals(user))) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                ServiceLocator locator =
                    serviceLocatorFactory.createServiceLocator(req);
                UserServiceResource resource =
                    new UserServiceResource(user, locator);
                sendXmlResponse(resp, resource);
                return;
            }

            // /user/<username>
            UserResource resource = new UserResource(user, getUrlBase(req));
            resp.setHeader("ETag", resource.getEntityTag());
            sendXmlResponse(resp, resource);
            return;
        }

        // /server/status
        if (req.getPathInfo().equals("/server/status")) {
            processServerStatus(req, resp);
            return;
        }

        m = PATTERN_SPACE_USAGE.matcher(req.getPathInfo());
        if (m.matches()) {
            String username = null;
            boolean isXml = false;
            boolean selected = false;

            if (m.group(1) != null && m.group(2) != null) {
                if (m.group(2).equals("/xml")) {
                    // xml single user report
                    username = m.group(1).substring(1);
                    isXml = true;
                    selected = true;
                }
            } else if (m.group(1) != null) {
                if (m.group(1).equals("/xml")) {
                    // xml aggregate report
                    username = null;
                    isXml = true;
                    selected = true;
                } else if (m.group(1).length() > 1) {
                    // plaintext username report
                    username = m.group(1).substring(1);
                    isXml = false;
                    selected = true;
                }
            } else {
                // plaintext aggregate report
                username = null;
                isXml = false;
                selected = true;
            }

            if (selected) {
                processSpaceUsage(req, resp, username, isXml);
                return;
            }
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
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

        if (req.getPathInfo().startsWith(URL_ACTIVATE)){
            processActivateUser(req, resp);
            return;
        }

        if (req.getPathInfo().startsWith("/user/delete")){
            processMultiUserDelete(req, resp);
            return;
        }
        
        if (req.getPathInfo().startsWith("/account/password/recover")){
            processRecoverPassword(req, resp);
            return;
        }

        if (req.getPathInfo().startsWith("/account/activation/send")){
            processSendActivation(req, resp);
            return;
        }

        if (req.getPathInfo().startsWith(URL_PASSWORD_RESET)){
            processResetPassword(req, resp);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // our methods

    /**
     */
    public ContentService getContentService() {
        return contentService;
    }

    /**
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

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

    /**
     */
    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    /**
     */
    public void setServiceLocatorFactory(ServiceLocatorFactory factory) {
        this.serviceLocatorFactory = factory;
    }

    public AccountActivator getAccountActivator() {
        return accountActivator;
    }

    public void setAccountActivator(AccountActivator activator) {
        this.accountActivator = activator;
    }
    
    public PasswordRecoverer getPasswordRecoverer() {
        return passwordRecoverer;
    }

    public void setPasswordRecoverer(PasswordRecoverer passwordRecoverer) {
        this.passwordRecoverer = passwordRecoverer;
    }

    public OutOfTheBoxHelper getOutOfTheBoxHelper() {
        return ootbHelper;
    }

    public void setOutOfTheBoxHelper(OutOfTheBoxHelper helper) {
        ootbHelper = helper;
    }
    
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
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
     * requests, removing the user and setting the response status and headers.
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

    /* Enforces preconditions on MultiUserDeletion. Returns
     * <code>true</code> if all preconditions are met, otherwise
     * sets the appropriate error response code and returns
     * <code>false</code>.
     */
    private boolean checkMultiUserDeletePreconditions(HttpServletRequest req,
            HttpServletResponse resp) {

        if (req.getContentType() == null ||
                ! req.getContentType().startsWith(
                        "application/x-www-form-urlencoded")) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return false;
        }

        return true;
    }

    /*
     * Delegated to by {@link #doDelete} to handle
     * POST /user/delete requests. These request MUST have a message
     * body containing the names of the users to be deleted in standard
     * url-encoded form input syntax, ie,
     *
     * user=alice&user=bob&user=carlton
     */
    private void processMultiUserDelete(HttpServletRequest req,
            HttpServletResponse resp) {
        if (checkMultiUserDeletePreconditions(req, resp)){
            Set<String> names = new HashSet<String>();

            for (String name : req.getParameterValues("user")){
                names.add(name);
            }

            try {
                userService.removeUsersByName(names);
            } catch (OverlordDeletionException e) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
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
     * Turn a paging query string into a PageCriteria object.
     */
    private PageCriteria<User.SortType>
        buildPageCriteria(HttpServletRequest req){

        PageCriteria<User.SortType> pageCriteria =
            new PageCriteria<User.SortType>(
                    DEFAULT_PAGE_NUMBER,
                    DEFAULT_PAGE_SIZE,
                    DEFAULT_SORT_ASCENDING,
                    DEFAULT_SORT_TYPE);

        Map<String, String[]> pagingParameterMap = req.getParameterMap();

        try {
            if (pagingParameterMap.containsKey(
                    PageCriteria.PAGE_SIZE_URL_KEY)) {
                
                int pageSize = Integer.parseInt(
                        pagingParameterMap.get(
                                PageCriteria.PAGE_SIZE_URL_KEY)[0]);
                                
                if (pageSize == 0){
                    throw new CmpException("Page size cannot be 0.");
                }
                pageCriteria.setPageSize(pageSize);
            }
        } catch (NumberFormatException e){
            throw new CmpException(
                    pagingParameterMap.get(PageCriteria.PAGE_SIZE_URL_KEY)[0] +
                    " is not a valid page size.");
        }

        try {
            if (pagingParameterMap.containsKey(
                    PageCriteria.PAGE_NUMBER_URL_KEY)
            ) {
                pageCriteria.setPageNumber(
                        Integer.parseInt(pagingParameterMap.get(
                                PageCriteria.PAGE_NUMBER_URL_KEY)[0]));
            }
        } catch (NumberFormatException e){
            throw new CmpException(pagingParameterMap.get(
                    PageCriteria.PAGE_NUMBER_URL_KEY)[0] +
                    " is not a valid page number.");
        }

        if (pagingParameterMap.containsKey(PageCriteria.SORT_ORDER_URL_KEY)) {
            String sortOrderParameter =
                pagingParameterMap.get(PageCriteria.SORT_ORDER_URL_KEY)[0];

            if (sortOrderParameter.equals(PageCriteria.ASCENDING_STRING)){
                pageCriteria.setSortAscending(true);
            } else if (
                    sortOrderParameter.equals(PageCriteria.DESCENDING_STRING)
            ){
                pageCriteria.setSortAscending(false);
            } else{
                throw new CmpException(
                        "Sort order " +
                        sortOrderParameter +
                        " not valid.");
            }
        }

        if (pagingParameterMap.containsKey(PageCriteria.SORT_TYPE_URL_KEY)) {

            User.SortType sortType = User.SortType.getByUrlString(
                    pagingParameterMap.get(PageCriteria.SORT_TYPE_URL_KEY)[0]);

            if (sortType != null){
                pageCriteria.setSortType(sortType);
            } else {
                throw new CmpException(
                        "Sort type " +
                        pagingParameterMap.get(
                                PageCriteria.SORT_TYPE_URL_KEY
                        )[0] +
                        " not valid."
                );
            }
        }
        
        if (pagingParameterMap.containsKey(PageCriteria.QUERY_URL_KEY)){
            String searchTerm = pagingParameterMap.get(PageCriteria.QUERY_URL_KEY)[0];
            pageCriteria.addOr("firstName", searchTerm);
            pageCriteria.addOr("lastName", searchTerm);
            pageCriteria.addOr("email", searchTerm);
            pageCriteria.addOr("username", searchTerm);
        }

        return pageCriteria;
    }
    
    /*
     * Delegated to by {@link #doGet} to handle requests
     * for the total number of users in the system.
     */
    private void processGetUserCount(HttpServletRequest req,
            HttpServletResponse resp)
    throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        
        sendPlainTextResponse(resp, Integer.toString(userService.getUsers().size()));
        return;
    }

    /*
     * Delegated to by {@link #doGet} to handle users GET
     * requests, retrieving all user accounts, setting the response
     * status and headers, and writing the response content.
     */
    private void processUsersGet(HttpServletRequest req,
            HttpServletResponse resp)
    throws ServletException, IOException {
        Collection<User> users;

        if (req.getQueryString() != null){
            PageCriteria<User.SortType> pageCriteria;
            try {
                pageCriteria = buildPageCriteria(req);
            }
            catch (CmpException e){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage());
                return;
            }
            users = userService.getUsers(pageCriteria).getList();

        } else {
            users = userService.getUsers();
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        sendXmlResponse(resp, new UsersResource(users, getUrlBase(req), 
                req.getParameterMap()));
    }

    /*
     * Delegated to by {@link #doGet} to handle server status GET
     * requests, taking a status snapshot, setting the response
     * status and headers, and writing the response content.
     */
    private void processServerStatus(HttpServletRequest req,
                                     HttpServletResponse resp)
        throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        StatusSnapshotResource resource =
            new StatusSnapshotResource(new StatusSnapshot());
        sendPlainTextResponse(resp, resource);
    }

    /*
     * Delegated to by {@link #doGet} to handle space usage requests.
     */
    private void processSpaceUsage(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   String username,
                                   boolean isXml)
        throws ServletException, IOException {
        User user = null;
        if (username != null) {
            user = userService.getUser(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (user.isOverlord()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        SpaceUsageResource resource = null;
        if (user == null) {
            if (log.isDebugEnabled())
                log.debug("generating usage report for all users");
            HashSet<SpaceUsageReport> reports =
                new HashSet<SpaceUsageReport>();
            for (User u : userService.getUsers()) {
                if (u.isOverlord())
                    continue;
                HomeCollectionItem home = contentService.getRootItem(u);
                SpaceUsageReport report = new SpaceUsageReport(u, home);
                reports.add(report);
            }
            resource = new SpaceUsageResource(reports);
        } else {
            if (log.isDebugEnabled())
                log.debug("generating usage report for user " + username);
            HomeCollectionItem home = contentService.getRootItem(user);
            resource =
                new SpaceUsageResource(new SpaceUsageReport(user, home));
        }

        resp.setStatus(HttpServletResponse.SC_OK);

        if (isXml)
            sendXmlResponse(resp, resource);
        else
            sendPlainTextResponse(resp, resource);
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
     * Delegated to by {@link #doPost} to handle user activation
     * requests.
     */
    private void processActivateUser(HttpServletRequest req,
            HttpServletResponse resp) {

        String username = req.getPathInfo().substring(
                URL_ACTIVATE.length());
        try {
            User user = userService.getUser(username);
            if (user.isActivated()){
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            user.activate();
            userService.updateUser(user);

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (DataRetrievalFailureException e){
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return; 
        }  
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
            UserResource resource = new UserResource(getUrlBase(req), xmldoc, entityFactory);
            User user = resource.getUser();
            user.setAdmin(Boolean.FALSE);
            user.setLocked(Boolean.FALSE);
            user = userService.createUser(user, createSignupListeners(req));
            resource = new UserResource(user, getUrlBase(req));
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setHeader("Content-Location", resource.getHomedirUrl());
            resp.setHeader("ETag", resource.getEntityTag());
            sendXmlResponse(resp, resource);
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
        } catch (InvalidStateException ise) {
            handleInvalidStateException(resp, ise);
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
            Boolean oldAdmin = user.getAdmin();
            Boolean oldLocked = user.isLocked();
            UserResource resource =
                new UserResource(user, getUrlBase(req), xmldoc, entityFactory);
            if (user.isUsernameChanged()) {
                // reset logged in user's username
                user.setUsername(oldUsername);
                log.warn("bad request for account update: " +
                "username may not be changed");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Username may not be changed");
                return;
            }
            if (user.isAdminChanged() &&
                    !oldAdmin){
                // Non admin tried to change admin status
                user.setAdmin(oldAdmin);
                log.warn("bad request for account update: " +
                "non-admin may not change own admin status");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Non admin may not change own admin status.");
                return;

            }
            if (user.isLocked() != oldLocked) {
                user.setLocked(oldLocked);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                               "User may not changed own locked status.");
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
        }catch (InvalidStateException ise) {
            handleInvalidStateException(resp, ise);
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
            UserResource resource = new UserResource(getUrlBase(req), xmldoc, entityFactory);
            User user = resource.getUser();
            if (user.getUsername() != null &&
                ! user.getUsername().equals(urlUsername)) {
                log.warn("bad request for user create: " +
                         "username does not match request URI");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Username does not match request URI");
                return;
            }
            userService.createUser(user, createUserCreateListeners(req));
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
        } catch (InvalidStateException ise) {
            handleInvalidStateException(resp, ise);
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
                new UserResource(user, getUrlBase(req), xmldoc, entityFactory);
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
        } catch (InvalidStateException ise) {
            handleInvalidStateException(resp, ise);
        }
    }
    
    /*
     * Delegated to by {@link #doPost} to handle password
     * recovery requests. 
     * 
     */
    private void processRecoverPassword(final HttpServletRequest req, 
            final HttpServletResponse resp) 
        throws IOException, ServletException {
        
        User user = getUserFromParameter(req);
        
        if (user != null) {
            
            String passwordRecoveryKey = 
                passwordRecoverer.createRecoveryKey();
            
            final PasswordRecovery passwordRecovery = 
                entityFactory.createPasswordRecovery(user, passwordRecoveryKey);
            
            userService.createPasswordRecovery(passwordRecovery);
            
            PasswordRecoveryMessageContext context = 
                createPasswordRecoveryMessageContext(req, passwordRecovery);
         
            passwordRecoverer.sendRecovery(passwordRecovery, context);
            
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void processResetPassword(HttpServletRequest req,
                                      HttpServletResponse resp) 
        throws ServletException, IOException{
        
        String path = req.getPathInfo();
        String passwordResetKey = path.substring(URL_PASSWORD_RESET.length());
        
        PasswordRecovery passwordRecovery = 
            userService.getPasswordRecovery(passwordResetKey);
        
        if (passwordRecovery != null){
            String newPassword = req.getParameter("password");
            
            if (newPassword != null){
                User user = passwordRecovery.getUser();

                user.setPassword(newPassword);

                userService.updateUser(user);

                userService.deletePasswordRecovery(passwordRecovery);

                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /*
     * Delegated to by {@link #doPost} to handle activation email 
     * resend requests
     * 
     */
    private void processSendActivation(final HttpServletRequest req, 
            final HttpServletResponse resp) 
        throws IOException, ServletException {
        
        User user = getUserFromParameter(req);
        
        if (user != null && user.getActivationId() != null) {
            accountActivator.sendActivationMessage(user, createActivationContext(req));
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private User getUserFromParameter(HttpServletRequest req){
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        
        if (username != null){
            return this.userService.getUser(username);
        } else if (email != null){
            return this.userService.getUserByEmail(email);
        } else return null;
    }
    
    private void handleInvalidStateException(HttpServletResponse resp,
                                             InvalidStateException ise)
        throws IOException {
        String message = ise.getInvalidValues()[0].getMessage();
        log.warn("model validation error: " + message);
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
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

            // Find the end of the username
            int endIndex = username.indexOf("/");
            if (endIndex > -1){
                username = username.substring(0, endIndex);
            }

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
                                 OutputsXml resource)
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
        resp.setContentType(CmpConstants.MEDIA_TYPE_XML);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }
    
    private void sendPlainTextResponse(HttpServletResponse resp, String text)
        throws ServletException, IOException {
            
        resp.setContentType(CmpConstants.MEDIA_TYPE_PLAIN_TEXT);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(text.length());
        resp.getWriter().write(text);
    }

    private void sendPlainTextResponse(HttpServletResponse resp,
            OutputsPlainText resource)
    throws ServletException, IOException {
        sendPlainTextResponse(resp, resource.toText());
    }

    private String getUrlBase(HttpServletRequest req) {
        // like response.encodeUrl() except does not include servlet
        // path or session id
        StringBuffer buf = new StringBuffer();
        buf.append(req.getScheme()).
            append("://").
            append(req.getServerName());
        if ((req.getScheme().equals("https") && req.getServerPort() != 443) ||
            (req.getScheme().equals("http") && req.getServerPort() != 80)) {
            buf.append(":").append(req.getServerPort());
        }
        if (! req.getContextPath().equals("/")) {
            buf.append(req.getContextPath());
        }
        return buf.toString();
    }

    private ServiceListener[] createSignupListeners(HttpServletRequest req) {
        return new ServiceListener[] { createActivationListener(req),
                                       createOotbListener(req) };
    }

    private ServiceListener[] createUserCreateListeners(HttpServletRequest req) {
        return new ServiceListener[] { createOotbListener(req) };
    }

    private ActivationListener createActivationListener(HttpServletRequest req) {
        return new ActivationListener(accountActivator, createActivationContext(req));
    }

    private ActivationContext createActivationContext(HttpServletRequest req) {
        ActivationContext activationContext = new ActivationContext();
        String urlBase = getUrlBase(req);
        activationContext.setLocale(req.getLocale());
        activationContext.setActivationLinkTemplate(
                urlBase+ "/account/activate/" + "{" + 
                ActivationContext.LINK_TEMPLATE_VAR_ACTIVATION_ID + "}");
        activationContext.setHostname(urlBase);
        activationContext.
            setSender(userService.getUser(User.USERNAME_OVERLORD));

        return activationContext;
    }

    private OutOfTheBoxListener createOotbListener(HttpServletRequest req) {
        OutOfTheBoxContext context = new OutOfTheBoxContext();
        context.setLocale(req.getLocale());
        // XXX get timezone from user somehow
        context.setTimeZone(TimeZone.getDefault());
        return new OutOfTheBoxListener(ootbHelper, context);
    }
    
    private PasswordRecoveryMessageContext 
        createPasswordRecoveryMessageContext(HttpServletRequest req, 
                PasswordRecovery recovery){
        PasswordRecoveryMessageContext context = 
            new PasswordRecoveryMessageContext();
        
        String urlBase = getUrlBase(req);
        
        context.setSender(userService.getUser(User.USERNAME_OVERLORD));
        context.setLocale(req.getLocale());
        context.setRecoveryLink(
                urlBase + URL_PASSWORD_RESET + recovery.getKey());
        
        context.setHostname(urlBase);
        
        return context;
    }
}
