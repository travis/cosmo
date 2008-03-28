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
package org.osaf.cosmo.model;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.osaf.cosmo.model.mock.MockEntityFactory;
import org.osaf.cosmo.model.mock.MockNoteItem;

/**
 * Test NoteOccurrenceItem
 */
public class NoteOccurrenceTest extends TestCase {
   
    private EntityFactory factory = new MockEntityFactory();
    
    public void testGenerateNoteOccurrence() throws Exception {
        
        MockNoteItem note = (MockNoteItem) factory.createNote();
        note.setUid("1");
        note.setCreationDate(new Date());
        note.setModifiedDate(new Date());
        note.setDisplayName("dn");
        note.setBody("body");
        note.addStamp(factory.createEventStamp(note));
        
        NoteOccurrence no = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070101"), note);
        NoteOccurrence no2 = NoteOccurrenceUtil.createNoteOccurrence(new net.fortuna.ical4j.model.Date("20070102"), note);
        
        
        Assert.assertEquals("1:20070101", no.getUid());
        Assert.assertEquals(note, no.getMasterNote());
        Assert.assertNotNull(no.getOccurrenceDate());
        
        Assert.assertEquals(note.getCreationDate(), no.getCreationDate());
        Assert.assertEquals("dn", no.getDisplayName());
        Assert.assertEquals("body", no.getBody());
        
        Assert.assertEquals(1, no.getStamps().size());
        
        Assert.assertFalse(no.equals(no2));
        Assert.assertTrue(no.hashCode() != no2.hashCode());
        
        try {
            no.setUid("blah");
            Assert.fail("able to perform unsupported op");
        } catch (UnsupportedOperationException e) {
            
        }
    }    
    
}
