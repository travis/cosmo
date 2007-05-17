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


/**
 * Interface that defines methods that should be
 * called on an Item's stamp during the lifecycle of 
 * an Item.  This allows custom code to run
 * for an arbitrary Stamp without having to 
 * update the DAO layer.  This allows us to index
 * EventStamps wihtout having to add custom indexing code
 * into the DAO layer.
 */
public interface StampHandler {
    /**
     * Invoked when a new Item is created
     * @param stamp stamp
     */
    public void onCreateItem(Stamp stamp);
    
    /**
     * Invoked when an existing Item is updated
     * @param stamp stamp
     */
    public void onUpdateItem(Stamp stamp);
    
    
    /**
     * Invoked when an existing Item is deleted.
     * @param stamp stamp
     */
    public void onDeleteItem(Stamp stamp);
}
