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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for shared item on server
 */
public abstract class Item extends BaseModelObject {

    private String uid;
    private String name;
    private Date creationDate;
    private Date modifiedDate;
    private Integer version;
    private Map<String, Attribute> attributes = new HashMap<String, Attribute>(0);
    private Set<Ticket> tickets = new HashSet<Ticket>(0);
    
    
    private CollectionItem parent = null;
    private User owner;

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }
    
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }
    
    public void addAttribute(Attribute attribute) {
        attribute.setItem(this);
        attributes.put(attribute.getName(), attribute);
    }
    
    public void removeAttribute(String key) {
        if(attributes.containsKey(key))
            attributes.remove(key);
    }

    public Attribute getAttribute(String key) {
        return attributes.get(key);
    }
    
    public Object getAttributeValue(String key) {
        Attribute attr = attributes.get(key);
        if (attr == null)
            return attr;
        return attr.getValue();
    }

    public void addStringAttribute(String key, String value) {
        addAttribute(new StringAttribute(key, value));
    }
    
    public void addIntegerAttribute(String key, Long value) {
        addAttribute(new IntegerAttribute(key, value));
    }
    
    public void addDateAttribute(String key, Date value) {
        addAttribute(new DateAttribute(key, value));
    }
    
    public void addMultiValueStringAttribute(String key, Set<String> value) {
        addAttribute(new MultiValueStringAttribute(key, value));
    }
    
    public void addDictionaryAttribute(String key, Map<String, String> value) {
        addAttribute(new DictionaryAttribute(key, value));
    }
    
    public void setAttribute(String key, Object value) {
        Attribute attr = (Attribute) attributes.get(key);
        
        if(attr==null)
        {
            if(value instanceof String)
                attr = new StringAttribute(key, (String) value);
            else if(value instanceof byte[])
                attr = new BinaryAttribute(key, (byte[]) value);
            else if(value instanceof Long)
                attr = new IntegerAttribute(key, (Long) value);
            else if(value instanceof Date)
                attr = new DateAttribute(key, (Date) value);
            else if(value instanceof Set)
                attr = new MultiValueStringAttribute(key, (Set) value);
            else if(value instanceof Map)
                attr = new DictionaryAttribute(key, (Map) value);
            else
                attr = new StringAttribute(key, value.toString());
            addAttribute(attr);
        } else {
            if(value instanceof String)
                ((StringAttribute) attr).setValue((String) value);
            else if(value instanceof byte[])
                ((BinaryAttribute) attr).setValue((byte[]) value);
            else if(value instanceof Long)
                ((IntegerAttribute) attr).setValue((Long) value);
            else if(value instanceof Date)
                ((DateAttribute) attr).setValue((Date) value);
            else if(value instanceof Set)
                ((MultiValueStringAttribute) attr).setValue((Set) value);
            else if(value instanceof Map)
                ((DictionaryAttribute) attr).setValue((Map) value);
            else
                ((StringAttribute) attr).setValue(value.toString());
        }
    }

    private void setAttributes(Map<String, Attribute> attributes) {
        this.attributes = attributes;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CollectionItem getParent() {
        return parent;
    }

    public void setParent(CollectionItem parent) {
        this.parent = parent;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    private void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }
}
