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
public class Event {
	
	private String id = null;
    private String description = null;
    private CosmoDate start = null;
    private CosmoDate end = null;
    private String title  = null;
    private String status = null;
    private RecurrenceRule recurrenceRule = null;
    private boolean isAllDay = false;
    private boolean isPointInTime = false;
    private boolean isAnyTime = false;
    private boolean masterEvent = false;
    private boolean instance = false;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    /**
     * When the event ends. For all day events this value represents the last day of the all
     * day event.
     * @return
     */
	public CosmoDate getEnd() {
		return end;
	}

	public void setEnd(CosmoDate end) {
		this.end = end;
	}

	public CosmoDate getStart() {
		return start;
	}

    /**
     * When the event starts. If this is an all day or an any-time event, then only the date
     * part (not the time) should be considered.
     * @param start
     */
	public void setStart(CosmoDate start) {
		this.start = start;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
    }

    public boolean isPointInTime() {
        return isPointInTime;
    }

    public void setPointInTime(boolean isPointInTime) {
        this.isPointInTime = isPointInTime;
    }

    public boolean isAnyTime() {
        return isAnyTime;
    }

    public void setAnyTime(boolean isAnyTime) {
        this.isAnyTime = isAnyTime;
    }

    public boolean isInstance() {
        return instance;
    }

    public void setInstance(boolean instance) {
        this.instance = instance;
    }

    public boolean isMasterEvent() {
        return masterEvent;
    }

    public void setMasterEvent(boolean masterEvent) {
        this.masterEvent = masterEvent;
    }

    public RecurrenceRule getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((description == null) ? 0 : description.hashCode());
		result = PRIME * result + ((end == null) ? 0 : end.hashCode());
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		result = PRIME * result + (instance ? 1231 : 1237);
		result = PRIME * result + (isAllDay ? 1231 : 1237);
		result = PRIME * result + (isAnyTime ? 1231 : 1237);
		result = PRIME * result + (isPointInTime ? 1231 : 1237);
		result = PRIME * result + (masterEvent ? 1231 : 1237);
		result = PRIME * result + ((recurrenceRule == null) ? 0 : recurrenceRule.hashCode());
		result = PRIME * result + ((start == null) ? 0 : start.hashCode());
		result = PRIME * result + ((status == null) ? 0 : status.hashCode());
		result = PRIME * result + ((title == null) ? 0 : title.hashCode());
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
		final Event other = (Event) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (instance != other.instance)
			return false;
		if (isAllDay != other.isAllDay)
			return false;
		if (isAnyTime != other.isAnyTime)
			return false;
		if (isPointInTime != other.isPointInTime)
			return false;
		if (masterEvent != other.masterEvent)
			return false;
		if (recurrenceRule == null) {
			if (other.recurrenceRule != null)
				return false;
		} else if (!recurrenceRule.equals(other.recurrenceRule))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
    
}
