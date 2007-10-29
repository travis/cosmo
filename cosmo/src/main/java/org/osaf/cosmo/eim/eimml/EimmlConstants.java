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
package org.osaf.cosmo.eim.eimml;

import javax.xml.namespace.QName;

/**
 * Defines constants for the EIMML data format.
 */
public interface EimmlConstants {

    /** */
    public static final String MEDIA_TYPE_EIMML = "application/eim+xml";

    /** */
    public static final String NS_CORE =
        "http://osafoundation.org/eim/0";
    /** */
    public static final String PRE_CORE = "eim";

    /** */
    public static final String EL_COLLECTION = "collection";
    /** */
    public static final QName QN_COLLECTION =
        new QName(NS_CORE, EL_COLLECTION);
    /** */
    public static final String EL_RECORDSET = "recordset"; 
    /** */
    public static final QName QN_RECORDSET = new QName(NS_CORE, EL_RECORDSET);
    /** */
    public static final String EL_RECORD = "record"; 

    /** */
    public static final String ATTR_NAME = "name";
    /** */
    public static final String ATTR_HUE = "hue";
    /** */
    public static final QName QN_NAME = new QName(ATTR_NAME);
    /** */
    public static final QName QN_HUE = new QName(ATTR_HUE);
    /** */
    public static final String ATTR_UUID = "uuid";
    /** */
    public static final QName QN_UUID = new QName(ATTR_UUID);
    /** */
    public static final String ATTR_DELETED = "deleted";
    /** */
    public static final QName QN_DELETED = new QName(NS_CORE, ATTR_DELETED);
    /** */
    public static final String ATTR_TYPE = "type";
    /** */
    public static final QName QN_TYPE = new QName(NS_CORE, ATTR_TYPE);
    /** */
    public static final String ATTR_KEY = "key";
    /** */
    public static final QName QN_KEY = new QName(NS_CORE, ATTR_KEY);
    /** */
    public static final String ATTR_EMPTY = "empty";
    /** */
    public static final QName QN_EMPTY = new QName(ATTR_EMPTY);
    /** */
    public static final String ATTR_MISSING = "missing";
    /** */
    public static final QName QN_MISSING = new QName(ATTR_MISSING);

    /** */
    public static final String TYPE_BYTES = "bytes";
    /** */
    public static final String TYPE_TEXT = "text";
    /** */
    public static final String TYPE_BLOB = "blob";
    /** */
    public static final String TYPE_CLOB = "clob";
    /** */
    public static final String TYPE_INTEGER = "integer";
    /** */
    public static final String TYPE_DATETIME = "datetime";
    /** */
    public static final String TYPE_DECIMAL = "decimal";
}
