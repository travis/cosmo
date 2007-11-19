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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

import org.hibernate.annotations.Target;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.TriageStatus;

/**
 * Hibernate persistent ContentItem.
 */
@Entity
@DiscriminatorValue("content")
public abstract class HibContentItem extends HibItem implements ContentItem {

    /**
     * 
     */
    private static final long serialVersionUID = 4904755977871771389L;
    
    @Column(name = "lastmodifiedby", length=255)
    private String lastModifiedBy = null;
    
    @Column(name = "lastmodification")
    private Integer lastModification = null;
    
    @Embedded
    @Target(HibTriageStatus.class)
    private TriageStatus triageStatus = new HibTriageStatus();
    
    @Column(name = "sent")
    private Boolean sent = null;
    
    @Column(name = "needsreply")
    private Boolean needsReply = null;
    
    public HibContentItem() {
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#getLastModifiedBy()
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#setLastModifiedBy(java.lang.String)
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#getLastModification()
     */
    public Integer getLastModification() {
        return lastModification;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#setLastModification(java.lang.Integer)
     */
    public void setLastModification(Integer lastModification) {
        this.lastModification = lastModification;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#getTriageStatus()
     */
    public TriageStatus getTriageStatus() {
        return triageStatus;
    }
  
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#setTriageStatus(org.osaf.cosmo.model.copy.TriageStatus)
     */
    public void setTriageStatus(TriageStatus ts) {
        triageStatus = ts;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#getSent()
     */
    public Boolean getSent() {
        return sent;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#setSent(java.lang.Boolean)
     */
    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#getNeedsReply()
     */
    public Boolean getNeedsReply() {
        return needsReply;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceContentItem#setNeedsReply(java.lang.Boolean)
     */
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
}
