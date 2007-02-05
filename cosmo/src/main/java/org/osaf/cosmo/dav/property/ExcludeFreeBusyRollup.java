/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.caldav.CaldavConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the Cosmo extended DAV cosmo:exclude-free-busy-rollup
 * property.
 */
public class ExcludeFreeBusyRollup extends AbstractDavProperty
    implements ExtendedDavConstants {

    private boolean flag;

    /**
     */
    public ExcludeFreeBusyRollup(boolean flag) {
        super(EXCLUDEFREEBUSYROLLUP, false);
        this.flag = flag;
    }

    /**
     * Returns a <code>Boolean</code> representing the property's
     * flag.
     */
    public Object getValue() {
        return Boolean.valueOf(flag);
    }
}
