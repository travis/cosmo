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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

/**
 * Represents an attribute associated with an Item.
 * An attribute consists of a QName (qualified name)
 * and a value.  The QName is composed from a namespace
 * and a localname.  The QName and Item determine
 * attribute uniqueness.  This means for a given Item
 * and QName, there can be only one attribute.
 * 
 * There are many different types of attributes 
 * (String, Integer, Binary, Boolean, etc.)
 * 
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
// Define a unique constraint on item, namespace, and localname
@Table(name="attribute", uniqueConstraints = {
        @UniqueConstraint(columnNames={"itemid", "namespace", "localname"})})
// Define indexes on discriminator and key fields
@org.hibernate.annotations.Table(
        appliesTo="attribute", 
        indexes={@Index(name="idx_attrtype", columnNames={"attributetype"}),
                 @Index(name="idx_attrname", columnNames={"localname"}),
                 @Index(name="idx_attrns", columnNames={"namespace"})})
@DiscriminatorColumn(
        name="attributetype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Attribute extends AuditableObject implements java.io.Serializable {

    // Fields
    private QName qname;
    private Item item;

    // Constructors
    /** default constructor */
    public Attribute() {
    }

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="namespace", column = @Column(name="namespace", nullable = false, length=255) ),
            @AttributeOverride(name="localName", column = @Column(name="localname", nullable = false, length=255) )
    } )
    public QName getQName() {
        return qname;
    }

    public void setQName(QName qname) {
        this.qname = qname;
    }
    
    /**
     * For backwards compatability.  Return the local name.
     * @return local name of attribute
     */
    @Transient
    public String getName() {
        if(qname==null)
            return null;
        
        return qname.getLocalName();
    }

    /**
     * @return Item attribute belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemid", nullable = false)
    public Item getItem() {
        return item;
    }

    /**
     * @param item
     *            Item attribute belongs to
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * @return the attribute value
     */
    @Transient
    public abstract Object getValue();

    /**
     * @param value
     *            the attribute value
     */
    public abstract void setValue(Object value);

    /**
     * Return a new instance of Attribute containing a copy of the Attribute
     * 
     * @return copy of Attribute
     */
    public abstract Attribute copy();

}
