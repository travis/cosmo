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
 * API for hooking into item life cycle.
 */
public interface ItemListener {
    /**
     * Invoked when a new Item is created
     * @param item item created
     */
    public void onCreateItem(Item item);
    
    /**
     * Invoked when an existing Item is updated
     * @param item item updated
     */
    public void onUpdateItem(Item item);
    
    
    /**
     * Invoked when an existing Item is deleted.
     * @param item deleted
     */
    public void onDeleteItem(Item item);
    
    
    /**
     * Invoked when an item is added to a collection.
     * @param item item added
     * @param collection collection
     */
    public void onItemAddedToCollection(Item item, CollectionItem collection); 
    
    
    /**
     * Invoked when an item is removed from a collection
     * @param item item removed
     * @param collection collection
     */
    public void removeItemFromCollection(Item item, CollectionItem collection);
}
