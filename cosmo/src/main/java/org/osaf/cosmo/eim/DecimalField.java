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

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an EIM field whose value is a decimal.
 */
public class DecimalField extends EimRecordField {
    private static final Log log = LogFactory.getLog(DecimalField.class);

    private int digits;
    private int decimalPlaces;

    /** */
    public DecimalField(String name,
                        BigDecimal value) {
        this(name, value,
             value != null ? value.precision() : -1,
             value != null ? value.scale() : -1);
    }

    /** */
    public DecimalField(String name,
                        BigDecimal value,
                        int digits,
                        int decimalPlaces) {
        super(name, value);
        this.digits = digits;
        this.decimalPlaces = decimalPlaces;
    }

    /** */
    public BigDecimal getDecimal() {
        return (BigDecimal) getValue();
    }

    /** */
    public int getDigits() {
        return digits;
    }

    /** */
    public void setDigits() {
        this.digits = digits;
    }

    /** */
    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    /** */
    public void setDecimalPlaces() {
        this.decimalPlaces = decimalPlaces;
    }
}
