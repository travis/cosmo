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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * Represents a Message Stamp.
 */
@Entity
@DiscriminatorValue("message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MessageStamp extends Stamp implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6100568628972081120L;
    
    public static final QName ATTR_MESSAGE_SUBJECT = new QName(
            MessageStamp.class, "subject");
    
    public static final QName ATTR_MESSAGE_TO = new QName(
            MessageStamp.class, "to");
    
    public static final QName ATTR_MESSAGE_CC = new QName(
            MessageStamp.class, "cc");
    
    public static final QName ATTR_MESSAGE_BCC = new QName(
            MessageStamp.class, "bcc");
    
    public static final QName ATTR_MESSAGE_WHO_FROM = new QName(
            MessageStamp.class, "whoFrom");
    
    public static final QName ATTR_MESSAGE_SENDER = new QName(
            MessageStamp.class, "sender");
    
    public static final QName ATTR_MESSAGE_DATE_SENT = new QName(
            MessageStamp.class, "dateSent");
    
    public static final QName ATTR_MESSAGE_IN_REPLY_TO = new QName(
            MessageStamp.class, "inReplyTo");
    
    public static final QName ATTR_MESSAGE_REFERENCES = new QName(
            MessageStamp.class, "references");
    
    /** default constructor */
    public MessageStamp() {
    }
    
    public MessageStamp(Item item) {
        setItem(item);
    }
    
    @Transient
    public String getType() {
        return "message";
    }
    
    // Property accessors
    @Transient
    public String getBcc() {
        // bcc stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_BCC);
    }

    public void setBcc(String bcc) {
        //bcc stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_BCC, bcc);
    }

    @Transient
    public String getCc() {
        // cc stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_CC);
    }

    public void setCc(String cc) {
        // cc stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_CC, cc);
    }

    @Transient
    public String getSubject() {
        // subject stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_SUBJECT);
    }

    public void setSubject(String subject) {
        // subject stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_SUBJECT, subject);
    }

    @Transient
    public String getTo() {
        // to stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_TO);
    }

    public void setTo(String to) {
        // to stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_TO, to);
    }
    
    @Transient
    public String getWhoFrom() {
        // whoFrom stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_WHO_FROM);
    }

    public void setWhoFrom(String whoFrom) {
        // whoFrom stored as TextAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_WHO_FROM, whoFrom);
    }
    
    @Transient
    public String getSender() {
        // sender stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_SENDER);
    }

    public void setSender(String sender) {
        // sender stored as TextAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_SENDER, sender);
    }
    
    @Transient
    public String getInReplyTo() {
        // inReployTo stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO);
    }

    public void setInReplyTo(String inReplyTo) {
        // inReployTo stored as TextAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO, inReplyTo);
    }
    
    @Transient
    public String getDateSent() {
        // inReployTo stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_DATE_SENT);
    }

    public void setDateSent(String dateSent) {
        // inReployTo stored as TextAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_DATE_SENT, dateSent);
    }
    
    @Transient
    public String getReferences() {
        // references stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_REFERENCES);
    }

    public void setReferences(String references) {
        // references stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
    }

    /**
     * Return MessageStamp from Item
     * @param item
     * @return MessageStamp from Item
     */
    public static MessageStamp getStamp(Item item) {
        return (MessageStamp) item.getStamp(MessageStamp.class);
    }
    
    public Stamp copy(Item item) {
        MessageStamp stamp = new MessageStamp();
        stamp.setSubject(getSubject());
        stamp.setTo(getTo());
        stamp.setBcc(getBcc());
        stamp.setCc(getCc());
        stamp.setInReplyTo(getInReplyTo());
        stamp.setReferences(getReferences());
        stamp.setSender(getSender());
        stamp.setWhoFrom(getWhoFrom());
        stamp.setDateSent(getDateSent());
        return stamp;
    }
}
