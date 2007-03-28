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
package org.osaf.cosmo.eim.schema;

import java.util.Iterator;
import java.util.List;

import org.osaf.cosmo.eim.EimException;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.EimRecordSetIterator;
import org.osaf.cosmo.model.ItemTombstone;

/**
 * Iterator that translates item tombstones to EIM recordsets.
 *
 * @see ItemTombstone
 * @see EimRecordSet
 */
public class TombstoneTranslationIterator implements EimRecordSetIterator {

    private Iterator<ItemTombstone> decorated;

    public TombstoneTranslationIterator(List<ItemTombstone> tombstones) {
        this.decorated = tombstones.iterator();
    }

    public boolean hasNext()
        throws EimException {
        return decorated.hasNext();
    }

    public EimRecordSet next()
        throws EimException {
        return new ItemTombstoneTranslator(decorated.next()).generateRecordSet();
    }
}
