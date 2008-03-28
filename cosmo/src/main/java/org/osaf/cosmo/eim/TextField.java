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

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an EIM field whose value is a UTF-8 string.
 * 
 * A text value can be at most 32 kilobytes long.
 */
public class TextField extends EimRecordField {
    private static final Log log = LogFactory.getLog(TextField.class);

    /** */
    public static final int MAX_LENGTH = 1024*32;

    /**
     * @throws IllegalArgumentException if the value is longer than
     * {@link #MAX_LENGTH}.
     */
    public TextField(String name,
                     String value) {
        super(name, value);
        if (value != null) {
            try {
                int len = value.getBytes("UTF-8").length;
                if (len > MAX_LENGTH)
                    throw new IllegalArgumentException("Value is " + len + " text, exceeding maximum length of " + MAX_LENGTH);
            } catch (UnsupportedEncodingException e) {
                // won't happen
            }
        }
    }

    /** */
    public String getText() {
        return (String) getValue();
    }
}
