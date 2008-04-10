/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.util;

import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

/**
 * Utility methods to process hierarchical String 
 * properties.
 *
 */
public class StringPropertyUtils {
    
    /**
     * Get all child keys of a parent key.  For example
     * for the set of keys: a.b, a.c and a.d the set
     * of child keys of a are [b,c,d].
     * @param parent parent key
     * @param keys keys to search
     * @return child keys
     */
    public static String[] getChildKeys(String parent, String[] keys) {
        HashSet<String> children = new HashSet<String>();
        if(!parent.endsWith("."))
            parent = parent + ".";
        for(String key: keys) {
            String end = StringUtils.substringAfter(key, parent);
            if("".equals(end) || end==null)
                continue;
            
            children.add(StringUtils.substringBefore(end, "."));
        }
        
        return children.toArray(new String[0]);
    }
}
