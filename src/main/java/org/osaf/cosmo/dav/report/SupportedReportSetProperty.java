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

import java.util.HashSet;
import java.util.Iterator;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is copied pretty much verbatim from
 * org.apache.jackrabit.webdav.version.SupportedReportSetProperty.
 */

/**
 * <code>SupportedReportSetProperty</code> represents the
 * DAV:supported-report-set property defined by RFC 3253. It identifies the
 * reports that are supported by the given resource.
 * 
 * <pre>
 *    &lt;!ELEMENT supported-report-set (supported-report*)&gt;
 *    &lt;!ELEMENT supported-report report&gt;
 *    &lt;!ELEMENT report ANY&gt;
 *    ANY value: a report element type
 * </pre>
 */
public class SupportedReportSetProperty extends AbstractDavProperty {

    private final HashSet reportTypes = new HashSet();

    /**
     * Create a new empty <code>SupportedReportSetProperty</code>.
     */
    public SupportedReportSetProperty() {
        super(DeltaVConstants.SUPPORTED_REPORT_SET, true);
    }

    /**
     * Create a new <code>SupportedReportSetProperty</code> property.
     * 
     * @param reportTypes
     *            that are supported by the resource having this property.
     */
    public SupportedReportSetProperty(ReportType[] reportTypes) {
        super(DeltaVConstants.SUPPORTED_REPORT_SET, true);
        for (int i = 0; i < reportTypes.length; i++) {
            addReportType(reportTypes[i]);
        }
    }

    /**
     * Add an additional report type to this property's value.
     * 
     * @param reportType
     */
    public void addReportType(ReportType reportType) {
        reportTypes.add(reportType);
    }

    /**
     * Returns true if the report type indicated in the specified
     * <code>RequestInfo</code> object is included in the supported reports.
     * 
     * @param reqInfo
     * @return true if the requested report is supported.
     */
    public boolean isSupportedReport(ReportInfo reqInfo) {
        Iterator it = reportTypes.iterator();
        while (it.hasNext()) {
            ReportType rt = (ReportType) it.next();
            if (rt.isRequestedReportType(reqInfo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a <code>Set</code> of
     <code>SupportedReportSetProperty.ReportTypeInfo</code>s for this
     * property.
     */
    public Object getValue() {
        HashSet infos = new HashSet();
        for (Iterator i=reportTypes.iterator(); i.hasNext();) {
            infos.add(new ReportTypeInfo((ReportType) i.next()));
        }
        return infos;
    }


    /**
     */
    public class ReportTypeInfo implements XmlSerializable {
        private ReportType reportType;

        /**
         */
        public ReportTypeInfo(ReportType reportType) {
            this.reportType = reportType;
        }

        /**
         */
        public Element toXml(Document document) {
            Element r =
                DomUtil.createElement(document,
                                      DeltaVConstants.XML_REPORT,
                                      DeltaVConstants.NAMESPACE);
            r.appendChild(reportType.toXml(document));

            Element sr =
                DomUtil.createElement(document,
                                      DeltaVConstants.XML_SUPPORTED_REPORT,
                                      DeltaVConstants.NAMESPACE);
            sr.appendChild(r);

            return sr;
        }
    }
}
