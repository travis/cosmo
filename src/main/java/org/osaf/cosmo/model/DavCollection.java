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
package org.osaf.cosmo.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A bean encapsulating the information about a dav collection.
 */
public class DavCollection extends DavResource {

    /**
     */
    public DavCollection() {
        super();
    }

//     /**
//      */
//     public boolean equals(Object o) {
//         if (! (o instanceof DavCollection)) {
//             return false;
//         }
//         DavCollection it = (DavCollection) o;
//         return new EqualsBuilder().
//             append(displayName, it.displayName).
//             isEquals();
//     }

//     /**
//      */
//     public int hashCode() {
//         return new HashCodeBuilder(11, 13).
//             append(displayName).
//             toHashCode();
//     }

//     /**
//      */
//     public String toString() {
//         return new ToStringBuilder(this).
//             append("displayName", displayName).
//             toString();
//     }
}
