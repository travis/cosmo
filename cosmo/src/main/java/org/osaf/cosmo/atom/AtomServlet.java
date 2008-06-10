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

import javax.activation.MimeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.protocol.error.Error;
import org.apache.abdera.protocol.server.FilterChain;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.BaseResponseContext;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;
import org.apache.abdera.util.MimeTypeHelper;
import org.apache.abdera.writer.StreamWriter;
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

    private static final String BEAN_PROVIDER = "atomProvider";

    private Provider provider;
    
    private WebApplicationContext wac;
    
    // Servlet methods

    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        RequestContext reqcontext = new ServletRequestContext(provider, request);
        FilterChain chain = new FilterChain(provider, reqcontext);
        try {
            output(request, response, chain.next(reqcontext));
        } catch (CosmoSecurityException e) {
            // Handle case where principal isn't authorized for target
            if(e instanceof ItemSecurityException) {
                handleItemSecurityError((ItemSecurityException) e, response);
            }
            else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            }
            return;
        } catch (Throwable t) {
            error("Error servicing request", t, response);
            return;
        }
        log.debug("Request complete");
    }
    
    private void output(HttpServletRequest request,
            HttpServletResponse response, ResponseContext context)
            throws IOException {
        if (context != null) {
            response.setStatus(context.getStatus());
            long cl = context.getContentLength();
            String cc = context.getCacheControl();
            if (cl > -1)
                response.setHeader("Content-Length", Long.toString(cl));
            if (cc != null && cc.length() > 0)
                response.setHeader("Cache-Control", cc);
            try {
                MimeType ct = context.getContentType();
                if (ct != null)
                    response.setContentType(ct.toString());
            } catch (Exception e) {
            }
            String[] names = context.getHeaderNames();
            for (String name : names) {
                Object[] headers = context.getHeaders(name);
                for (Object value : headers) {
                    if (value instanceof Date)
                        response.setDateHeader(name, ((Date) value).getTime());
                    else
                        response.setHeader(name, value.toString());
                }
            }

            if (!request.getMethod().equals("HEAD") && context.hasEntity()) {
                context.writeTo(response.getOutputStream());
            }
        } else {
            error("Internal Server Error", null, response);
        }
    }
    
    private void error(String message, Throwable t, HttpServletResponse response)
            throws IOException {
        if (t != null)
            log.error(message, t);
        else
            log.error(message);

        if (response.isCommitted()) {
            log
                    .error("Could not write an error message as the headers & HTTP status were already committed!");
        } else {
            response.setStatus(500);
            StreamWriter sw = getAbdera().newStreamWriter().setOutputStream(
                    response.getOutputStream(), "UTF-8");
            Error.create(sw, 500, message, t);
            sw.close();
        }
    }

    public Abdera getAbdera() {
        return provider.getAbdera();
    }
    
    // GenericServlet methods

    /**
     * Loads the servlet context's <code>WebApplicationContext</code> and
     * wires up dependencies. If no <code>WebApplicationContext</code> is
     * found, dependencies must be set manually (useful for testing).
     * 
     * @throws ServletException
     *             if required dependencies are not found
     */
    public void init()
        throws ServletException {
        super.init();

        wac = WebApplicationContextUtils.
            getWebApplicationContext(getServletContext());

        if (wac != null) {
            provider = (Provider)
                getBean(BEAN_PROVIDER, Provider.class);
        }

        if (provider == null)
            throw new ServletException("provider must not be null");
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
        ResponseContext rc = getBaseContext(se.createDocument(getAbdera()), se.getCode(), null);
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
        
        response.setWriter(getAbdera().getWriterFactory().getWriter("PrettyXML"));
        
        return response;
    }
}
