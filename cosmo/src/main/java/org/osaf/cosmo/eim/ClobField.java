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

import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents an EIM field whose value is an arbitrary-length chunk of
 * character data.
 * 
 * No information is provided as to the media type or character
 * encoding of the value.
 */
public class ClobField extends EimRecordField {
    private static final Log log = LogFactory.getLog(ClobField.class);

    /** */
    public ClobField(String name,
                     Reader value) {
        super(name, value);
    }

    /** */
    public Reader getClob() {
        return (Reader) getValue();
    }
}
