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

import java.util.HashMap;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.jdom.Element;
import org.jdom.Namespace;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.report.caldav.FreeBusyReport;
import org.osaf.cosmo.dav.report.caldav.MultigetReport;
import org.osaf.cosmo.dav.report.caldav.QueryReport;

/**
 * This class is copied pretty much verbatim from
 * org.apache.jackrabit.webdav.version.ReportType.
 */

/**
 * <code>ReportType</code>...
 */
public class ReportType implements DeltaVConstants {

    private static final HashMap types = new HashMap();
    public static final ReportType CALDAV_QUERY = register(
            CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_QUERY,
            CosmoDavConstants.NAMESPACE_CALDAV, QueryReport.class);
    public static final ReportType CALDAV_MULTIGET = register(
            CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_MULTIGET,
            CosmoDavConstants.NAMESPACE_CALDAV, MultigetReport.class);
    public static final ReportType CALDAV_FREEBUSY = register(
            CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_FREEBUSY,
            CosmoDavConstants.NAMESPACE_CALDAV, FreeBusyReport.class);

    private final String name;
    private final Namespace namespace;
    private final Class reportClass;

    /**
     * Private constructor
     * 
     * @see #register(String, Namespace, Class)
     */
    private ReportType(String name, Namespace namespace, Class reportClass) {
        this.name = name;
        this.namespace = namespace;
        this.reportClass = reportClass;
    }

    /**
     * Creates a new {@link Report} with this type.
     * 
     * @return
     * @throws DavException
     */
    public Report createReport()
        throws DavException {
        try {
            return (Report) reportClass.getConstructor(new Class[0])
                    .newInstance(new Object[0]);
        } catch (Exception e) {
            // should never occur
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to register Report.");
        }
    }

    /**
     * Returns an Xml element representing this report type. It may be used to
     * build the body for a REPORT request.
     * 
     * @return Xml representation
     */
    public Element toXml() {
        return new Element(name, namespace);
    }

    /**
     * Returns true if this <code>ReportType</code> is requested by the given
     * <code>ReportInfo</code>
     * 
     * @param reqInfo
     * @return
     */
    public boolean isRequestedReportType(ReportInfo reqInfo) {
        if (reqInfo != null) {
            Element elem = reqInfo.getReportElement();
            if (elem != null) {
                return name.equals(elem.getName())
                        && namespace.equals(elem.getNamespace());
            }
        }
        return false;
    }

    /**
     * Register the report type with the given name, namespace and class, that
     * can run that report.
     * 
     * @param name
     * @param namespace
     * @param reportClass
     * @return
     * @throws IllegalArgumentException
     *             if either parameter is <code>null</code> or if the given
     *             class does not implement the {@link Report} interface or if
     *             it does not provide an empty constructor.
     */
    public static ReportType register(String name,
                                      Namespace namespace,
                                      Class reportClass) {
        if (name == null || namespace == null || reportClass == null) {
            throw new IllegalArgumentException(
                    "A ReportType cannot be registered with a null name, namespace or report class");
        }

        String key = buildKey(namespace, name);
        if (types.containsKey(key)) {
            return (ReportType) types.get(key);
        } else {
            // test if this report class has an empty constructor and implements
            // Report interface (either directly or through a superclass)
            boolean isValidClass = false;
            Class test = reportClass;
            while (!isValidClass && (test != null)) {
                Class[] interfaces = test.getInterfaces();
                for (int i = 0; i < interfaces.length && !isValidClass; i++) {
                    isValidClass = (interfaces[i] == Report.class);
                }
                test = test.getSuperclass();
            }
            if (!isValidClass) {
                throw new IllegalArgumentException(
                        "The specified report class must implement the Report interface.");
            }

            try {
                reportClass.getConstructor(new Class[0]);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(
                        "The specified report class must provide a default constructor.");
            }

            ReportType type = new ReportType(name, namespace, reportClass);
            types.put(key, type);
            return type;
        }
    }

    /**
     * Return the <code>ReportType</code> requested by the given report info
     * object.
     * 
     * @param reportInfo
     * @return the requested <code>ReportType</code>
     * @throws IllegalArgumentException
     *             if the reportInfo is <code>null</code> or if the requested
     *             report type has not been registered yet.
     */
    public static ReportType getType(ReportInfo reportInfo) {
        if (reportInfo == null) {
            throw new IllegalArgumentException("ReportInfo must not be null.");
        }
        String key = buildKey(reportInfo.getReportElement().getNamespace(),
                reportInfo.getReportElement().getName());
        if (types.containsKey(key)) {
            return (ReportType) types.get(key);
        } else {
            throw new IllegalArgumentException("The request report '" + key
                    + "' has not been registered yet.");
        }
    }

    /**
     * Build the key from the given namespace and name.
     * 
     * @param namespace
     * @param name
     * @return key identifying the report with the given namespace and name
     */
    private static String buildKey(Namespace namespace, String name) {
        return "{" + namespace.getURI() + "}" + name;
    }
}
