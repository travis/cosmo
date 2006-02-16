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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.core.query.QueryTreeBuilderRegistry;
import org.apache.jackrabbit.core.query.lucene.TextFilterFactory;

import org.osaf.cosmo.CosmoConstants;

/**
 * @author cyrusdaboo
 * 
 * Initialises the required text filters and custom queries on webapp startup.
 * 
 */
public class TextFilterListener implements ServletContextListener {
    private static final Log log = LogFactory.getLog(TextFilterListener.class);

    /**
     */
    public static final String INIT_PARAM_USE_TEXT_FILTER = "use-text-filter";

    /*
     */
    public void contextInitialized(ServletContextEvent sce) {

        if (CosmoConstants.INDEX_VIRTUAL_PROPERTIES) {
            // Add our special text/calendar data indexer to the list of data
            // indexers used by Jackrabbit
            if (log.isDebugEnabled()) {
                log.debug("loading text/calendar text filter");
            }
            TextFilterFactory.addTextFilter(new TextCalendarTextFilter());
        }

        // Add our extended XPath query builder to Jackrabbit.
        QueryTreeBuilderRegistry.addBuilder(
                XPathTimeRangeQueryBuilder.XPATH_TIMERANGE,
                new TimeRangeQueryBuilder());
    }

    /*
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Does nothing
    }
}
