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
package org.osaf.cosmo.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extends the Spring
 * {@link org.springframework.web.servlet.DispatcherServlet}
 * to provide Cosmo-specific behaviors.
 */
public class DispatcherServlet
    extends org.springframework.web.servlet.DispatcherServlet {
    private static final Log log = LogFactory.getLog(DispatcherServlet.class);

    // HttpServlet methods

    /**
     * Extends the superclass method to catch and log any unhandled
     * throwables.
     */
    protected void service(HttpServletRequest req,
                           HttpServletResponse res)
        throws ServletException, IOException {
        try {
            super.service(req, res);
        } catch (Throwable e) {
            log.error("Internal UI error", e);
            res.sendError(500, "Internal UI error: " + e.getMessage());
        }
    }
}
