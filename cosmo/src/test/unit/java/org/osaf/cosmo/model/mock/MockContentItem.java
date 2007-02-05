/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.model.mock;

import org.osaf.cosmo.model.ContentItem;



/**
 * Extend <code>ContentItem</code> to be able to set mock db id.
 * This is useful for tests that depend on this id 
 * being set.
 */
public class MockContentItem extends ContentItem {
    Long mockId = new Long(-1);
    Integer mockVersion = new Integer(0);
    
    @Override
    public Long getId() {
        return mockId;
    }
    
    public void setMockId(Long id) {
        mockId = id;
    }
    
    @Override
    public Integer getVersion() {
        return mockVersion;
    }
    
    public void setMockVersion(Integer version) {
        mockVersion = version;
    }
   
}
