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

import java.util.ArrayList;
import java.util.List;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for generators that map to <code>Stamp</code>s.
 *
 * @see Stamp
 */
public abstract class BaseStampGenerator extends BaseGenerator {
    private static final Log log =
        LogFactory.getLog(BaseStampGenerator.class);

    private Stamp stamp;

    /**
     * This class should not be instantiated directly.
     */
    protected BaseStampGenerator(String prefix,
                                 String namespace,
                                 Item item) {
        super(prefix, namespace, item);
    }

    /**
     * Copies the data from a stamp into one or more EIM records if
     * the stamp has been modified since the given timestamp.
     *
     * @param timestamp the number of milliseconds since the epoch, or
     * <code>-1</code> to ignore modification state
     */
    public List<EimRecord> generateRecords(long timestamp) {
        if (timestamp != -1 &&
            stamp != null &&
            stamp.getModifiedDate().getTime() < timestamp)
            return new ArrayList<EimRecord>(0);
        return generateRecords();
    }

    /**
     * Copies the data from a stamp into one or more EIM records
     * regardless of when the stamp was last modified.
     */
    public abstract List<EimRecord> generateRecords();

    /** */
    public Stamp getStamp() {
        return stamp;
    }

    /** */
    protected void setStamp(Stamp stamp) {
        this.stamp = stamp;
    }
}
