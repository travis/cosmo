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
package org.osaf.cosmo.model.filter;


/**
 * Adds ContentItem specific criteria to ItemFilter.
 * Matches only ContentItem instances.
 */
public class ContentItemFilter extends ItemFilter {
   
    public static final String ORDER_BY_TRIAGE_STATUS_RANK = "ContentItem.triageStatus.rank";
    
    private Integer triageStatus = null;
    
    public ContentItemFilter() {}

    public Integer getTriageStatus() {
        return triageStatus;
    }

    /**
     * Match ContentItems with a specific triageStatus code.
     * Should be one of:
     * <p>
     *  <code> TriageStatus.CODE_DONE<br/>
     *  TriageStatus.CODE_NOW<br/>
     *  TriageStatus.CODE_LATER</code>
     *  <p>
     *  A value of -1 means match items with no triageStatus.
     * @param triageStatus
     */
    public void setTriageStatus(Integer triageStatus) {
        this.triageStatus = triageStatus;
    }
}
