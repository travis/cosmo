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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for an EIM record field.
 *
 * Subclasses are required to provide accesors and mutators for field
 * values with Java types (eg byte[], String, BigDecimal, etc).
 */
public abstract class EimRecordField {
    private static final Log log = LogFactory.getLog(EimRecordField.class);

    private EimRecord record;

    /** */
    public static int BYTES = 1;
    /** */
    public static int TEXT = 2;
    /** */
    public static int LOB = 3;
    /** */
    public static int INTEGER = 4;
    /** */
    public static int DATETIME = 5;
    /** */
    public static int DECIMAL = 6;

    private String name;

    /** */
    protected EimRecordField(String name) {
        this.name = name;
    }

    /** */
    public String getName() {
        return name;
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
