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
package org.osaf.cosmo.ui.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A {@link javax.servlet.ServletContextListener} that uses a
 * {@link ServletContextConfigurer} to load the Cosmo application
 * configuration when the servlet context is started.
 */
public class ConfigurationListener implements ServletContextListener {
    private static final Log log =
        LogFactory.getLog(ConfigurationListener.class);
    private static final String BEAN_SERVLET_CONTEXT_CONFIGURER =
        "servletContextConfigurer";

    /**
     * Gets the configurer from the
     * {@link org.springframework.web.context.WebApplicationContext}
     * and directs it to configure the servlet context.
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

        // load application configuration
        ServletContextConfigurer scc = (ServletContextConfigurer)
            wac.getBean(BEAN_SERVLET_CONTEXT_CONFIGURER,
                        ServletContextConfigurer.class);
        scc.configure(sc);
    }

    /**
     * Does nothing.
     */
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
