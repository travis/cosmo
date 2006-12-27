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
package org.osaf.cosmo.mc;

import java.util.Iterator;

import org.osaf.cosmo.eim.EimRecordSet;

/**
 * Bean class that aggregates all of the EIM records for a subscribe
 * or synchronize response and provides the corresponding
 * synchronization token.
 *
 * @see EimRecord
 * @see SyncToken
 */
public class SyncRecords {

    private Iterator<EimRecordSet> recordsets;
    private SyncToken token;

    /** */
    public SyncRecords(Iterator<EimRecordSet> recordsets,
                       SyncToken token) {
        this.recordsets = recordsets;
        this.token = token;
    }

    /** */
    public Iterator<EimRecordSet> getRecordSets() {
        return recordsets;
    }

    /** */
    public SyncToken getToken() {
        return token;
    }
}
