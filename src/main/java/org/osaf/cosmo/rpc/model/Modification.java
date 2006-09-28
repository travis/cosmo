package org.osaf.cosmo.rpc.model;

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
public class Modification {
    private CosmoDate instanceDate;
    private String[] modifiedProperties;
    private Event event;
    
    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }
    public CosmoDate getInstanceDate() {
        return instanceDate;
    }
    public void setInstanceDate(CosmoDate instanceDate) {
        this.instanceDate = instanceDate;
    }
    public String[] getModifiedProperties() {
        return modifiedProperties;
    }
    public void setModifiedProperties(String[] modifiedProperties) {
        this.modifiedProperties = modifiedProperties;
    }
    
    
}
