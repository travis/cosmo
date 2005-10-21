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
package org.osaf.cosmo.ui.config.jcr;

import javax.jcr.Session;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.ui.config.ServletContextConfigurer;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.springmodules.jcr.JcrSessionFactory;
import org.springmodules.jcr.SessionFactoryUtils;
import org.springmodules.jcr.SessionHolder;
import org.springmodules.jcr.support.DefaultSessionHolderProvider;

/**
 * Extends {@link ServletContextConfigurer} to open a JCR session
 * for use during configuration.
 */
public class JcrServletContextConfigurer extends ServletContextConfigurer {
    private static final Log log =
        LogFactory.getLog(JcrServletContextConfigurer.class);
    private static final String BEAN_SESSION_FACTORY =
        "homedirSessionFactory";

    private Session session;
    private JcrSessionFactory sessionFactory;

    /**
     * Opens a JCR session, execute the superclass'
     * <code>configure</code> method, then closes the session.
     */
    public void configure(ServletContext sc) {
        openSession(sc);
        super.configure(sc);
        closeSession();
    }

    /**
     */
    protected void openSession(ServletContext sc) {
        WebApplicationContext wac =
            WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
        sessionFactory = (JcrSessionFactory)
            wac.getBean(BEAN_SESSION_FACTORY, JcrSessionFactory.class);
        session = SessionFactoryUtils.getSession(sessionFactory, true);
        SessionHolder sessionHolder =
            new DefaultSessionHolderProvider().createSessionHolder(session);
        TransactionSynchronizationManager.bindResource(sessionFactory,
                                                       sessionHolder);
    }

    /**
     */
    protected void closeSession() {
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.releaseSession(session, sessionFactory);
    }
}
