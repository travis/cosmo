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
package org.osaf.cosmo.eim.schema.event;

/**
 * Constants related to the event schema.
 *
 * @see EventItem
 */
public interface EventConstants {
    /** */
    public static final String FIELD_DTSTART = "dtstart";
    /** */
    public static final int MAXLEN_DTSTART = 256;
    /** */
    public static final String FIELD_DURATION = "duration";
    /** */
    public static final int MAXLEN_DURATION = 40;
    /** */
    public static final String FIELD_LOCATION = "location";
    /** */
    public static final int MAXLEN_LOCATION = 256;
    /** */
    public static final String FIELD_RRULE = "rrule";
    /** */
    public static final int MAXLEN_RRULE = 1024 * 32;
    /** */
    public static final String FIELD_EXRULE = "exrule";
    /** */
    public static final int MAXLEN_EXRULE = 1024 * 32;
    /** */
    public static final String FIELD_RDATE = "rdate";
    /** */
    public static final int MAXLEN_RDATE = 1024 * 32;
    /** */
    public static final String FIELD_EXDATE = "exdate";
    /** */
    public static final int MAXLEN_EXDATE = 1024 *32;
    /** */
    public static final String FIELD_STATUS = "status";
    /** */
    public static final int MAXLEN_STATUS = 256;
    /** */
    public static final String FIELD_BODY = "body";
    /** */
    public static final String FIELD_DISPLAY_NAME = "displayName";
    /** */
    public static final int MAXLEN_DISPLAY_NAME = 256;
}
