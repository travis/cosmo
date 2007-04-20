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