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
package org.osaf.cosmo.atom;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.model.Base;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.protocol.server.HttpResponse;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.impl.BaseResponseContext;
import org.apache.abdera.protocol.server.impl.HttpServletRequestContext;
import org.apache.abdera.protocol.server.servlet.HttpResponseServletAdapter;
import org.apache.abdera.util.MimeTypeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet which handles Atom Syndication Format and Atom Publishing
 * Protocol access to the server.
 */
public class AtomServlet extends HttpServlet implements AtomConstants {
    private static final Log log = LogFactory.getLog(AtomServlet.class);

    private static final String BEAN_SERVICE_CONTEXT =
        "atomServiceContext";

    private WebApplicationContext wac;
    private ServiceContext serviceContext;

    // Servlet methods

    protected void service(HttpServletRequest req,
                           HttpServletResponse res)
        throws ServletException, IOException {
        RequestContext requestContext;
        try {
            requestContext = new HttpServletRequestContext(serviceContext, req);
        } catch (CosmoSecurityException e) {
            // Handle case where principal isn't authorized for target
            if(e instanceof ItemSecurityException) {
                handleItemSecurityError((ItemSecurityException) e, res);
            }
            else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            }
            return;
        }
        
        HttpResponse response = new HttpResponseServletAdapter(res);
        serviceContext.getRequestHandlerManager().get(requestContext).
            process(serviceContext, requestContext, response);
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
    public void init()
        throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (wac != null) {
            serviceContext = (ServiceContext)
                getBean(BEAN_SERVICE_CONTEXT, ServiceContext.class);
        }

        if (serviceContext == null)
            throw new ServletException("serviceContext must not be null");
    }

    // our methods

    /** */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /** */
    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
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
    
    protected void handleItemSecurityError(ItemSecurityException ise, HttpServletResponse resp) {
        InsufficientPrivilegesException se = new InsufficientPrivilegesException(ise);
        ResponseContext rc = getBaseContext(se.createDocument(serviceContext.getAbdera()), se.getCode(), null);
        try {
            rc.writeTo(resp.getOutputStream());
        } catch (Throwable e) {
            log.error("Error writing XML", e);
            log.error("Original exception", ise);
            resp.setStatus(500);
        } 
    }
    
    protected ResponseContext getBaseContext(Base base,
            int status, Date lastModified) {

        BaseResponseContext response = new BaseResponseContext(base);
        response.setStatus(status);
        if (lastModified != null)
            response.setLastModified(lastModified);
        response.setContentType(MimeTypeHelper.getMimeType(base));
        Document doc = base instanceof Document ? (Document) base
                : ((Element) base).getDocument();
        if (doc.getEntityTag() != null) {
            response.setEntityTag(doc.getEntityTag());
        } else if (doc.getLastModified() != null) {
            response.setLastModified(doc.getLastModified());
        }
        
        response.setWriter(serviceContext.getAbdera().getWriterFactory().getWriter("PrettyXML"));
        
        return response;
    }
}
