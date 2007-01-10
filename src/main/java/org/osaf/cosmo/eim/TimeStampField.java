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
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an EIM field whose value is an instant in time.
 *
 * Timestamp is not an actual EIM value type but rather is derived
 * from the decimal value type.
 */
public class TimeStampField extends DecimalField {
    private static final Log log = LogFactory.getLog(TimeStampField.class);

    /** */
    public TimeStampField(String name,
                          Date value) {
        super(name, (value != null ? new BigDecimal(value.getTime()) : null),
              20, 0);
    }

    /** */
    public Date getTimeStamp() {
        if (getValue() == null)
            return null;
        return new Date(getDecimal().longValue());
    }
}
