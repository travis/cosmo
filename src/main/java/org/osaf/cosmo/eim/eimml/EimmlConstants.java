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
    public static final String MEDIA_TYPE_EIMML = "text/xml";

    /** */
    public static final String NS_CORE =
        "http://osafoundation.org/eimml/core";
    /** */
    public static final String PRE_CORE = "core";
    /** */
    public static final String NS_COLLECTION =
        "http://osafoundation.org/eimml/collection";
    /** */
    public static final String PRE_COLLECTION = "collection";
    /** */
    public static final String NS_ITEM =
        "http://osafoundation.org/eimml/item";
    /** */
    public static final String PRE_ITEM = "item";
    /** */
    public static final String NS_EVENT =
        "http://osafoundation.org/eimml/event";
    /** */
    public static final String PRE_EVENT = "event";
    /** */
    public static final String NS_TASK =
        "http://osafoundation.org/eimml/task";
    /** */
    public static final String PRE_TASK = "task";
    /** */
    public static final String NS_MESSAGE =
        "http://osafoundation.org/eimml/message";
    /** */
    public static final String PRE_MESSAGE = "message";
    /** */
    public static final String NS_NOTE =
        "http://osafoundation.org/eimml/note";
    /** */
    public static final String PRE_NOTE = "note";

    /** */
    public static final String EL_RECORDS = "records";
    /** */
    public static final QName QN_RECORDS = new QName(NS_CORE, EL_RECORDS);
    /** */
    public static final String EL_RECORDSET = "recordset"; 
    /** */
    public static final QName QN_RECORDSET = new QName(NS_CORE, EL_RECORDSET);
    /** */
    public static final String EL_RECORD = "record"; 

    /** */
    public static final String ATTR_UUID = "uuid";
    /** */
    public static final QName QN_UUID = new QName(NS_CORE, ATTR_UUID);
    /** */
    public static final String ATTR_DELETED = "deleted";
    /** */
    public static final QName QN_DELETED = new QName(NS_CORE, ATTR_DELETED);
    /** */
    public static final String ATTR_TYPE = "type";
    /** */
    public static final QName QN_TYPE = new QName(NS_CORE, ATTR_TYPE);
    /** */
    public static final String ATTR_TRANSFER_ENCODING = "transferEncoding";
    /** */
    public static final QName QN_TRANSFER_ENCODING =
        new QName(NS_CORE, ATTR_TRANSFER_ENCODING);

    /** */
    public static final String TRANSFER_ENCODING_BASE64 = "base64";

    /** */
    public static final String TYPE_BYTES = "bytes";
    /** */
    public static final String TYPE_TEXT = "text";
    /** */
    public static final String TYPE_LOB = "lob";
    /** */
    public static final String TYPE_INTEGER = "integer";
    /** */
    public static final String TYPE_DATETIME = "datetime";
    /** */
    public static final String TYPE_DECIMAL = "decimal";
}
