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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.ServerConstants;
import org.osaf.cosmo.server.ServiceLocator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A resource listing the entry point URLs for clients to access the
 * various protocols and interfaces provided by the server.
 */
public class UserServiceResource
    implements CmpResource, OutputsXml, AtomConstants, EimmlConstants,
               ICalendarConstants, ServerConstants {
    private static final Log log =
        LogFactory.getLog(UserServiceResource.class);

    private static final String EL_SERVICE = "service";
    private static final String EL_USERNAME = "username";
    private static final String EL_LINK = "link";
    private static final String EL_BASE = "base";
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_HREF = "href";

    /** */
    public static final Namespace NS_USER_SERVICE =
        Namespace.getNamespace(null, NS_CMP.getURI() + "/userService");

    private User user;
    private ServiceLocator locator;

    // CmpResource methods

    /**
     * Constructs a resource that represents the given {@link User}.
     */
    public UserServiceResource(User user,
                               ServiceLocator locator) {
        this.user = user;
        this.locator = locator;
    }

    /**
     * Returns the user backing this resource.
     */
    public Object getEntity() {
        return user;
    }

    // OutputsXml methods

    /**
     * Returns an XML representation of the resource in the form of a
     * {@link org.w3c.dom.Element}.
     */
    public Element toXml(Document doc) {
        Element service =
            DomUtil.createElement(doc, EL_SERVICE, NS_USER_SERVICE);

        Element username =
            DomUtil.createElement(doc, EL_USERNAME, null);
        DomUtil.setText(username, user.getUsername());
        service.appendChild(username);

        Element cmpLink = makeLinkElement(doc, SVC_CMP,
                                          CmpConstants.MEDIA_TYPE_XML,
                                          locator.getCmpUrl(user));
        service.appendChild(cmpLink);

        Element mcLink = makeLinkElement(doc, SVC_MORSE_CODE,
                                         CmpConstants.MEDIA_TYPE_XML,
                                         locator.getMorseCodeUrl(user));
        service.appendChild(mcLink);

        Element atomLink = makeLinkElement(doc, SVC_ATOM,
                                           MEDIA_TYPE_ATOMSVC,
                                           locator.getAtomUrl(user));
        service.appendChild(atomLink);

        Element davLink = makeLinkElement(doc, SVC_DAV,
                                          CmpConstants.MEDIA_TYPE_XML,
                                          locator.getDavUrl(user));
        service.appendChild(davLink);

        Element davPrincipalLink =
            makeLinkElement(doc, SVC_DAV_PRINCIPAL,
                            CmpConstants.MEDIA_TYPE_XML,
                            locator.getDavPrincipalUrl(user));
        service.appendChild(davPrincipalLink);

        Element davCalendarHomeLink =
            makeLinkElement(doc, SVC_DAV_CALENDAR_HOME,
                            CmpConstants.MEDIA_TYPE_XML,
                            locator.getDavCalendarHomeUrl(user));
        service.appendChild(davCalendarHomeLink);

        Element atomBase = makeBaseElement(doc, SVC_ATOM, MEDIA_TYPE_ATOM,
                                           locator.getAtomBase());
        service.appendChild(atomBase);

        Element mcBase = makeBaseElement(doc, SVC_MORSE_CODE, MEDIA_TYPE_EIMML,
                                         locator.getMorseCodeBase());
        service.appendChild(mcBase);

        Element pimBase = makeBaseElement(doc, SVC_PIM, MEDIA_TYPE_HTML,
                                          locator.getPimBase());
        service.appendChild(pimBase);

        Element webcalBase = makeBaseElement(doc, SVC_WEBCAL,
                                             ICALENDAR_MEDIA_TYPE,
                                             locator.getWebcalBase());
        service.appendChild(webcalBase);

        return service;
    }

    private Element makeLinkElement(Document doc,
                                    String title,
                                    String type,
                                    String href) {
        return makeLinkElement(doc, EL_LINK, title, type, href);
    }

    private Element makeLinkElement(Document doc,
                                    String name,
                                    String title,
                                    String type,
                                    String href) {
        Element link = DomUtil.createElement(doc, name, null);
        DomUtil.setAttribute(link, ATTR_TITLE, null, title);
        DomUtil.setAttribute(link, ATTR_TYPE, null, type);
        DomUtil.setAttribute(link, ATTR_HREF, null, href);
        return link;
    }

    private Element makeBaseElement(Document doc,
                                    String title,
                                    String type,
                                    String href) {
        return makeLinkElement(doc, EL_BASE, title, type, href);
    }
}
