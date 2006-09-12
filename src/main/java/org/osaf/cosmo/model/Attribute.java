/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
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
	 * @param Item attribute belongs to
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
