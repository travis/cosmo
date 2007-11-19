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
package org.osaf.cosmo.eim.schema.contentitem;

import java.math.BigDecimal;
import java.util.Calendar;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseApplicatorTestCase;
import org.osaf.cosmo.eim.schema.text.TriageStatusFormat;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.model.mock.MockNoteItem;
import org.osaf.cosmo.model.mock.MockTriageStatus;

/**
 * Test Case for {@link ContentItemApplicator}.
 */
public class ContentItemApplicatorTest extends BaseApplicatorTestCase
    implements ContentItemConstants {
    private static final Log log =
        LogFactory.getLog(ContentItemApplicatorTest.class);

    public void testApplyField() throws Exception {
        ContentItem contentItem = new MockNoteItem();

        EimRecord record = makeTestRecord();

        ContentItemApplicator applicator =
            new ContentItemApplicator(contentItem);
        applicator.applyRecord(record);

        checkTextValue(record.getFields().get(0),
                       contentItem.getDisplayName());
        checkTextValue(record.getFields().get(1),
                       TriageStatusFormat.getInstance(new MockEntityFactory()).
                       format(contentItem.getTriageStatus()));
        checkBooleanValue(record.getFields().get(2), contentItem.getSent());
        checkBooleanValue(record.getFields().get(3),
                          contentItem.getNeedsReply());
        checkTimeStampValue(record.getFields().get(4),
                            contentItem.getClientCreationDate());
        checkUnknownValue(record.getFields().get(5), contentItem);
    }
    
    public void testApplyMissingField() throws Exception {
        NoteItem modification = new MockNoteItem();
        NoteItem parent = new MockNoteItem();
        parent.setDisplayName("test");
        modification.setModifies(parent);

        EimRecord record = makeTestMissingRecord();

        ContentItemApplicator applicator =
            new ContentItemApplicator(modification);
        applicator.applyRecord(record);

        Assert.assertNull(modification.getDisplayName());
    }

    private EimRecord makeTestRecord() {
        EimRecord record = new EimRecord(PREFIX_ITEM, NS_ITEM);

        record.addField(new TextField(FIELD_TITLE, "The Bangs"));

        TriageStatus ts = new MockTriageStatus();
        ts.setCode(TriageStatus.CODE_DONE);
        ts.setRank(new BigDecimal("-12345.67"));
        ts.setAutoTriage(Boolean.TRUE);
        record.addField(new TextField(FIELD_TRIAGE,
                                      TriageStatusFormat.getInstance(new MockEntityFactory()).
                                      format(ts)));

        record.addField(new IntegerField(FIELD_HAS_BEEN_SENT, new Integer(1)));
        record.addField(new IntegerField(FIELD_NEEDS_REPLY, new Integer(0)));
        BigDecimal createdOn =
            new BigDecimal(Calendar.getInstance().getTime().getTime());
        record.addField(new DecimalField(FIELD_CREATED_ON, createdOn));
        record.addField(new TextField("Phish", "The Lizzards"));

        return record;
    }
    
    private EimRecord makeTestMissingRecord() {
        EimRecord record = new EimRecord(PREFIX_ITEM, NS_ITEM);

        addMissingTextField(FIELD_TITLE, record);

        TriageStatus ts = new MockTriageStatus();
        ts.setCode(TriageStatus.CODE_DONE);
        ts.setRank(new BigDecimal("-76543.21"));
        ts.setAutoTriage(Boolean.TRUE);
        record.addField(new TextField(FIELD_TRIAGE,
                                      TriageStatusFormat.getInstance(new MockEntityFactory()).
                                      format(ts)));

        record.addField(new IntegerField(FIELD_HAS_BEEN_SENT, new Integer(0)));
        record.addField(new IntegerField(FIELD_NEEDS_REPLY, new Integer(1)));
        BigDecimal createdOn =
            new BigDecimal(Calendar.getInstance().getTime().getTime());
        record.addField(new DecimalField(FIELD_CREATED_ON, createdOn));
        record.addField(new TextField("Phish", "The Lizzards"));

        return record;
    }
}
