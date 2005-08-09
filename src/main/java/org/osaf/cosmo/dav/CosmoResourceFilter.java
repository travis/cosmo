/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.dav;

import org.apache.jackrabbit.webdav.simple.DefaultResourceFilter;

/**
 * A {@link org.apache.jackrabbit.webdav.simple.DefaultResourceFilter}
 * that statically defines Cosmo resources to filter out of PROPFIND
 * and directory listing responses.
 */
public class CosmoResourceFilter extends DefaultResourceFilter {
    protected static final String[] FILTERED_PREFIXES =
    { "rep", "jcr", "icalendar", "ticket" };
    
    /**
     */
    public CosmoResourceFilter() {
        super();
        setFilteredPrefixes(FILTERED_PREFIXES);
    }
}
