/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.eim.schema.modifiedby;

/**
 * Constants related to the modifiedby schema.
 */
public interface ModifiedByConstants {
    /** */
    public static final String FIELD_TIMESTAMP = "timestamp";
    /** */
    public static final String FIELD_USERID = "userid";
    /** */
    public static final int MAXLEN_USERID = 256;
    /** */
    public static final String FIELD_ACTION = "action";
}
