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
package org.osaf.cosmo.repository;

import javax.jcr.RepositoryException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A {@link javax.servlet.ServletContextListener} that invokes a
 * {@link RepositoryInitializer} to initialize the structure and
 * content of the Cosmo repository at startup.
 *
 * A repository is only initialized if we detect that there is no root
 * user in the repository.
 */
public class RepositoryListener implements ServletContextListener {
    private static final Log log =
        LogFactory.getLog(RepositoryListener.class);

    /**
     */
    public static final String BEAN_INITIALIZER = "repositoryInitializer";

    /**
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

        RepositoryInitializer initializer =(RepositoryInitializer)
            wac.getBean(BEAN_INITIALIZER, RepositoryInitializer.class);
        initializer.initialize();
    }

    /**
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // does nothing
    }
}
