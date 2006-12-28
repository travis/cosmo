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
package org.osaf.cosmo.eim.schema.contentitem;

/**
 * Constants related to the content item schema.
 *
 * @see ContentItem
 */
public interface ContentItemConstants {
    /** */
    public static final String FIELD_TITLE = "title";
    /** */
    public static final int MAXLEN_TITLE = 256;
    /** */
    public static final String FIELD_TRIAGE_STATUS = "triageStatus";
    /** */
    public static final int MAXLEN_TRIAGE_STATUS = 256;
    /** */
    public static final String FIELD_TRIAGE_STATUS_CHANGED =
        "triageStatusChanged";
    /** */
    public static final int DIGITS_TRIAGE_STATUS_CHANGED = 11;
    /** */
    public static final int DEC_TRIAGE_STATUS_CHANGED = 2;
    /** */
    public static final String FIELD_LAST_MODIFIED_BY = "lastModifiedBy";
    /** */
    public static final int MAXLEN_LAST_MODIFIED_BY = 256;
    /** */
    public static final String FIELD_CREATED_ON = "createdOn";
}
