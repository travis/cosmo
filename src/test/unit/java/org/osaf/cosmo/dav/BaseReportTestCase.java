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
package org.osaf.cosmo.dav;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.User;

/**
 * Base class for servlet test cases using the REPORT method.
 */
public abstract class BaseReportTestCase extends BaseDavServletTestCase {
    private static final Log log = LogFactory.getLog(BaseReportTestCase.class);

    private static final String[] REPORT_EVENTS = {
        "reports/put/1.ics",
        "reports/put/2.ics",
        "reports/put/3.ics",
        "reports/put/4.ics",
        "reports/put/5.ics",
        "reports/put/6.ics",
        "reports/put/7.ics"
    };

    protected String testUri;

    /**
     */
    protected void setUp() throws Exception {
        super.setUp();

        User user = testHelper.makeAndStoreDummyUser();

        // make a calendar collection to put events into
        Node calendarCollectionNode =
            testHelper.addCalendarCollectionNode("/" + user.getUsername(),
                                                 getName());
        setUpDavData(calendarCollectionNode);

        // XXX: not sure why we have to save, since theoretically
        // testHelper and the servlet are using the same jcr session
        calendarCollectionNode.getParent().save();

        // XXX: URL-escape
        testUri = "/" + user.getUsername() + "/" + getName() + "/";
    }

    /**
     */
    protected void setUpDavData(Node node) throws Exception {
        // put all test events in the calendar collection
        for (int i=0; i<REPORT_EVENTS.length; i++) {
            Node resourceNode =
                testHelper.addCalendarResourceNode(node, REPORT_EVENTS[i]);
        }
    }
}
