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

/**
 * Represents an attribute connected to an Item.  Items
 * have a map of attributes associated to them.  There
 * are many different types of attributes (String, Integer, etc.)
 * Each attribute has a single Item associated to it.
 */
public abstract class Attribute extends BaseModelObject implements java.io.Serializable {

	// Fields    
	private String name;
	private Item item;

	// Constructors

	/** default constructor */
	public Attribute() {
	}

	
	/**
	 * @return name of attribute
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name name of attribute
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Item attribute belongs to
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @param item attribute belongs to
	 */
	public void setItem(Item item) {
		this.item = item;
	}
	
	/**
	 * @return the attribute value
	 */
	public abstract Object getValue();
    
    /**
     * @param value the attribute value
     */
    public abstract void setValue(Object value);
    
    /**
     * Return a new instance of Attribute 
     * containing a copy of the Attribute
     * @return copy of Attribute
     */
    public abstract Attribute copy();

}
