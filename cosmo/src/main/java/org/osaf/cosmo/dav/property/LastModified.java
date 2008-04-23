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
package org.osaf.cosmo.dav.property;

import java.text.DateFormat;
import java.util.Date;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;

/**
 * Represents the DAV:getlastmodified property.
 */
public class LastModified extends StandardDavProperty {

    private static ThreadLocal<DateFormat> dateFormatLocal = new ThreadLocal<DateFormat>();
    
    public LastModified(Date date) {
        super(DavPropertyName.GETLASTMODIFIED, lm(date), false);
    }

    private static String lm(Date date) {
        // need one DateFormat instance per thread
        DateFormat df = dateFormatLocal.get();
        if(df==null) {
            // modificationDate date format per RFC 1123
            df = new HttpDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            dateFormatLocal.set(df);
        }
            
        if (date == null)
            date = new Date();
        
        return df.format(date);
    }
}
