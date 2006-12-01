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

import java.text.DecimalFormat;

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
    public static final String EL_RECORD = "record"; 
    /** */
    public static final String EL_UUID = "uuid";
    /** */
    public static final String EL_DELETED = "deleted";
    /** */
    public static final String EL_TITLE = "title";
    /** */
    public static final String EL_TRIAGE_STATUS = "triageStatus";
    /** */
    public static final String EL_TRIAGE_STATUS_CHANGED = "triageStatusChanged";
    /** */
    public static final String EL_LAST_MODIFIED_BY = "lastModifiedBy";
    /** */
    public static final String EL_CREATED_ON = "createdOn";
    /** */
    public static final String EL_DTSTART = "dtstart";
    /** */
    public static final String EL_DTEND = "dtend";
    /** */
    public static final String EL_LOCATION = "location";
    /** */
    public static final String EL_RRULE = "rrule";
    /** */
    public static final String EL_EXRULE = "exrule";
    /** */
    public static final String EL_RDATE = "rdate";
    /** */
    public static final String EL_EXDATE = "exdate";
    /** */
    public static final String EL_RECURRENCE_ID = "recurrenceId";
    /** */
    public static final String EL_STATUS = "status";
    /** */
    public static final String EL_BODY = "body";
    /** */
    public static final String EL_ICAL_UID = "icalUid";
    /** */
    public static final String EL_SUBJECT = "subject";
    /** */
    public static final String EL_TO = "to";
    /** */
    public static final String EL_CC = "cc";
    /** */
    public static final String EL_BCC = "bcc";

    /** */
    public static final DecimalFormat DECIMAL_FORMATTER =
        new DecimalFormat("###########.##");
}
