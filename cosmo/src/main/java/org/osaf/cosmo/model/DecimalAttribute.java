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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;

/**
 * Represents attribute with a decimal value.
 */
@Entity
@DiscriminatorValue("decimal")
public class DecimalAttribute extends Attribute implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7830581788843520989L;
    
    @Column(name = "decvalue", precision = 19, scale = 6)
    @Type(type="org.hibernate.type.BigDecimalType")
    private BigDecimal value;

    /** default constructor */
    public DecimalAttribute() {
    }

    public DecimalAttribute(QName qname, BigDecimal value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    public BigDecimal getValue() {
        return this.value;
    }

    public Attribute copy() {
        DecimalAttribute attr = new DecimalAttribute();
        attr.setQName(getQName().copy());
        if(value!=null)
            attr.setValue(new BigDecimal(value.toString()));
        return attr;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setValue(Object value) {
        if (value != null && !(value instanceof BigDecimal))
            throw new ModelValidationException(
                    "attempted to set non BigDecimal value on attribute");
        setValue((BigDecimal) value);
    }

}
