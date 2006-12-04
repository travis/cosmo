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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;


/**
 * Represents a Message Stamp.
 */
@Entity
@DiscriminatorValue("message")
@SecondaryTable(name="message_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MessageStamp extends Stamp implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6100568628972081120L;
    private String subject = null;
    private String to = null;
    private String cc = null;
    private String bcc = null;
    
    /** default constructor */
    public MessageStamp() {
    }
    
    @Transient
    public String getType() {
        return "message";
    }
    
    // Property accessors
    @Column(table="message_stamp", name="msgbcc")
    @Type(type="text")
    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    @Column(table="message_stamp", name="msgcc")
    @Type(type="text")
    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @Column(table="message_stamp", name="msgsubject")
    @Type(type="text")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Column(table="message_stamp", name="msgto")
    @Type(type="text")
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Stamp copy() {
        MessageStamp stamp = new MessageStamp();
        stamp.subject = subject;
        stamp.to = to;
        stamp.bcc = bcc;
        stamp.cc = cc;
        return stamp;
    }
}
