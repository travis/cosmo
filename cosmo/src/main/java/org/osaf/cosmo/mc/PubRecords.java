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
package org.osaf.cosmo.mc;

import org.osaf.cosmo.eim.EimRecordSetIterator;

/**
 * Bean class that provides access to the EIM records in a publish
 * request.
 *
 * @see EimRecordSet
 */
public class PubRecords {

    private EimRecordSetIterator iterator;
    private String name;
    private Long hue;

    /** */
    public PubRecords(EimRecordSetIterator iterator,
                      String name, Long hue) {
        this.iterator = iterator;
        this.name = name;
        this.hue = hue;
    }

    /** */
    public EimRecordSetIterator getRecordSets() {
        return iterator;
    }

    /** */
    public String getName() {
        return name;
    }
    
    /** */
    public Long getHue() {
        return hue;
    }
}
