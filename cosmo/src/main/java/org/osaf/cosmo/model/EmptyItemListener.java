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
 * Empty ItemListener implementation.  Subclasses can override
 * individual methods.
 */
public class EmptyItemListener implements ItemListener {

    public void onCreateItem(Item item) {
    }

    public void onDeleteItem(Item item) {
    }

    public void onItemAddedToCollection(Item item, CollectionItem collection) {
    }

    public void onUpdateItem(Item item) {
    }

    public void removeItemFromCollection(Item item, CollectionItem collection) {
    }
}
