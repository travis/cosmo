/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines constants for extended HTTP status codes defined by the
 * Cosmo CMP.
 */
public class CmpConstants {
    private static final Log log = LogFactory.getLog(CmpConstants.class);

    private static HashMap statusCodes = new HashMap();

    /** */
    public static final String MEDIA_TYPE_XML = "text/xml";
    /** */
    public static final String MEDIA_TYPE_PLAIN_TEXT = "text/plain";

    /** */
    public static final int SC_USERNAME_IN_USE = 431;
    /** */
    public static final String RP_USERNAME_IN_USE = "Username In Use";

    /** */
    public static final int SC_EMAIL_IN_USE = 432;
    /** */
    public static final String RP_EMAIL_IN_USE = "Email In Use";
	
	/** */
    public static final String USER_LIST_PATH = "/cmp/users";

    static {
        addStatusCode(SC_USERNAME_IN_USE, RP_USERNAME_IN_USE);
        addStatusCode(SC_EMAIL_IN_USE, RP_EMAIL_IN_USE);
    }

    private static void addStatusCode(int statusCode, String reasonPhrase) {
        statusCodes.put(new Integer(statusCode), reasonPhrase);
    }

    /**
     */
    public static String getReasonPhrase(int statusCode) {
        return (String) statusCodes.get(new Integer(statusCode));
    }
   
}
