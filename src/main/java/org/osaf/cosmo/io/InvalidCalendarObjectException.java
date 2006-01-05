/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.io;

/**
 * An exception indicating that a calendar resource submitted for
 * import did not obey all restrictions specified in by section 4.1 of
 * CalDAV (MUST NOT contain more than one type of calendar component,
 * MUST not specify the iCalendar METHOD property, etc).
 */
public class InvalidCalendarObjectException extends RuntimeException {

    /**
     */
    public InvalidCalendarObjectException(String message) {
        super(message);
    }
}
