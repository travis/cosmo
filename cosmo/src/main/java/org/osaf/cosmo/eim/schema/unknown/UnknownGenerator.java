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
package org.osaf.cosmo.eim.schema.unknown;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.schema.BaseGenerator;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates EIM records from item attributes in unknown namespaces.
 */
public class UnknownGenerator extends BaseGenerator {
    private static final Log log =
        LogFactory.getLog(UnknownGenerator.class);

    private static final HashSet<QName> KNOWN_ATTRIBUTES =
        new HashSet<QName>();
    private static final HashSet<String> KNOWN_NAMESPACES =
        new HashSet<String>();

    static {
        KNOWN_NAMESPACES.add(NoteItem.class.getName());
        KNOWN_NAMESPACES.add(MessageStamp.class.getName());
        KNOWN_NAMESPACES.add(CalendarCollectionStamp.class.getName());
        KNOWN_NAMESPACES.add(NS_ITEM);
        KNOWN_NAMESPACES.add(NS_NOTE);
        KNOWN_NAMESPACES.add(NS_EVENT);
        KNOWN_NAMESPACES.add(NS_TASK);
        KNOWN_NAMESPACES.add(NS_MESSAGE);
    }

    /** */
    public UnknownGenerator(Item item) {
        super(null, null, item);
    }

    /**
     * Copies event properties and attributes into a event record.
     */
    public List<EimRecord> generateRecords() {
        // index unknown namespaces
        HashSet<String> idx = new HashSet<String>();
        for (QName qn : getItem().getAttributes().keySet()) {
            if (KNOWN_ATTRIBUTES.contains(qn) ||
                KNOWN_NAMESPACES.contains(qn.getNamespace()))
                continue;
            idx.add(qn.getNamespace());
        }

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();

        int prefix = 1;
        for (String ns : idx) {
            EimRecord record = new EimRecord("pre" + prefix++, ns);
            record.addFields(generateUnknownFields(ns));
            records.add(record);
        }

        return records;
    }
}
