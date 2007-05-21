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

dojo.provide("cosmotest.model.test_delta");
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");


dojo.lang.mixin(cosmotest.model.test_delta, {
    test_simpleDelta : function(){
        var note = getSimpleEventNote();
        var delta = new cosmo.model.Delta(note);
        delta.addProperty("body", "body");
        delta.addProperty("displayName", "new");
        assertEquals("new", delta.getProperty("displayName"));
        assertFalse(delta.getProperty("body") == undefined);

        delta.deltafy();

        assertEquals("new", delta.getProperty("displayName"));
        assertTrue(delta.getProperty("body") == undefined);
    }
});

function getSimpleEventNote(){
    var note = new cosmo.model.Note({
        body: "body",
        uid: "123",
        displayName: "display"
    });

    var eventStamp = note.getEventStamp(true);
    return note;
}
