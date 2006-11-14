/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.cmp;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.server.SpaceUsageReport;
import org.osaf.cosmo.util.DateUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A resource view of a {@link SpaceUsageReport}. This is a read-only
 * resource.
 */
public class SpaceUsageResource
    implements CmpResource, OutputsPlainText, OutputsXml {
    private static final Log log = LogFactory.getLog(SpaceUsageResource.class);

    /** */
    public static final String EL_ITEMS = "items";
    /** */
    public static final String EL_ITEM = "item";
    /** */
    public static final String EL_MODIFIED = "modified";
    /** */
    public static final String EL_OWNER = "owner";
    /** */
    public static final String EL_SIZE = "size";
    /** */
    public static final String EL_PATH = "path";

    private HashSet<SpaceUsageReport> reports =
        new HashSet<SpaceUsageReport>();

    /**
     * Constructs a resource that represents the given
     * {@link SpaceUsageReport}.
     */
    public SpaceUsageResource(SpaceUsageReport report) {
        this.reports.add(report);
    }

    /**
     * Constructs a resource that represents a set of
     * {@link SpaceUsageReport}s;
     */
    public SpaceUsageResource(Set<SpaceUsageReport> reports) {
        this.reports.addAll(reports);
    }

    // CmpResource methods

    /**
     * Returns the <code>Set<SpaceUsageReport></code> that backs this
     * resource.
     */
    public Object getEntity() {
        return reports;
    }


    // OutputsPlainText methods

    /**
     * Returns a plain text representation of the report.
     *
     * The text is structured like so:
     *
     * <verbatim>
     *   2006-11-01T19:21:24-0800        bcm     0       /bcm
     *   2006-11-10T00:17:03-0800        bcm     2643    /bcm/maven.xml
     *   2006-11-01T19:21:43-0800        bcm     0       /bcm/office_calendar
     *   2006-11-01T19:22:19-0800        bcm     454     /bcm/office_calendar/dddc1d18-f8e0-11da-8167-c8bc8b8b8752.ics
     * </verbatim>
     */
    public String toText() {
        StringBuffer buf = new StringBuffer();

        for (SpaceUsageReport report : reports) {
            for (SpaceUsageReport.UsageLineItem lineItem :
                     report.getLineItems()) {
                String modified =
                    DateUtil.formatRfc3339Date(lineItem.getLastAccessed());
                buf.append(modified).append("\t").
                    append(lineItem.getOwner().getUsername()).append("\t").
                    append(lineItem.getSize()).append("\t").
                    append(lineItem.getPath()).append("\n");
            }
        }

        return buf.toString();
    }

    // OutputsXml methods

    /**
     * Returns an XML representation of the resource in the form of a
     * {@link org.w3c.dom.Element}.
     *
     * The XML is structured like so:
     *
     * <pre>
     * <items>
     *   <item>
     *     <modified>2006-11-01T19:21:24-0800</modified>
     *     <owner>bcm</owner>
     *     <size>15088</size>
     *     <path>/bcm/pom.xml</path>
     *   </item>
     * </items>
     * </pre>
     */
    public Element toXml(Document doc) {
        Element items =  DomUtil.createElement(doc, EL_ITEMS, NS_CMP);

        for (SpaceUsageReport report : reports) {
            HashSet<Element> elements = toXml(doc, report);
            for (Element e : elements)
                items.appendChild(e);
        }

        return items;
    }

    private HashSet<Element> toXml(Document doc,
                                   SpaceUsageReport report) {
        HashSet<Element> items = new HashSet<Element>();

        for (SpaceUsageReport.UsageLineItem lineItem : report.getLineItems()) {
            Element item = DomUtil.createElement(doc, EL_ITEM, NS_CMP);

            Element modified = DomUtil.createElement(doc, EL_MODIFIED, NS_CMP);
            String m = DateUtil.formatRfc3339Date(lineItem.getLastAccessed());
            DomUtil.setText(modified, m);
            item.appendChild(modified);

            Element owner = DomUtil.createElement(doc, EL_OWNER, NS_CMP);
            DomUtil.setText(owner, lineItem.getOwner().getUsername());
            item.appendChild(owner);

            Element size = DomUtil.createElement(doc, EL_SIZE, NS_CMP);
            DomUtil.setText(size, lineItem.getSize().toString());
            item.appendChild(size);

            Element path = DomUtil.createElement(doc, EL_PATH, NS_CMP);
            DomUtil.setText(path, lineItem.getPath());
            item.appendChild(path);

            items.add(item);
        }

        return items;
    }
}
