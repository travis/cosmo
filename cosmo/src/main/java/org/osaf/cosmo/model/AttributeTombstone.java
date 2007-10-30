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
package org.osaf.cosmo.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * When an Attribute is removed from an item, a tombstone is attached
 * to the item to track when this removal ocurred.
 */
@Entity
@DiscriminatorValue("attribute")
public class AttributeTombstone extends Tombstone {
    
    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="namespace", column = @Column(name="namespace", length=255) ),
            @AttributeOverride(name="localName", column = @Column(name="localname", length=255) )
    } )
    private QName qname = null;

    public AttributeTombstone() {
    }
    
    public AttributeTombstone(Item item, Attribute attribute) {
        super(item);
        qname = attribute.getQName();
    }
    
    public AttributeTombstone(Item item, QName qname) {
        super(item);
        this.qname = qname;
    }
    
    public QName getQName() {
        return qname;
    }

    public void setQName(QName qname) {
        this.qname = qname;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AttributeTombstone))
            return false;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(
                qname, ((AttributeTombstone) obj).getQName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(21, 31).appendSuper(super.hashCode())
                .append(qname.hashCode()).toHashCode();
    }
    
    
}
