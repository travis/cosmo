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
package org.osaf.cosmo.service.triage;

import java.util.Date;
import java.util.Set;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * Defines API for processing a TriageStatus query.
 * A TriageStatus query is run against a collection and
 * returns all NoteItems that belong to a specified 
 * TriageStatus (NOW,DONE,LATER).  The criteria that
 * determines if a NoteItem belongs to a certain status
 * is complicated and based on different rules depending
 * on the status queried.
 */
public interface TriageStatusQueryProcessor {
    public Set<NoteItem> processTriageStatusQuery(CollectionItem collection,
            String triageStatusLabel,
            Date pointInTime);
}
