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

import java.util.ArrayList;
import java.util.Iterator;

import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.schema.EimRecordTranslationIterator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;

/**
 * Bean class that provides access to the EIM records in a publish
 * request.
 *
 * @see EimRecord
 * @see SyncToken
 */
public class PubRecords {

    private Iterator<EimRecordSet> iterator;
    private String name;

    /** */
    public PubRecords(Iterator<EimRecordSet> iterator,
                      String name) {
        this.iterator = iterator;
        this.name = name;
    }

    /** */
    public Iterator<EimRecordSet> getRecordSets() {
        return iterator;
    }

    /** */
    public String getName() {
        return name;
    }
}
