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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

/**
 * Represents a compound triage status value.
 */
@Embeddable
public class TriageStatus {

    /** */
    public static final String LABEL_NOW = "NOW";
    /** */
    public static final String LABEL_LATER = "LATER";
    /** */
    public static final String LABEL_DONE = "DONE";
    /** */
    public static final int CODE_NOW = 100;
    /** */
    public static final int CODE_LATER = 200;
    /** */
    public static final int CODE_DONE = 300;

    private Integer code = null;
    private BigDecimal rank = null;
    private Boolean autoTriage = null;
    
    public TriageStatus() {
    }
   
    @Column(name = "triagestatuscode")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Column(name = "triagestatusrank", precision = 12, scale = 2)
    @Type(type="org.hibernate.type.BigDecimalType")
    public BigDecimal getRank() {
        return rank;
    }

    public void setRank(BigDecimal rank) {
        this.rank = rank;
    }

    @Column(name = "isautotriage")
    public Boolean isAutoTriage() {
        return autoTriage;
    }

    public void setAutoTriage(Boolean autoTriage) {
        this.autoTriage = autoTriage;
    }
        
    public TriageStatus copy() {
        TriageStatus copy = new TriageStatus();
        copy.setCode(code);
        copy.setRank(rank);
        copy.setAutoTriage(autoTriage);
        return copy;
    }

    public String toString() {
        return new ToStringBuilder(this).
            append("code", code).
            append("rank", rank).
            append("autoTriage", autoTriage).
            toString();
    }

    public boolean equals(Object obj) {
        if (! (obj instanceof TriageStatus))
            return false;
        if (this == obj)
            return true;
        TriageStatus ts = (TriageStatus) obj;
        return new EqualsBuilder().
            append(code, ts.code).
            append(rank, ts.rank).
            append(autoTriage, ts.autoTriage).
            isEquals();
    }

    public static TriageStatus createInitialized() {
        TriageStatus ts = new TriageStatus();
        ts.setCode(new Integer(CODE_NOW));
        // XXX there's gotta be a better way!
        String time = (System.currentTimeMillis() / 1000) + ".00";
        ts.setRank(new BigDecimal(time).negate());
        ts.setAutoTriage(Boolean.TRUE);
        return ts;
    }

    public static String label(Integer code) {
        if (code.equals(CODE_NOW))
            return LABEL_NOW;
        if (code.equals(CODE_LATER))
            return LABEL_LATER;
        if (code.equals(CODE_DONE))
            return LABEL_DONE;
        throw new IllegalStateException("Unknown code " + code);
    }

    public static Integer code(String label) {
        if (label.equals(LABEL_NOW))
            return new Integer(CODE_NOW);
        if (label.equals(LABEL_LATER))
            return new Integer(CODE_LATER);
        if (label.equals(LABEL_DONE))
            return new Integer(CODE_DONE);
        throw new IllegalStateException("Unknown label " + label);
    }
}
