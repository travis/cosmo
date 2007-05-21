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
package org.osaf.cosmo.eim.schema;


/**
 * Defines constants for EIM record types.
 */
public interface EimSchemaConstants {

    /** */
    public static final String NS_ITEM =
        "http://osafoundation.org/eim/item/0";
    /** */
    public static final String PREFIX_ITEM = "item";
    /** */
    public static final String NS_OCCURRENCE_ITEM =
        "http://osafoundation.org/eim/occurrence/0";
    /** */
    public static final String PREFIX_OCCURRENCE_ITEM = "occurrence";
    /** */
    public static final String NS_MODIFIEDBY =
        "http://osafoundation.org/eim/modifiedBy/0";
    /** */
    public static final String PREFIX_MODIFIEDBY = "modby";
    /** */
    public static final String NS_EVENT =
        "http://osafoundation.org/eim/event/0"; 
    /** */
    public static final String PREFIX_EVENT = "event";
    /** */
    public static final String NS_DISPLAY_ALARM =
        "http://osafoundation.org/eim/displayAlarm/0"; 
    /** */
    public static final String PREFIX_DISPLAY_ALARM = "displayAlarm";
    /** */
    public static final String NS_TASK =
        "http://osafoundation.org/eim/task/0";
    /** */
    public static final String PREFIX_TASK = "task";
    /** */
    public static final String NS_MESSAGE =
        "http://osafoundation.org/eim/mail/0";
    /** */
    public static final String PREFIX_MESSAGE = "mail";
    /** */
    public static final String NS_NOTE =
        "http://osafoundation.org/eim/note/0";
    /** */
    public static final String PREFIX_NOTE = "note";

    /** */
    public static final String FIELD_UUID = "uuid";

    /** */
    public static final int DIGITS_TIMESTAMP = 20;
    /** */
    public static final int DEC_TIMESTAMP = 0;
}
