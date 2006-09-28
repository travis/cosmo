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
package org.osaf.cosmo.rpc.model;


/**
 * @author bobbyrullo
 *
 */
public class RecurrenceRule {
    /**
     * Frequencies for recurrence.
     */
    public static final String FREQUENCY_WEEKLY  = "weekly";
    public static final String FREQUENCY_DAILY   = "daily";
    public static final String FREQUENCY_MONTHLY = "monthly";
    public static final String FREQUENCY_YEARLY  = "yearly";
    public static final String FREQUENCY_BIWEEKLY  = "biweekly";

    
    private String frequency = null;
    private CosmoDate endDate = null;
    private String customRule = null;
    private CosmoDate[] exceptionDates = null; 
    private Modification[] modifications = null;
    
    /**
     * Returns how often this event should be repeated
     * 
     * @return one of the constants beginning with "FREQUENCY_"
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency.
     * 
     * @param frequency must be one of the "FREQUNCY_XXX" constants
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }    

    /**
     * Repeats the event until this date.
     * @return
     */
    public CosmoDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(CosmoDate endDate) {
        this.endDate = endDate;
    }

    /**
     * If this event was created outside of Scooby and has a more complex
     * recurrence rule, then a human-readable string representation is found
     * here
     * @return
     */
    public String getCustomRule() {
        return customRule;
    }
    
    public void setCustomRule(String customRule) {
        this.customRule = customRule;
    }
    
    public CosmoDate[] getExceptionDates() {
        return exceptionDates;
    }

    public void setExceptionDates(CosmoDate[] exceptionDates) {
        this.exceptionDates = exceptionDates;
    }
    
    public Modification[] getModifications() {
        return modifications;
    }

    public void setModifications(Modification[] modifications) {
        this.modifications = modifications;
    }
    
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((customRule == null) ? 0 : customRule.hashCode());
		result = PRIME * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = PRIME * result + ((frequency == null) ? 0 : frequency.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RecurrenceRule other = (RecurrenceRule) obj;
		if (customRule == null) {
			if (other.customRule != null)
				return false;
		} else if (!customRule.equals(other.customRule))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (frequency == null) {
			if (other.frequency != null)
				return false;
		} else if (!frequency.equals(other.frequency))
			return false;
		return true;
	}



    
}
