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
 * Represents an EIM field whose value is a sequence of bytes.
 * 
 * A byte value can be at most 1024 bytes long.
 */
public class BytesField extends EimRecordField {
    private static final Log log = LogFactory.getLog(BytesField.class);

    /** */
    public static final int MAX_LENGTH = 1024;

    /**
     * @throws IllegalArgumentException if the value is longer than
     * {@link #MAX_LENGTH}.
     */
    public BytesField(String name,
                      byte[] value) {
        super(name, value);
        if (value.length > MAX_LENGTH)
            throw new IllegalArgumentException("Value is " + value.length + " bytes, exceeding maximum length of " + MAX_LENGTH);
    }

    /** */
    public byte[] getBytes() {
        return (byte[]) getValue();
    }
}
