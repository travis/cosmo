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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.StampTombstone;
import org.osaf.cosmo.model.Tombstone;

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
        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        // if stamp doesn't exist, check tombstones
        if(stamp==null) {
            // only check tombstones if timestamp is present
            if(timestamp!=-1)
                checkForTombstones(records, timestamp);
            return records;
        }
        
        if (stamp.getModifiedDate().getTime() < timestamp)
            // the stamp has not changed since the given time
            return records;
       
        // the stamp has changed since the given time
        addRecords(records);
        return records;
    }

    /**
     * Adds one or more records representing the data from an active
     * stamp. Called by {@link #generateRecords(long)} when returning
     * records for an active timestamp.
     */
    protected abstract void addRecords(List<EimRecord> records);

    /**
     * Adds key fields to the record. May be called by
     * {@link #generateRecords(long)} when returning a deleted record
     * for an inactive timestamp. May also be used in implementations
     * of {@link #addRecords(List<EimRecord>)}.
     */
    protected abstract void addKeyFields(EimRecord record);

    /** */
    public Stamp getStamp() {
        return stamp;
    }
    
    /**
     * In order for the generator to determine if a stamp has been
     * deleted, it needs to know the stamp types to search for in
     * the set of tombstones for the item.  The reason there could
     * be multiple types is EventStamp and EventExceptionStamp are
     * both handled by the same generator.  In most cases this will
     * return a single value.
     * @return set of stamp types that this generator encompasses
     */
    protected abstract Set<String> getStampTypes();

    /** */
    protected void setStamp(Stamp stamp) {
        this.stamp = stamp;
    }
    
    /**
     * Need to override comparing attribute from stamp to parent stamp.
     */
    @Override
    protected boolean isMissingAttribute(String attribute) {
        if (!isModification())
            return false;

        Stamp modStamp = getStamp();
       
        return isMissingAttribute(attribute, modStamp);
    }
    
    
    /**
     * Search the current Item for StampTombstones that much the
     * generator's stamp types.  If a match is found and the timestamp
     * on the tombstone is more recent than the given timestamp, add
     * a deletion record to the recordset.
     * @param records recordset to add deletion record
     * @param timestamp timestamp to compare against
     */
    protected void checkForTombstones(ArrayList<EimRecord> records, long timestamp) {
        // check for StampTombstone
        for(Iterator<Tombstone> it = getItem().getTombstones().iterator(); it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof StampTombstone) {
                if( getStampTypes().contains(((StampTombstone) ts).getStampType())) {
                   
                    // Ignore tombstones for subscribes, because subscribes don't
                    // need to know about past deletions
                    if(timestamp==-1)
                        return;

                    // Ignore if Tombstone occured before last timestamp
                    if(ts.getTimestamp().getTime()< timestamp)
                        return;

                    // the stamp has been deleted since the given time
                    EimRecord record = new EimRecord(getPrefix(), getNamespace());
                    addKeyFields(record);
                    record.setDeleted(true);
                    records.add(record);
                }
            }
        }   
    }
}
