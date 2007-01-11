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

import java.text.DecimalFormat;

/**
 * Defines constants for EIM record types.
 */
public interface EimSchemaConstants {

    /** */
    public static final String NS_ITEM =
        "http://osafoundation.org/eim/item";
    /** */
    public static final String PREFIX_ITEM = "item";
    /** */
    public static final String NS_EVENT =
        "http://osafoundation.org/eim/event"; 
    /** */
    public static final String PREFIX_EVENT = "event";
    /** */
    public static final String NS_EVENT_MODIFICATION =
        "http://osafoundation.org/eim/eventModification"; 
    /** */
    public static final String PREFIX_EVENT_MODIFICATION = "eventModification";
   /** */
    public static final String NS_TASK =
        "http://osafoundation.org/eim/task";
    /** */
    public static final String PREFIX_TASK = "task";
    /** */
    public static final String NS_MESSAGE =
        "http://osafoundation.org/eim/message";
    /** */
    public static final String PREFIX_MESSAGE = "message";
    /** */
    public static final String NS_NOTE =
        "http://osafoundation.org/eim/note";
    /** */
    public static final String PREFIX_NOTE = "note";
    /** */
    public static final String NS_ICALEXT =
        "http://osafoundation.org/eim/icalext";
    /** */
    public static final String PREFIX_ICALEXT = "icalext";

    /** */
    public static final String FIELD_UUID = "uuid";
}
