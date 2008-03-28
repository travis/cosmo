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
package org.osaf.cosmo.eim.schema.modifiedby;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseApplicatorTestCase;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test Case for {@link ModifiedByApplicator}.
 */
public class ModifiedByApplicatorTest extends BaseApplicatorTestCase
    implements ModifiedByConstants {
    private static final Log log =
        LogFactory.getLog(ModifiedByApplicatorTest.class);

    public void testApplyRecentModification() throws Exception {
        String origUuid = "deadbeef";
        String origLastModifiedBy = "bcm@osafoundation.org";
        Integer origAction = ContentItem.Action.CREATED;
        Date origModifiedDate = makeDate("2007-01-01T12:00:00");
        ContentItem contentItem = makeTestItem(origUuid,
                                               origLastModifiedBy,
                                               origAction,
                                               origModifiedDate);

        Date updateDate = makeDate("2007-01-02T12:00:00");
        String updateLastModifiedBy = "bcm@maz.org";
        Integer updateAction = ContentItem.Action.EDITED;
        EimRecord record = makeTestRecord(contentItem.getUid(),
                                          updateLastModifiedBy,
                                          updateAction,
                                          updateDate);

        ModifiedByApplicator applicator =
            new ModifiedByApplicator(contentItem);
        applicator.applyRecord(record);

        assertEquals("uid wrongly modified", origUuid, contentItem.getUid());
        assertEquals("lastModifiedBy not modified", updateLastModifiedBy,
                     contentItem.getLastModifiedBy());
        assertEquals("lastModification not modified", updateAction,
                     contentItem.getLastModification());
        assertEquals("clientModifiedDate not modified", updateDate,
                     contentItem.getClientModifiedDate());
    }

    public void testApplyOldModification() throws Exception {
        String origUuid = "deadbeef";
        String origLastModifiedBy = "bcm@osafoundation.org";
        Integer origAction = ContentItem.Action.CREATED;
        Date origModifiedDate = makeDate("2007-01-01T12:00:00");
        ContentItem contentItem = makeTestItem(origUuid,
                                               origLastModifiedBy,
                                               origAction,
                                               origModifiedDate);

        Date updateDate = makeDate("2006-01-02T12:00:00");
        String updateLastModifiedBy = "bcm@maz.org";
        Integer updateAction = ContentItem.Action.SENT;
        EimRecord record = makeTestRecord(contentItem.getUid(),
                                          updateLastModifiedBy,
                                          updateAction,
                                          updateDate);
        ModifiedByApplicator applicator =
            new ModifiedByApplicator(contentItem);
        applicator.applyRecord(record);

        assertEquals("uid wrongly modified", origUuid, contentItem.getUid());
        assertEquals("lastModifiedBy wrongly modified", origLastModifiedBy,
                     contentItem.getLastModifiedBy());
        assertEquals("lastModification wrongly modified", origAction,
                     contentItem.getLastModification());
        assertEquals("clientModifiedDate wrongly modified", origModifiedDate,
                     contentItem.getClientModifiedDate());
    }

    public void testApplyDeleted() throws Exception {
        String origUuid = "deadbeef";
        String origLastModifiedBy = "bcm@osafoundation.org";
        Integer origAction = ContentItem.Action.CREATED;
        Date origModifiedDate = makeDate("2007-01-01T12:00:00");
        ContentItem contentItem = makeTestItem(origUuid,
                                               origLastModifiedBy,
                                               origAction,
                                               origModifiedDate);

        Date updateDate = makeDate("2006-01-02T12:00:00");
        String updateLastModifiedBy = "bcm@maz.org";
        EimRecord record = makeTestDeletedRecord(origUuid);

        ModifiedByApplicator applicator =
            new ModifiedByApplicator(contentItem);
        applicator.applyRecord(record);

        assertEquals("uid wrongly modified", origUuid, contentItem.getUid());
        assertEquals("lastModification wrongly modified", origAction,
                     contentItem.getLastModification());
        assertEquals("lastModifiedBy wrongly modified", origLastModifiedBy,
                     contentItem.getLastModifiedBy());
        assertEquals("clientModifiedDate wrongly modified", origModifiedDate,
                     contentItem.getClientModifiedDate());
    }

    private ContentItem makeTestItem(String uuid,
                                     String lastModifiedBy,
                                     Integer action,
                                     Date modifiedDate) {
        ContentItem contentItem = new MockNoteItem();
        contentItem.setUid(uuid);
        contentItem.setLastModifiedBy(lastModifiedBy);
        contentItem.setLastModification(action);
        contentItem.setClientModifiedDate(modifiedDate);
        return contentItem;
    }

    private EimRecord makeTestRecord(String uuid,
                                     String userid,
                                     Integer action,
                                     Date date) {
        EimRecord record = new EimRecord(PREFIX_MODIFIEDBY, NS_MODIFIEDBY);
        record.addKeyField(new TextField(FIELD_UUID, uuid));
        record.addKeyField(new TextField(FIELD_USERID, userid));
        record.addKeyField(new IntegerField(FIELD_ACTION, action));
        record.addKeyField(new DecimalField(FIELD_TIMESTAMP,
                                            makeTimestamp(date),
                                            DEC_TIMESTAMP, DIGITS_TIMESTAMP));
        return record;
    }

    private EimRecord makeTestDeletedRecord(String uuid) {
        EimRecord record = new EimRecord(PREFIX_MODIFIEDBY, NS_MODIFIEDBY);
        record.addKeyField(new TextField(FIELD_UUID, uuid));
        record.setDeleted(true);
        return record;
    }

    private Date makeDate(String formatted)
        throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(formatted);
    }

    private BigDecimal makeTimestamp(Date date) {
        return new BigDecimal(date.getTime() / 1000);
    }
}
