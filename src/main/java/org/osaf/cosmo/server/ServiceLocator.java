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
package org.osaf.cosmo.server;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;

/**
 * This class encapsulates the addressing scheme for all client
 * services provided by Cosmo, those protocols and interfaces that
 * allow communication between clients and Cosmo.
 *
 * <h2>Service Addresses</h2>
 *
 * Each service is "mounted" in the server's URL-space at its own
 * prefix relative to the "application mount URL". This URL is
 * composed of the scheme, host, and port information (see RFC 1738),
 * followed by the context path of the Cosmo web application and the
 * servlet path of the protocol or interface.
 * <p>
 * For example, the URL <code>http://localhost:8080/cosmo/dav</code>
 * addresses the Cosmo WebDAV service.
 *
 * <h2>Collection Addresses</h2>
 *
 * Collections in the Cosmo database are addressed similarly
 * regardless of which service is used to access the data. See
 * {@link CollectionPath} for details on the makeup of collection
 * URLs.
 * <p>
 * Note that individual items contained within collections are not
 * addressable at this time.
 *
 * <h2>Other Addresses</h2>
 *
 * Other entities in the Cosmo server are addressable via various
 * services (for example, users in CMP). This class does not specify
 * how those entities are addressed, leaving this job to the service
 * specifications.
 * <p>
 * Similarly, WebDAV provides hierarchical path-based access to
 * collections and items. These WebDAV URLs do not use the collection
 * addressing scheme detailed above but instead use a service-specific
 * scheme that is not interpreted by this class.
 *
 * @see ServiceLocatorFactory
 * @see CollectionPath
 */
public class ServiceLocator implements ServerConstants {
    private static final Log log = LogFactory.getLog(ServiceLocator.class);

    /**
     * The service id for WebDAV
     */
    public static final String SVC_DAV = "dav";
    /**
     * The service id for Atom
     */
    public static final String SVC_ATOM = "atom";
    /**
     * The service id for Morse Code
     */
    public static final String SVC_MORSE_CODE = "mc";
    /**
     * The service id for the Web UI
     */
    public static final String SVC_WEB = "web";
    /**
     * The service id for webcal
     */
    public static final String SVC_WEBCAL = "webcal";

    private static final String PATH_COLLECTION = "collection";

    private String appMountUrl;
    private String ticketKey;
    private ServiceLocatorFactory factory;

    /**
     * Returns a <code>ServiceLocator</code> instance that uses the
     * uses the given application mount URL as the base for all
     * service URLs.
     *
     * @param appMountUrl the application mount URL
     * @param factory the service location factory
     */
    public ServiceLocator(String appMountUrl,
                          ServiceLocatorFactory factory) {
        this(appMountUrl, null, factory);
    }

    /**
     * Returns a <code>ServiceLocator</code> instance that uses the
     * uses the given application mount URL as the base for and
     * includes the given ticket key in all service URLs.
     *
     * @param appMountUrl the application mount URL
     * @param factory the service location factory
     * @param ticketKey the ticket key
     */
    public ServiceLocator(String appMountUrl,
                          String ticketKey,
                          ServiceLocatorFactory factory) {
        this.appMountUrl = appMountUrl;
        this.ticketKey = ticketKey;
        this.factory = factory;
    }

    /**
     * Returns a map of URLs for the collection keyed by service id.
     */
    public Map<String,String> getCollectionUrls(CollectionItem collection) {
        HashMap<String,String> urls = new HashMap<String,String>();
        urls.put(SVC_DAV, getDavUrl(collection));
        urls.put(SVC_ATOM, getAtomUrl(collection));
        urls.put(SVC_MORSE_CODE, getMorseCodeUrl(collection));
        urls.put(SVC_WEB, getWebUrl(collection));
        urls.put(SVC_WEBCAL, getWebcalUrl(collection));
        return urls;
    }

    /**
     * Returns the WebDAV URL of the collection.
     */
    public String getDavUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getDavPrefix());
    }

    /**
     * Returns the Atom URL of the collection.
     */
    public String getAtomUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getAtomPrefix());
    }

    /**
     * Returns the Morse Code URL of the collection.
     */
    public String getMorseCodeUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection,
                                      factory.getMorseCodePrefix());
    }

    /**
     * Returns the Web UI URL of the collection.
     */
    public String getWebUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getWebPrefix());
    }

    /**
     * Returns the webcal URL of the collection.
     */
    public String getWebcalUrl(CollectionItem collection) {
        return calculateCollectionUrl(collection, factory.getWebcalPrefix());
    }

    private String calculateCollectionUrl(CollectionItem collection,
                                          String servicePrefix) {
        StringBuffer buf = new StringBuffer(appMountUrl);

        buf.append(servicePrefix).
            append("/").append(PATH_COLLECTION).
            append("/").append(collection.getUid());

        if (ticketKey != null)
            buf.append("?").
                append(PARAM_TICKET).append("=").append(ticketKey);

        return buf.toString();
    }
}
