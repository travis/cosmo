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
 * Represents the primary key of an EIM record. Composed of one or
 * more record fields.
 */
public class EimRecordKey {
    private static final Log log = LogFactory.getLog(EimRecordKey.class);

    private EimRecord record;
    private List<EimRecordField> fields;

    /** */
    public EimRecordKey() {
        fields = new ArrayList<EimRecordField>();
    }

    /** */
    public List<EimRecordField> getFields() {
        return fields;
    }

    /** */
    public void addField(EimRecordField field) {
        fields.add(field);
        field.setRecord(record);
    }

    /** */
    public EimRecord getRecord() {
        return record;
    }

    /** */
    public void setRecord(EimRecord record) {
        this.record = record;
    }

    /** */
    public String toString() {
        return ToStringBuilder.
            reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
