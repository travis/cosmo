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

import java.util.Iterator;
import java.util.List;

import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.schema.EimTranslator;
import org.osaf.cosmo.model.Item;

/**
 * Iterator that translates items to EIM records.
 *
 * @see Item
 * @see EimRecord
 */
public class EimRecordTranslationIterator implements Iterator {

    private Iterator<Item> decorated;

    /** */
    public EimRecordTranslationIterator(List<Item> items) {
        decorated = items.iterator();
    }

    /** */
    public boolean hasNext() {
        return decorated.hasNext();
    }

    /** */
    public EimRecordSet next() {
        return new EimTranslator(decorated.next()).generateRecords();
    }

    /** */
    public void remove() {
        throw new UnsupportedOperationException("remove method not supported");
    }
}
