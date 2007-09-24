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
package org.osaf.cosmo.service.freebusy;

import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * Defines the api for free-busy queries.
 */
public interface FreeBusyQueryProcessor {
    
    /**
     * Generate a VFREEBUSY component containing freebusy 
     * periods for a collection.
     * @param collection collection to query
     * @param period time range to query freebusy information
     * @return VFREEBUSY component containing freebusy periods
     */
    public VFreeBusy generateFreeBusy(CollectionItem collection, Period period);

    /**
     * Generate a VFREEBUSY component containing freebusy 
     * periods for an item.
     * @param item item to query
     * @param period time range to query freebusy information
     * @return VFREEBUSY component containing freebusy periods
     */
    public VFreeBusy generateFreeBusy(NoteItem item, Period period);
}
