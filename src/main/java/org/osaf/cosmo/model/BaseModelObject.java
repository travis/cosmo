package org.osaf.cosmo.model;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Base class for model objects.
 *
 * @author Brian Moseley
 */
public abstract class BaseModelObject implements Comparable, Serializable {

    /**
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     */
    public int compareTo(Object o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }

    /**
     */
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     */
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
