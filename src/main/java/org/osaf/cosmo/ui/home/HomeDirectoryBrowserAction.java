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
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.model.CollectionResource;
import org.osaf.cosmo.model.FileResource;
import org.osaf.cosmo.model.Resource;
import org.osaf.cosmo.service.HomeDirectoryService;
import org.osaf.cosmo.ui.CosmoAction;

/**
 * Action for browsing a user's home directory.
 */
public class HomeDirectoryBrowserAction extends CosmoAction {
    private static final Log log =
        LogFactory.getLog(HomeDirectoryBrowserAction.class);

    private HomeDirectoryService homeDirectoryService;

    /**
     */
    public static final String PARAM_PATH = "path";
    /**
     */
    public static final String ATTR_COLLECTION = "Collection";
    /**
     */
    public static final String FWD_COLLECTION = "collection";

    /**
     */
    public ActionForward browse(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
        throws Exception {
        String path = request.getParameter(PARAM_PATH);

        Resource resource = homeDirectoryService.getResource(path);

        if (resource instanceof CollectionResource) {
            request.setAttribute(ATTR_COLLECTION, resource);
            addTitleParam(request, resource.getPath());
            return mapping.findForward(FWD_COLLECTION);
        }
        else if (resource instanceof FileResource) {
            FileResource file = (FileResource) resource;

            // set headers
            response.setStatus(HttpServletResponse.SC_OK);
            if (file.getContentType() != null) {
                response.setContentType(file.getContentType());
            }
            else {
                response.setContentType("application/octet-stream");
            }
            if (file.getContentLength() != null) {
                response.setContentLength(file.getContentLength().intValue());
            }
            if (file.getContentEncoding() != null) {
                response.setCharacterEncoding(file.getContentEncoding());
            }
            if (file.getContentLanguage() != null) {
                response.setHeader("Content-Language",
                                   file.getContentLanguage());
            }

            // spool data
            InputStream in = file.getContent();
            OutputStream out = response.getOutputStream();
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                }
            } finally {
                in.close();
            }
            response.flushBuffer();

            return null;
        }
        else {
            throw new ServletException("resource at path " + path +
                                       " neither file nor collection");
        }
    }

    /**
     */
    public HomeDirectoryService getHomeDirectoryService() {
        return homeDirectoryService;
    }

    /**
     */
    public void setHomeDirectoryService(HomeDirectoryService service) {
        this.homeDirectoryService = service;
    }
}
