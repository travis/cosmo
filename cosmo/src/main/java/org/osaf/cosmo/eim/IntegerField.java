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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an EIM field whose value is an unsigned 32-bit integer.
 */
public class IntegerField extends EimRecordField {
    private static final Log log = LogFactory.getLog(IntegerField.class);

    /** */
    public IntegerField(String name,
                        Integer value) {
        super(name, value);
    }

    /** */
    public IntegerField(String name,
                        boolean value) {
        super(name, value ? new Integer(1) : new Integer(0));
    }

    /** */
    public Integer getInteger() {
        return (Integer) getValue();
    }
}
