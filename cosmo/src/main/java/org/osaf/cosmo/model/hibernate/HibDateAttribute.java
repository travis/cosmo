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
package org.osaf.cosmo.model.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.DateAttribute;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;

/**
 * Hibernate persistent DateAtttribute.
 */
@Entity
@DiscriminatorValue("date")
public class HibDateAttribute extends HibAttribute implements
        java.io.Serializable, DateAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 5263977785074085449L;
    
    @Column(name = "datevalue")
    @Type(type="timestamp")
    private Date value;

    /** default constructor */
    public HibDateAttribute() {
    }

    public HibDateAttribute(QName qname, Date value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#getValue()
     */
    public Date getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.DateAttribute#setValue(java.util.Date)
     */
    public void setValue(Date value) {
        this.value = value;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Attribute#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Date))
            throw new ModelValidationException(
                    "attempted to set non Date value on attribute");
        setValue((Date) value);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.hibernate.HibAttribute#copy()
     */
    public Attribute copy() {
        DateAttribute attr = new HibDateAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value.clone());
        return attr;
    }

}
