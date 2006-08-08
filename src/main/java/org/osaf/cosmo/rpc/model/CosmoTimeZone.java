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

public class CosmoTimeZone {
    
    private int minutesOffset = 0;
    private String timeZoneID = null;
    private String name = null;
    
    public CosmoTimeZone(){
        
    }
    
    public int getMinutesOffset() {
        return minutesOffset;
    }

    public void setMinutesOffset(int minutesOffset) {
        this.minutesOffset = minutesOffset;
    }

    public String getId() {
        return timeZoneID;
    }

    public void setId(String tzID) {
        this.timeZoneID = tzID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + minutesOffset;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((timeZoneID == null) ? 0 : timeZoneID.hashCode());
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
		final CosmoTimeZone other = (CosmoTimeZone) obj;
		if (minutesOffset != other.minutesOffset)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (timeZoneID == null) {
			if (other.timeZoneID != null)
				return false;
		} else if (!timeZoneID.equals(other.timeZoneID))
			return false;
		return true;
	}

}
