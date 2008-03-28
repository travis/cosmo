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
package org.osaf.cosmo.eim.schema;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseApplicatorTestCase;
import org.osaf.cosmo.model.Stamp;

/**
 * Test Case for {@link BaseStampApplicator}.
 */
public class BaseStampApplicatorTest extends BaseApplicatorTestCase {
    private static final Log log =
        LogFactory.getLog(BaseStampApplicatorTest.class);

    private static final String PREFIX = "cosmo";
    private static final String NAMESPACE = "cosmo";

    private TestStampApplicator applicator;

    protected void setUp() throws Exception {
        applicator = new TestStampApplicator(PREFIX, NAMESPACE);
    }

    public void testApplyRecordNullNamespace() throws Exception {
        try {
            applicator.applyRecord(new EimRecord(null, null));
            fail("Applied record with null namespace");
        } catch (IllegalArgumentException e) {}
    }

    public void testApplyRecordWrongNamespace() throws Exception {
        try {
            applicator.applyRecord(new EimRecord("bad", "bad"));
            fail("Applied record with null namespace");
        } catch (IllegalArgumentException e) {}
    }

    public void testApplyDeletion() throws Exception {
        EimRecord record = new EimRecord(PREFIX, NAMESPACE);
        TextField field = new TextField("foo", "bar");
        record.addField(field);

        record.setDeleted(true);
        applicator.applyRecord(record);
        assertTrue("Did not apply deletion", applicator.isDeletionApplied());
        assertFalse("Erroneously created stamp", applicator.isStampCreated());
        assertFalse("Erroneously applied field " + field,
                    applicator.isFieldApplied(field));
    }

    public void testApplyRecord() throws Exception {
        EimRecord record = new EimRecord(PREFIX, NAMESPACE);
        TextField field = new TextField("foo", "bar");
        record.addField(field);

        applicator.applyRecord(record);
        assertFalse("Erroneously applied deletion",
                    applicator.isDeletionApplied());
        assertTrue("Did not create stamp", applicator.isStampCreated());
        assertTrue("Did not apply field " + field,
                   applicator.isFieldApplied(field));
    }

    private class TestStampApplicator extends BaseStampApplicator {
        private boolean stampCreated;
        private boolean deletionApplied;
        private HashSet<EimRecordField> appliedFields;

        public TestStampApplicator(String prefix,
                                   String namespace) {
            super(prefix, namespace, null);
            stampCreated = false;
            deletionApplied = false;
            appliedFields = new HashSet<EimRecordField>();
        }

        protected Stamp createStamp(EimRecord record) throws EimSchemaException {
            stampCreated = true;
            return null;
        }

        protected void applyDeletion(EimRecord record)
            throws EimSchemaException {
            deletionApplied = true;
        }

        protected void applyField(EimRecordField field)
            throws EimSchemaException {
            appliedFields.add(field);
        }

        public boolean isDeletionApplied() {
            return deletionApplied;
        }

        public Set<EimRecordField> getAppliedFields() {
            return appliedFields;
        }

        public boolean isFieldApplied(EimRecordField field) {
            return appliedFields.contains(field);
        }

        public boolean isStampCreated() {
            return stampCreated;
        }
    }
}
