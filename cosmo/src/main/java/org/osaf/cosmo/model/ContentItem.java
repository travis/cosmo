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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

/**
 * Extends {@link Item} to represent an abstract
 * content item.
 */
@Entity
@DiscriminatorValue("content")
public abstract class ContentItem extends Item {

    /**
     * 
     */
    private static final long serialVersionUID = 4904755977871771389L;
    
    private String lastModifiedBy = null;
    private Integer lastModification = null;
    private TriageStatus triageStatus = new TriageStatus();
    private Boolean sent = null;
    private Boolean needsReply = null;
    
    public ContentItem() {
    }
    
    @Column(name = "lastmodifiedby", length=255)
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Column(name = "lastmodification")
    public Integer getLastModification() {
        return lastModification;
    }

    public void setLastModification(Integer lastModification) {
        this.lastModification = lastModification;
    }

    @Embedded
    public TriageStatus getTriageStatus() {
        return triageStatus;
    }
  
    public void setTriageStatus(TriageStatus ts) {
        triageStatus = ts;
    }

    @Column(name = "sent")
    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    @Column(name = "needsreply")
    public Boolean getNeedsReply() {
        return needsReply;
    }

    public void setNeedsReply(Boolean needsReply) {
        this.needsReply = needsReply;
    }
    
    @Override
    protected void copyToItem(Item item) {
        if(!(item instanceof ContentItem))
            return;
        
        super.copyToItem(item);
        
        ContentItem contentItem = (ContentItem) item;
        
        contentItem.setLastModifiedBy(getLastModifiedBy());
        contentItem.setLastModification(getLastModification());
        contentItem.setTriageStatus(getTriageStatus());
        contentItem.setSent(getSent());
        contentItem.setNeedsReply(getNeedsReply());
    }

    public static class Action {

        public static final int EDITED = 100;
        public static final int QUEUED = 200;
        public static final int SENT = 300;
        public static final int UPDATED = 400;
        public static final int CREATED = 500;

        public static boolean validate(Integer action) {
            if (action == null)
                return false;
            return (action.intValue() == EDITED ||
                    action.intValue() == QUEUED ||
                    action.intValue() == SENT ||
                    action.intValue() == UPDATED ||
                    action.intValue() == CREATED);
        }
    }
}
