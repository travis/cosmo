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

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * Represents an item on server.  All
 * content in cosmo extends from Item.
 */
public interface Item extends AuditableObject{

    /**
     * Return all stamps associated with Item.  Use
     * addStamp() and removeStamp() to manipulate set.
     * @return set of stamps associated with Item
     */
    public Set<Stamp> getStamps();

    /**
     * @return Map of Stamps indexed by Stamp type.
     */
    public Map<String, Stamp> getStampMap();

    /**
     * Add stamp to Item
     * @param stamp stamp to add
     */
    public void addStamp(Stamp stamp);

    /**
     * Remove stamp from Item.
     * @param stamp stamp to remove
     */
    public void removeStamp(Stamp stamp);

    /**
     * Get the stamp that corresponds to the specified type
     * @param type stamp type to return
     * @return stamp
     */
    public Stamp getStamp(String type);

    /**
     * Get the stamp that corresponds to the specified class
     * @param clazz class of stamp to return
     * @return stamp
     */
    public Stamp getStamp(Class clazz);

    /**
     * Get all Attributes of Item.  Use addAttribute() and 
     * removeAttribute() to manipulate map.
     * @return
     */
    public Map<QName, Attribute> getAttributes();

    public void addTicket(Ticket ticket);

    public void removeTicket(Ticket ticket);

    public void addAttribute(Attribute attribute);

    /**
     * Remove attribute in default namespace with local name.
     * @param name local name of attribute to remove
     */
    public void removeAttribute(String name);

    /**
     * Remove attribute.
     * @param qname qualifed name of attribute to remove.
     */
    public void removeAttribute(QName qname);

    /**
     * Remove all attributes in a namespace.
     * @param namespace namespace of attributes to remove
     */
    public void removeAttributes(String namespace);

    /**
     * Get attribute in default namespace with local name.
     * @param name local name of attribute
     * @return attribute in default namespace with given name
     */
    public Attribute getAttribute(String name);

    /**
     * Get attribute with qualified name.
     * @param qname qualified name of attribute to retrieve
     * @return attribute with qualified name.
     */
    public Attribute getAttribute(QName qname);

    /**
     * Get attribute value with local name in default namespace
     * @param name local name of attribute
     * @return attribute value
     */
    public Object getAttributeValue(String name);

    /**
     * Get attribute value with qualified name
     * @param qname qualified name of attribute
     * @return attribute value
     */
    public Object getAttributeValue(QName qname);

    /**
     * Add new StringAttribute in default namespace
     * @param name local name of attribute to add
     * @param value String value of attribute to add
     */
    public void addStringAttribute(String name, String value);

    /**
     * Add new StringAttribute
     * @param qname qualified name of attribute to add
     * @param value String value of attribute to add
     */
    public void addStringAttribute(QName qname, String value);

    /**
     * Add new IntegerAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Integer value of attribute to add
     */
    public void addIntegerAttribute(String name, Long value);

    /**
     * Add new IntegerAttribute
     * @param qname qualified name of attribute to add
     * @param value Integer value of attribute to add
     */
    public void addIntegerAttribute(QName qname, Long value);

    /**
     * Add new BooleanAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Boolean value of attribute to add
     */
    public void addBooleanAttribute(String name, Boolean value);

    /**
     * Add new BooleanAttribute
     * @param qname qualified name of attribute to addd
     * @param value Boolean value of attribute to add
     */
    public void addBooleanAttribute(QName qname, Boolean value);

    /**
     * Add new DateAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Date value of attribute to add
     */
    public void addDateAttribute(String name, Date value);

    /**
     * Add new DateAttribute
     * @param qname qualified name of attribute to add
     * @param value Date value of attribute to add
     */
    public void addDateAttribute(QName qname, Date value);

    /**
     * Add new MultiValueStringAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Set value of attribute to add
     */
    public void addMultiValueStringAttribute(String name, Set<String> value);

    /**
     * Add new MultiValueStringAttribute
     * @param qname qualified name of attribute to add
     * @param value Set value of attribute to add
     */
    public void addMultiValueStringAttribute(QName qname, Set<String> value);

    /**
     * Add new DictionaryAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Map value of attribute to add
     */
    public void addDictionaryAttribute(String name, Map<String, String> value);

    /**
     * Add new DictionaryAttribute
     * @param qname qualified name of attribute to add
     * @param value Map value of attribute to add
     */
    public void addDictionaryAttribute(QName qname, Map<String, String> value);

    /**
     * Add new XmlAttribute in default namespace
     * @param name local name of attribute to add
     * @param value Element value of attribute to add
     */
    public void addXmlAttribute(String name, Element value);

    /**
     * Add new XmlAttribute
     * @param qname qualified name of attribute to add
     * @param value Element value of attribute to add
     */
    public void addXmlAttribute(QName qname, Element value);

    /**
     * Set attribute value of attribute with local name in default
     * namespace.
     * @param name local name of attribute
     * @param value value to update attribute
     */
    public void setAttribute(String name, Object value);

    /**
     * Set attribute value attribute with qualified name
     * @param key qualified name of attribute
     * @param value value to update attribute
     */
    @SuppressWarnings("unchecked")
    public void setAttribute(QName key, Object value);

    /**
     * Return Attributes for a given namespace.  Attributes are returned
     * in a Map indexed by the name of the attribute.
     * @param namespace namespace of the Attributes to return
     * @return map of Attributes indexed by the name of the attribute
     */
    public Map<String, Attribute> getAttributes(String namespace);

    public Date getClientCreationDate();

    public void setClientCreationDate(Date clientCreationDate);

    public Date getClientModifiedDate();

    public void setClientModifiedDate(Date clientModifiedDate);

    public String getName();

    public void setName(String name);

    /**
     * @return Item's human readable name
     */
    public String getDisplayName();

    /**
     * @param displayName Item's human readable name
     */
    public void setDisplayName(String displayName);

    public User getOwner();

    public void setOwner(User owner);

    public String getUid();

    public void setUid(String uid);

    public Integer getVersion();

    public Set<CollectionItem> getParents();

    /**
     * Return a single parent.
     * @deprecated
     */
    public CollectionItem getParent();

    public Boolean getIsActive();

    public void setIsActive(Boolean isActive);

    /**
     * Get all Tickets on Item.  
     * @return set of tickets
     */
    public Set<Ticket> getTickets();

    public Set<Tombstone> getTombstones();

    public Item copy();

}