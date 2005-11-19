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
package org.osaf.cosmo.jackrabbit.query;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jackrabbit.core.query.QueryTreeBuilderRegistry;
import org.apache.jackrabbit.core.query.lucene.TextFilterFactory;

/**
 * @author cyrusdaboo
 * 
 * Initialises the required text filters on webapp startup.
 * 
 */
public class TextFilterListener implements ServletContextListener {

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {

        // Add our special text/calendar data indexer to the list of data
        // indexers used by Jackrabbit
        TextFilterFactory.addTextFilter(new TextCalendarTextFilter());

        // Add our extended XPath query builder to Jabkrabbit.
        QueryTreeBuilderRegistry.addBuilder(
                XPathTimeRangeQueryBuilder.XPATH_TIMERANGE,
                new TimeRangeQueryBuilder());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        // Does nothing
    }

}
