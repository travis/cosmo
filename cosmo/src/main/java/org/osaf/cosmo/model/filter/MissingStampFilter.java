/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.model.filter;

import org.osaf.cosmo.model.Stamp;


/**
 * Filter that matches items that do not
 * have a specified Stamp.  For example you can use
 * this filter to match items that are NOT events,
 * meaning they don't have an EventStamp.
 */
public class MissingStampFilter extends StampFilter {

    private Class missingStampClass = null;
   
    public MissingStampFilter() {}
    
    public MissingStampFilter(Stamp stamp) {
        this.missingStampClass = stamp.getClass();
    }
    
    public MissingStampFilter(Class clazz) {
        this.missingStampClass = clazz;
    }
    
    public Class getMissingStampClass() {
        return missingStampClass;
    }

    public void setMissingStampClass(Class missingStampClass) {
        this.missingStampClass = missingStampClass;
    }

    @Override
    public String getType() {
        return "missingstamp";
    }
}
