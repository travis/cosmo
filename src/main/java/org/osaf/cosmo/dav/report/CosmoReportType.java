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
package org.osaf.cosmo.dav.report;

import org.apache.jackrabbit.webdav.version.report.ReportType;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.caldav.FreeBusyReport;
import org.osaf.cosmo.dav.caldav.MultigetReport;
import org.osaf.cosmo.dav.caldav.QueryReport;

/**
 * This class registers additional DAV report types supported by
 * Cosmo.
 *
 * @see org.apache.jackrabbit.webdav.version.report.ReportType
 */
public class CosmoReportType {

    public static final ReportType CALDAV_QUERY =
        ReportType.register(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_QUERY,
                            CosmoDavConstants.NAMESPACE_CALDAV,
                            QueryReport.class);
    public static final ReportType CALDAV_MULTIGET =
        ReportType.register(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_MULTIGET,
                            CosmoDavConstants.NAMESPACE_CALDAV,
                            MultigetReport.class);
    public static final ReportType CALDAV_FREEBUSY =
        ReportType.register(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_FREEBUSY,
                            CosmoDavConstants.NAMESPACE_CALDAV,
                            FreeBusyReport.class);
}
