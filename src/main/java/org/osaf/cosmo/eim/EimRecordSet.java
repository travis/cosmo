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
package org.osaf.cosmo.eim;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a set of EIM records that are related to the same
 * logical entity.
 * <p>
 * All records in a set are associated with the same item as
 * represented by the set's uuid.
 * <p>
 * A recordset may be marked as "deleted", representing an entity
 * that has been removed from storage.
 */
public class EimRecordSet {
    private static final Log log = LogFactory.getLog(EimRecordSet.class);

    private String uuid;
    private ArrayList<EimRecord> records;
    private boolean deleted = false;

    /** */
    public EimRecordSet() {
        records = new ArrayList<EimRecord>();
    }

    /** */
    public String getUuid() {
        return uuid;
    }

    /** */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** */
    public List<EimRecord> getRecords() {
        return records;
    }

    /** */
    public void addRecord(EimRecord record) {
        records.add(record);
        record.setRecordSet(this);
    }

    /** */
    public void addRecords(List<EimRecord> records) {
        for (EimRecord r : records)
            addRecord(r);
    }

    /** */
    public boolean isDeleted() {
        return deleted;
    }

    /** */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /** */
    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
