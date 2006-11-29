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
package org.osaf.cosmo.eim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.MessageStamp;

/**
 * Models an EIM mail message record.
 */
public class MailMessageRecord extends EimRecord {
    private static final Log log = LogFactory.getLog(MailMessageRecord.class);

    private String subject;
    private String to;
    private String cc;
    private String bcc;

    /** */
    public MailMessageRecord() {
    }

    /** */
    public MailMessageRecord(MessageStamp stamp) {
        setUuid(stamp.getItem().getUid());
        subject = stamp.getSubject();
        to = stamp.getTo();
        cc = stamp.getCc();
        bcc = stamp.getBcc();
    }

    /** */
    public void applyTo(MessageStamp stamp) {
        if (! stamp.getItem().getUid().equals(getUuid()))
            throw new IllegalArgumentException("cannot apply record to item with non-matching uuid");

        stamp.setSubject(subject);
        stamp.setTo(to);
        stamp.setCc(cc);
        stamp.setBcc(bcc);
    }

    /** */
    public String getSubject() {
        return subject;
    }

    /** */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /** */
    public String getTo() {
        return to;
    }

    /** */
    public void setTo(String to) {
        this.to = to;
    }

    /** */
    public String getCc() {
        return cc;
    }

    /** */
    public void setCc(String cc) {
        this.cc = cc;
    }

    /** */
    public String getBcc() {
        return bcc;
    }

    /** */
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }
}
