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
 * Represents a single EIM record.
 * <p>
 * An EIM record is associated with a particular namespace that allows
 * EIM processors to understand the semantics of the entity modeled by
 * the record. The namespace has an accompanying prefix that can be
 * used as a shorthand for the namespace when serializing the record.
 * <p>
 * A record is composed of 1..n fields representing the data of the
 * record.
 * <p>
 * A record may be marked as "deleted", representing the fact that an
 * aspect of an entity (for example a stamp) has been removed from
 * storage.
 *
 * @see EimRecordField
 */
public class EimRecord {
    private static final Log log = LogFactory.getLog(EimRecord.class);

    private EimRecordSet recordset;
    private String prefix;
    private String namespace;
    private ArrayList<EimRecordField> fields;
    private boolean deleted = false;
    private EimRecordKey key;

    /** */
    public EimRecord() {
        this(null, null);
    }

    /** */
    public EimRecord(String prefix,
                     String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
        fields = new ArrayList<EimRecordField>();
    }

    /** */
    public String getPrefix() {
        return prefix;
    }

    /** */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /** */
    public String getNamespace() {
        return namespace;
    }

    /** */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /** */
    public EimRecordKey getKey() {
        return key;
    }
    
    public void setKey(EimRecordKey key){
        this.key = key;
    }

    /** */
    public List<EimRecordField> getFields() {
        return fields;
    }

    /** */
    public void addKeyField(EimRecordField field) {
        if (key == null) {
            key = new EimRecordKey();
            key.setRecord(this);
        }
        key.addField(field);
    }

    /** */
    public void addField(EimRecordField field) {
        fields.add(field);
        field.setRecord(this);
    }

    /** */
    public void addFields(List<EimRecordField> fields) {
        for (EimRecordField field : fields)
            addField(field);
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
    public EimRecordSet getRecordSet() {
        return recordset;
    }

    /** */
    public void setRecordSet(EimRecordSet recordset) {
        this.recordset = recordset;
    }

    /** */
    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
