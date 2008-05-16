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

dojo.provide("cosmotest.model.util.test_util");
dojo.require("cosmo.model.util");

cosmotest.model.util.test_util = {
test_simplePropertyApplicator: function (){
    var app  = cosmo.model.util.simplePropertyApplicator;
    NewClass = null;
    dojo.declare("NewClass", null, {});
    app.initializeClass(NewClass, {"enhanceInitializer": true});
    app.addProperty(NewClass, "numProp", {"default" : 1});
    app.addProperty(NewClass, "stringProp", {"default" : "default"});
    app.addProperty(NewClass, "objProp", {"default" : function(){
        return {prop : "hello"};
    }
    });
    app.addProperty(NewClass, "numPropNoDefault");

    var instance = new NewClass();
    
    
    //test the num prop first
    //make sure the getter and setter exist
    jum.assertFalse(instance["getNumProp"] == null);
    jum.assertFalse(instance["setNumProp"] == null);
    
    //the default is one
    jum.assertEquals(1,instance.getNumProp());
    
    //test setters, getters
    instance.setNumProp(2);
    jum.assertEquals(2,instance.getNumProp());
    instance.setNumProp(0);
    jum.assertEquals(0,instance.getNumProp());
    
    //test the string prop's getters, setters
    jum.assertEquals("default", instance.getStringProp());
    instance.setStringProp("new value");
    jum.assertEquals("new value", instance.getStringProp());
    instance.setStringProp(null);
    jum.assertEquals(null, instance.getStringProp());

    //test the obj prop's getters, setters
    jum.assertEquals("hello", instance.getObjProp()["prop"]);
    instance.setObjProp(null);
    jum.assertEquals(null, instance.getStringProp());
    instance.setObjProp({prop: "new"});    
    jum.assertEquals("new", instance.getObjProp()["prop"]);
    
    var instance2 = new NewClass();
    instance2.getObjProp()["prop"] = "bye";
    var instance3 = new NewClass();
    jum.assertEquals("bye", instance2.getObjProp()["prop"]);
    jum.assertEquals("hello", instance3.getObjProp()["prop"]);
},

test_enhanceclass: function(){
    EnhancedClass = null;
    dojo.declare("EnhancedClass", null, {});
    var app  = cosmo.model.util.simplePropertyApplicator;
    app.enhanceClass(EnhancedClass,
    [  ["numProp", {"default" : 1}],
       ["stringProp", {"default" : "default"}],
       ["objProp", {"default" : function(){
                                    return {prop : "hello"};
                                }
                    }],
       ["numPropNoDefault"]
    ],
    {"enhanceInitializer": true});
    
    var instance = new EnhancedClass();
    jum.assertEquals(1, instance.getNumProp());
    jum.assertEquals("default", instance.getStringProp());
    jum.assertEquals("hello", instance.getObjProp()["prop"]);
    
},

    test_immutable : function(){
        ImmutableClass = null;
        dojo.declare("ImmutableClass", null, {});
        var app  = cosmo.model.util.simplePropertyApplicator;
        app.enhanceClass(ImmutableClass, 
            [  ["stringProp", {"default": "default"}]], 
            {"enhanceInitializer":true, "immutable": true});
        var instance = new ImmutableClass({stringProp:"NoChanges"});
        jum.assertEquals("NoChanges", instance.getStringProp());
        var caught = false;
        try {
            instance.setStringProp("Change!");
        } catch (e){
            caught = true;
        }
        jum.assertTrue(caught);
    }

}
