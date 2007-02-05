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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;

/**
 * Represents an attribute with a date value with
 * no timezone information.
 */
@Entity
@DiscriminatorValue("date")
public class DateAttribute extends Attribute implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5263977785074085449L;
    
    private Date value;

    /** default constructor */
    public DateAttribute() {
    }

    public DateAttribute(QName qname, Date value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    @Column(name = "datevalue")
    @Type(type="timestamp")
    public Date getValue() {
        return this.value;
    }

    public void setValue(Date value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        if (value != null && !(value instanceof Date))
            throw new ModelValidationException(
                    "attempted to set non Date value on attribute");
        setValue((Date) value);
    }
    
    public Attribute copy() {
        DateAttribute attr = new DateAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value.clone());
        return attr;
    }

}
