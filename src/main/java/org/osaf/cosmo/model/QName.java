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
package org.osaf.cosmo.model;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Represents a qualified name. A qualified name 
 * consists of a namespace and a local name.
 * 
 * Attribtues are indexed by a qualified name.
 */
@Embeddable
public class QName {
    
    public static final String DEFAULT_NAMESPACE = "";
    
    private String namespace = null;
    private String localName = null;
    
    public QName() {}
    
    /**
     * Create new QName with specified namespace and local name.
     * @param namespace namespace
     * @param localName local name
     */
    public QName(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }
    
    /**
     * Create a new QName with the specified local name.  The namespace
     * is the fully qualified name of the specified class.
     * @param clazz class to generate namespace from
     * @param localName local name
     */
    public QName(Class clazz, String localName) {
        this(clazz.getName(), localName);
    }
    
    /**
     * Create new QName with default namespace and specified local name.
     * @param localName local name
     */
    public QName(String localName) {
        this(DEFAULT_NAMESPACE, localName);
    }
    
    /**
     * Get local name
     * @return local name
     */
    public String getLocalName() {
        return localName;
    }
    
    /**
     * Set local name
     * @param localName local name
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /**
     * Create copy of QName object.
     * @return copy of current QName object
     */
    public QName copy() {
        return new QName(namespace, localName);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(13, 27).
            append(namespace).append(localName).toHashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (! (o instanceof QName)) {
            return false;
        }
        QName it = (QName) o;
        return new EqualsBuilder().
            append(namespace, it.namespace).
            append(localName, it.localName).
            isEquals();
    }
    
}
