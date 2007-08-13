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

import java.io.Reader;

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

    public static final String FORMAT_DATE_SENT = "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z";

    /**
     * 
     */
    private static final long serialVersionUID = -6100568628972081120L;
    
    public static final QName ATTR_MESSAGE_ID = new QName(
            MessageStamp.class, "messageId");
    
    public static final QName ATTR_MESSAGE_HEADERS = new QName(
            MessageStamp.class, "headers");
    
    public static final QName ATTR_MESSAGE_FROM = new QName(
            MessageStamp.class, "from");
    
    public static final QName ATTR_MESSAGE_TO = new QName(
            MessageStamp.class, "to");
    
    public static final QName ATTR_MESSAGE_CC = new QName(
            MessageStamp.class, "cc");
    
    public static final QName ATTR_MESSAGE_BCC = new QName(
            MessageStamp.class, "bcc");
    
    public static final QName ATTR_MESSAGE_ORIGINATORS = new QName(
            MessageStamp.class, "originators");
    
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
    public String getMessageId() {
        // id stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_ID);
    }

    public void setMessageId(String id) {
        // id stored as StringAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_ID, id);
        updateTimestamp();
    }
    
    @Transient
    public String getHeaders() {
        // headers stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_HEADERS);
    }

    public void setHeaders(String headers) {
        // headers stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_HEADERS, headers);
        updateTimestamp();
    }
    
    public void setHeaders(Reader headers) {
        // headers stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_HEADERS, headers);
        updateTimestamp();
    }
    
    @Transient
    public String getFrom() {
        // from stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_FROM);
    }

    public void setFrom(String from) {
        // from stored as StringAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_FROM, from);
        updateTimestamp();
    }

    @Transient
    public String getTo() {
        // to stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_TO);
    }

    public void setTo(String to) {
        // to stored as StringAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_TO, to);
        updateTimestamp();
    }
    
    @Transient
    public String getBcc() {
        // bcc stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_BCC);
    }

    public void setBcc(String bcc) {
        //bcc stored as StringAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_BCC, bcc);
        updateTimestamp();
    }

    @Transient
    public String getCc() {
        // cc stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_CC);
    }

    public void setCc(String cc) {
        // cc stored as StringAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_CC, cc);
        updateTimestamp();
    }
 
    @Transient
    public String getOriginators() {
        // originators stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_ORIGINATORS);
    }

    public void setOriginators(String originators) {
        // originators stored as StringAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_ORIGINATORS, originators);
        updateTimestamp();
    }
    
    @Transient
    public String getDateSent() {
        // inReployTo stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_DATE_SENT);
    }

    public void setDateSent(String dateSent) {
        // inReployTo stored as TextAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_DATE_SENT, dateSent);
        updateTimestamp();
    }
    
    @Transient
    public String getInReplyTo() {
        // inReployTo stored as StringAttribute on Item
        return StringAttribute.getValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO);
    }

    public void setInReplyTo(String inReplyTo) {
        // inReployTo stored as TextAttribute on Item
        StringAttribute.setValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO, inReplyTo);
        updateTimestamp();
    }

    @Transient
    public String getReferences() {
        // references stored as TextAttribute on Item
        return TextAttribute.getValue(getItem(), ATTR_MESSAGE_REFERENCES);
    }

    public void setReferences(String references) {
        // references stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
        updateTimestamp();
    }
    
    public void setReferences(Reader references) {
        // references stored as TextAttribute on Item
        TextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
        updateTimestamp();
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
        stamp.setMessageId(getMessageId());
        stamp.setTo(getTo());
        stamp.setBcc(getBcc());
        stamp.setCc(getCc());
        stamp.setInReplyTo(getInReplyTo());
        stamp.setReferences(getReferences());
        stamp.setFrom(getFrom());
        stamp.setHeaders(getHeaders());
        stamp.setOriginators(getOriginators());
        stamp.setDateSent(getDateSent());
        return stamp;
    }
}
