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
package org.osaf.cosmo.eim.json;


/**
 * Defines constants for the JSON-EIM data format.
 */
public interface JsonConstants {

    public static final String MEDIA_TYPE_EIM_JSON = "text/eim+json";

    public static final String KEY_UUID = "uuid";
    public static final String KEY_DELETED = "deleted";
    public static final String KEY_DELETED_RECORDS = "deletedRecords";
    public static final String KEY_RECORDS = "records";
    public static final String KEY_NS = "ns";
    public static final String KEY_KEY = "key";
    public static final String KEY_FIELDS = "fields";
    public static final String KEY_MISSING_FIELDS = "missingFields";
    

}
