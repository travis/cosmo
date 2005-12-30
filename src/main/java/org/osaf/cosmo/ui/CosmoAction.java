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
package org.osaf.cosmo.ui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.ActionDispatcher;

import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.ui.config.ServletContextConfigurer;

/**
 * Base class for Cosmo UI actions that provides important
 * utility methods and uses a
 * {@link org.apache.struts.actions.ActionDispatcher} to delegate
 * action execution.
 *
 * The dispatcher instance is created when needed by the
 * <code>createDispatcher()</code> method, which subclasses may
 * override.
 *
 * By default the dispatcher uses the
 * <code>ActionDispatcher.MAPPING_FLAVOR</code> flavor. This choice
 * can be overridden with <code>setDispatcherFlavor()</code>.
 */
public class CosmoAction extends Action {
    private static final Log log = LogFactory.getLog(CosmoAction.class);

    private ActionDispatcher dispatcher;
    private int dispatcherFlavor = ActionDispatcher.MAPPING_FLAVOR;
    private ServletContextConfigurer configurer;
    private CosmoSecurityManager securityManager;

    /**
     * The request parameter in which view title parameters are
     * stored: <code>TitleParam</code>
     */
    public static final String ATTR_TITLE_PARAM = "TitleParam";

    /**
     * Delegates execution to the method chosen by the dispatcher
     * returned by <code>getDispatcher()</code>.
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        return getDispatcher().execute(mapping, form, request, response);
    }

    /**
     */
    public ActionDispatcher getDispatcher() {
        if (dispatcher == null) {
            dispatcher = createDispatcher();
        }
        return dispatcher;
    }

    /**
     */
    protected ActionDispatcher createDispatcher() {
        return new ActionDispatcher(this, getDispatcherFlavor());
    }

    /**
     */
    public int getDispatcherFlavor() {
        return dispatcherFlavor;
    }

    /**
     */
    public void setDispatcherFlavor(int flavor) {
        dispatcherFlavor = flavor;
    }

    /**
     * Store a title parameter in the <code>ATTR_TITLE_PARAM</code>
     * request attribute.
     *
     * Title parameters can be used by a view to build a page title
     * including dynamic elements that are set by the action but not
     * known by the view at rendering time. An example is using a
     * layout JSP to include the name of an object being managed via
     * an administrative console in the title of an HTML page without
     * the layout knowing the type of object it is displaying.
     */
    public void addTitleParam(HttpServletRequest request,
                              String param) {
        List params = (List) request.getAttribute(ATTR_TITLE_PARAM);
        if (params == null) {
            params = new ArrayList();
            request.setAttribute(ATTR_TITLE_PARAM, params);
        }
        params.add(param);
    }

    /**
     * Saves the key for a global confirmation message into the HTTP
     * session such that it can be retrieved by the Struts
     * <html:messages message="true"/> tag.
     */
    public void saveConfirmationMessage(HttpServletRequest request,
                                        String key) {
        ActionMessages messages = getMessages(request);
        messages.add(ActionMessages.GLOBAL_MESSAGE,
                     new ActionMessage(key));
        saveMessages(request.getSession(), messages);
    }

    /**
     * Saves the key for a property-specific error message into the
     * HTTP request such that it can be retrieved by the Struts
     * <html:messages/> tag.
     */
    public void saveErrorMessage(HttpServletRequest request,
                                 String key,
                                 String property) {
        if (property == null) {
            throw new IllegalArgumentException("cannot save an error message with a null property");
        }
        ActionMessages messages = getErrors(request);
        messages.add(property, new ActionMessage(key));
        saveErrors(request.getSession(), messages);
    }

    /**
     * Saves the key for a global error message into the HTTP request
     * such that it can be retrieved by the Struts <html:messages/>
     * tag.
     */
    public void saveErrorMessage(HttpServletRequest request,
                                 String key) {
        saveErrorMessage(request, key, ActionMessages.GLOBAL_MESSAGE);
    }

    /**
     * Copies an {@link org.apache.struts.ActionForward}, appending
     * the given path to the copied forward's original path.
     */
    public ActionForward copyForward(ActionForward forward,
                                     String path) {
        String newPath = forward.getPath();
        if (path != null) {
            newPath = newPath + path;
        }
        return new ActionForward(forward.getName(), newPath,
                                 forward.getRedirect(),
                                 forward.getModule());
    }

    /**
     * Returns an absolute URL 
     */
    public String getContextRelativeURL(HttpServletRequest req,
                                        String path) {
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
        if (path != null) {
            buf.append(path);
        }
        return buf.toString();
    }

    /**
     */
    public ServletContextConfigurer getConfigurer() {
        return configurer;
    }

    /**
     */
    public void setConfigurer(ServletContextConfigurer configurer) {
        this.configurer = configurer;
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
}
