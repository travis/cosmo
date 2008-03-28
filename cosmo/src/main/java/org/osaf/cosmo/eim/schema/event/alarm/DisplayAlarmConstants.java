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
package org.osaf.cosmo.eim.schema.event.alarm;

import org.osaf.cosmo.eim.schema.event.EventConstants;

/**
 * Constants related to the display alarm schema.
 *
 * @see EventItem
 */
public interface DisplayAlarmConstants extends EventConstants {
    /** */
    public static final String FIELD_TRIGGER = "trigger";
    /** */
    public static final int MAXLEN_TRIGGER = 64;
    /** */
    public static final String FIELD_DESCRIPTION = "description";
    /** */
    public static final int MAXLEN_DESCRIPTION = 1024;
    /** */
    public static final String FIELD_DURATION = "duration";
    /** */
    public static final int MAXLEN_DURATION = 32;
    /** */
    public static final String FIELD_REPEAT = "repeat";
}
