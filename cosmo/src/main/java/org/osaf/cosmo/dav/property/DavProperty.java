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

import org.osaf.cosmo.dav.ExtendedDavConstants;

/**
 * <p>
 * Extends the jcr-server DavProperty interface.
 * </p>
 */
public interface DavProperty
    extends org.apache.jackrabbit.webdav.property.DavProperty,
    ExtendedDavConstants {

    /**
     * <p>
     * Returns the text content of the property value as a string. The string
     * is calculated by concatening the text and character data content of
     * every element in the value.
     * </p>
     */
    public String getValueText();

    /**
     * <p>
     * Returns the language of the property value's text content as specified
     * by the <code>xml:lang</code> attribute. Example: <code>en_US</code>.
     * </p>
     */
    public String getLanguage();
}
