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
package org.osaf.cosmo.dav.impl;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.caldav.report.MultigetReport;
import org.osaf.cosmo.dav.caldav.report.QueryReport;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * Test case for <code>DavCalendarResource</code>.
 */
public class DavCalendarResourceTest extends BaseDavTestCase {
    private static final Log log = LogFactory.getLog(DavCalendarResourceTest.class);

    public void testCaldavReportTypes() throws Exception {
        TestCalendarResource test = new TestCalendarResource(null, null);

        assert(test.getReportTypes().contains(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY));
        assert(test.getReportTypes().contains(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET));
        assert(test.getReportTypes().contains(QueryReport.REPORT_TYPE_CALDAV_QUERY));
    }

    public class TestCalendarResource extends DavCalendarResource {
        private Calendar calendar;

        public TestCalendarResource(ContentItem item,
                                    DavResourceLocator locator,
                                    DavResourceFactory factory)
            throws DavException {
            super(item, locator, factory);
        }

        public TestCalendarResource(DavResourceLocator locator,
                                    DavResourceFactory factory)
            throws DavException {
            super(new NoteItem(), locator, factory);
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
    }
}
