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

import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.model.DavCollection;
import org.osaf.cosmo.model.DavResource;
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

        DavResource resource = homeDirectoryService.getResource(path);

        if (resource instanceof DavCollection) {
            request.setAttribute(ATTR_COLLECTION, resource);
            addTitleParam(request, resource.getPath());
            return mapping.findForward(FWD_COLLECTION);
        }

        // XXX: write the resource's stream directly

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.write("display name: " + resource.getDisplayName() + "\n");
        writer.write("path: " + resource.getPath() + "\n");

        return null;
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
