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
package org.osaf.cosmo.model.filter;

import org.osaf.cosmo.model.NoteItem;

/**
 * Adds NoteItem specific criteria to ItemFilter.
 * Matches only NoteItem instances.
 */
public class NoteItemFilter extends ItemFilter {
    public String icalUid = null;
    
    public NoteItemFilter() {}
    
    /**
     * Match notes with a body that matches a given String.
     * @param body body string to match
     */
    public void setBody(String body) {
        TextAttributeFilter filter = (TextAttributeFilter) getAttributeFilter(NoteItem.ATTR_NOTE_BODY);
        if(filter==null) {
            filter = new TextAttributeFilter(NoteItem.ATTR_NOTE_BODY);
            getAttributeFilters().add(filter);
        }
        if(body==null)
            getAttributeFilters().remove(filter);
        else
            filter.setValue(body);
    }

    public String getIcalUid() {
        return icalUid;
    }

    
    /**
     * Match notes with an specific icalUid
     * @param icalUid
     */
    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }
}
