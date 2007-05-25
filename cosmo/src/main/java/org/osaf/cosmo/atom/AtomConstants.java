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
package org.osaf.cosmo.atom;

import javax.xml.namespace.QName;

/**
 */
public interface AtomConstants {

    // media type constants

    /** */
    public static final String MEDIA_TYPE_ATOMSVC = "application/atomsvc+xml";
    /** */
    public static final String MEDIA_TYPE_ATOM = "application/atom+xml";
    /** */
    public static final String MEDIA_TYPE_HTML = "text/html";
    /** */
    public static final String MEDIA_TYPE_XML = "text/xml";
    /** */
    public static final String MEDIA_TYPE_TEXT = "text/plain";
    /** */
    public static final String MEDIA_TYPE_URLENCODED =
        "application/x-www-form-urlencoded";

    // link relation constants

    /** */
    public static final String REL_SELF = "self";
    /** */
    public static final String REL_ALTERNATE = "alternate";
    /** */
    public static final String REL_MORSE_CODE = "morse code";
    /** */
    public static final String REL_DAV = "dav";
    /** */
    public static final String REL_WEBCAL = "webcal";
    /** */
    public static final String REL_PARENT = "parent";
    /** */
    public static final String REL_MODIFIES = "modifies";
    /** */
    public static final String REL_MODIFICATION = "modification";

    // projection constants

    /** */
    public static final String PROJECTION_BASIC = "basic";
    /** */
    public static final String PROJECTION_FULL = "full";
    /** */
    public static final String PROJECTION_DETAILS = "details";

    // data format constants

    /** */
    public static final String FORMAT_EIM_JSON = "eim-json";
    /** */
    public static final String FORMAT_EIMML = "eimml";
    /** */
    public static final String FORMAT_HTML = "html";
    /** */
    public static final String FORMAT_TEXT = "text";

    // XML constants

    /** */
    public static final String NS_COSMO = "http://osafoundation.org/cosmo/Atom";
    /** */
    public static final String PRE_COSMO = "cosmo";

    /** */
    public static final QName QN_TICKET =
        new QName(NS_COSMO, "ticket", PRE_COSMO);
    /** */
    public static final QName QN_COLLECTION =
        new QName(NS_COSMO, "collection", PRE_COSMO);
    /** */
    public static final QName QN_TYPE =
        new QName(NS_COSMO, "type", PRE_COSMO);
    /** */
    public static final QName QN_EXISTS =
        new QName(NS_COSMO, "exists", PRE_COSMO);
}
