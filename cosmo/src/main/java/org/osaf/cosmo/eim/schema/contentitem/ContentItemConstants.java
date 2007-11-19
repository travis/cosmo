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
 * @see MockContentItem
 */
public interface ContentItemConstants {
    /** */
    public static final String FIELD_TITLE = "title";
    /** */
    public static final int MAXLEN_TITLE = 1024;
    /** */
    public static final String FIELD_TRIAGE = "triage";
    /** */
    public static final int MAXLEN_TRIAGE = 256;
    /** */
    public static final String FIELD_HAS_BEEN_SENT = "hasBeenSent";
    /** */
    public static final String FIELD_NEEDS_REPLY = "needsReply";
    /** */
    public static final String FIELD_CREATED_ON = "createdOn";
}
