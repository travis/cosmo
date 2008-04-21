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
package org.osaf.cosmo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Defines server-wide constant attributes.
 */
public class CosmoConstants {

    // cannot be instantiated
    private CosmoConstants() {
    }

    /**
     * The "friendly" name of the product used for casual identification.
     */
    public static final String PRODUCT_NAME = "Chandler Server";

    /**
     * A string identifier for Cosmo used to distinguish it from other
     * software products.
     */
    public static final String PRODUCT_ID =
        "-//Open Source Applications Foundation//NONSGML Chandler Server//EN";

    /**
     * The URL of the Cosmo product web site.
     */
    public static final String PRODUCT_URL =
        "http://cosmo.osafoundation.org/";

    /**
     * The Cosmo release version number.
     */
    public static final String PRODUCT_VERSION;
    // XXX: add build timestamp for snapshots
    
    /**
     * The Cosmo secham version.  This may or may not change when the
     * PRODUCT_VERSION changes.
     */
    public static final String SCHEMA_VERSION = "160";

    /**
     * The servlet context attribute which contains the Cosmo server
     * administrator's email address.
     */
    public static final String SC_ATTR_SERVER_ADMIN = "cosmo.server.admin";

    // read the product version from VERSION_FILE

    private static String VERSION_FILE = "cosmo.version.txt";

    static {
        try {
            Charset utf8 = Charset.forName("UTF-8");
            InputStream in = CosmoConstants.class.getClassLoader().
                getResourceAsStream(VERSION_FILE);
            if (in == null) {
                throw new RuntimeException("can't find " + VERSION_FILE);
            }
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in, utf8));
            PRODUCT_VERSION = reader.readLine();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException("can't load" + VERSION_FILE, e);
        }
    }
}
