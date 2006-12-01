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
package org.osaf.cosmo.eim;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.ContentItem;

/**
 * Models an EIM item record.
 */
public class ItemRecord extends EimRecord {
    private static final Log log = LogFactory.getLog(ItemRecord.class);

    private String title;
    private String triageStatus;
    private BigDecimal triageStatusChanged;
    private String lastModifiedBy;
    private Date createdOn;

    /** */
    public ItemRecord() {
    }

    /** */
    public ItemRecord(ContentItem item) {
        super(item);
        title = item.getDisplayName();
        triageStatus = item.getTriageStatus();
        triageStatusChanged = item.getTriageStatusUpdated();
        lastModifiedBy = item.getLastModifiedBy();
        createdOn = item.getCreationDate();
    }

    /** */
    public void applyTo(ContentItem item) {
        super.applyTo(item);
        item.setDisplayName(title);
        item.setTriageStatus(triageStatus);
        item.setTriageStatusUpdated(triageStatusChanged);
        item.setLastModifiedBy(lastModifiedBy);
        item.setCreationDate(createdOn);
    }

    /** */
    public String getTitle() {
        return title;
    }

    /** */
    public void setTitle(String title) {
        this.title = title;
    }

    /** */
    public String getTriageStatus() {
        return triageStatus;
    }

    /** */
    public void setTriageStatus(String triageStatus) {
        this.triageStatus = triageStatus;
    }

    /** */
    public BigDecimal getTriageStatusChanged() {
        return triageStatusChanged;
    }

    /** */
    public void setTriageStatusChanged(BigDecimal triageStatusChanged) {
        this.triageStatusChanged = triageStatusChanged;
    }

    /** */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /** */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /** */
    public Date getCreatedOn() {
        return createdOn;
    }

    /** */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
