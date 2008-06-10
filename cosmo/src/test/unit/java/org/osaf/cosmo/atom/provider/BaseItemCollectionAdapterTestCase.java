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
package org.osaf.cosmo.atom.provider;

import java.util.Properties;

import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.text.XhtmlCollectionFormat;

/**
 * Base class for for {@link ItemCollectionAdapter} tests.
 */
public abstract class BaseItemCollectionAdapterTestCase extends BaseCollectionAdapterTestCase {
    private static final Log log =
        LogFactory.getLog(BaseItemCollectionAdapterTestCase.class);

    protected BaseCollectionAdapter createAdapter() {
        ItemCollectionAdapter adapter = new ItemCollectionAdapter();
        adapter.setProcessorFactory(helper.getProcessorFactory());
        adapter.setContentService(helper.getContentService());
        return adapter;
    }

    protected Properties serialize(NoteItem item) {
        if (item == null)
            return null;

        Properties props = new Properties();

        props.setProperty("uid", item.getUid() != null ? item.getUid() : "");
        props.setProperty("name", item.getDisplayName() != null ?
                          item.getDisplayName() : "");

        EventStamp es = StampUtils.getEventStamp(item);
        if (es != null)
            props.setProperty("startDate", es.getStartDate().toString());

        return props;
    }

    protected String serialize(CollectionItem collection) {
        if (collection == null)
            return null;

        XhtmlCollectionFormat format = new XhtmlCollectionFormat();
        return format.format(collection);
    }

    protected Properties serialize(VEvent event) {
        if (event == null)
            return null;

        Properties props = new Properties();

        props.setProperty("uid", event.getUid().getValue());
        props.setProperty("name", event.getSummary().getValue());
        props.setProperty("startDate", event.getStartDate().getValue());

        return props;
    }
}
