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

dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");

test_declareStamp = function(){
    cosmo.model.declareStamp("TestStamp", "test",
        [["testString", String, {"default" : "def"}],
         ["testArrayOfNumbers", [Array, Number], {"default" : function(){return [1,2,3]}}]],
        {
            initializer: function(kwArgs){
                this.initializeProperties(kwArgs);
            }});
    
    jum.assertFalse(typeof(TestStamp) == "undefined");
    var s = new TestStamp();
    jum.assertEquals(s.getTestString(), "def");
    jum.assertEquals([1,2,3], s.getTestArrayOfNumbers());
    var attr = s.stampMetaData.getAttribute("testArrayOfNumbers");
    assertEquals(attr.type[0], String);
    assertEquals(attr.type[1], Number);
}

test_addGetRemoveStamp = function(){
};

test_addGetRemoveModification = function(){
};

test_getEventStampGetTaskStamp = function(){
};

test_testNoteInstance = function(){
    var note = new cosmo.model.Note({
        body: "body",
        uid: "123",
        displayName: "display"
    });
    
    var date = new cosmo.datetime.Date(2000,0,1);
    var noteInstance = note.getNoteInstance(date);
    
    jum.assertEquals(note.getBody(), noteInstance.getBody());
    
    noteInstance.setBody("new");
    jum.assertEquals(noteInstance.getBody(), "new");
    
    var mod = note.getModification(date);
    jum.assertTrue(mod != null);
    jum.assertEquals(mod.getModifiedProperties()["body"] == "new");
    
};

test_equals = function(){    var equals = cosmo.model.util.equals;
    jum.assertTrue(equals(1,1));
    var date1 = new cosmo.datetime.Date(2001,1,1);
    var date2 = new cosmo.datetime.Date(2001,1,1);
    jum.assertTrue(equals(date1,date2));
    
    date1.setMonth(2);
    jum.assertFalse(equals(date1,date2));

    date1 = new cosmo.datetime.Date(2001,1,1);
    date2 = new cosmo.datetime.Date(2001,1,1);
    
    jum.assertTrue(equals(date1,date2));
    date1.tzId= "blah";
    jum.assertFalse(equals(date1,date2));
    
    jum.assertFalse(equals(date1, null));
    jum.assertFalse(equals(null, date1));
    var caught = false;
    try {
        equals(1, "1");
    } catch (e){
        caught = true;
    }
    jum.assertTrue(caught);
    
};

test_ = function(){
};

test_ = function(){
};

