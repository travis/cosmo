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
package org.osaf.cosmo.eim.schema.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseApplicatorTestCase;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test Case for {@link CollectionApplicator}.
 */
public class CollectionApplicatorTest extends BaseApplicatorTestCase {
    private static final Log log =
        LogFactory.getLog(CollectionApplicatorTest.class);

    public void testApplyField() throws Exception {
        CollectionItem collection = new CollectionItem();

        EimRecord record = makeTestRecord();

        CollectionApplicator applicator = new CollectionApplicator(collection);
        applicator.applyRecord(record);

        checkUnknownValue(record.getFields().get(0), collection);
    }

    private EimRecord makeTestRecord() {
        EimRecord record = new EimRecord(PREFIX_COLLECTION, NS_COLLECTION);

        record.addField(new TextField("Iron Maiden",
                                      "Die With Your Boots On"));

        return record;
    }
}
