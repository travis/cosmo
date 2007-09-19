/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

dojo.provide("cosmotest.datetime.test_serialize");

dojo.require("cosmo.datetime.serialize");

cosmotest.datetime.test_serialize = {
    

    test_fromIso8601: function (){
   
    },

    test_fromIso8601Date: function (){

    },

    test_fromIso8601Time: function (){

    },

    test_fromRfc3339: function (){

    },

    test_addIso8601Duration: function (){

    },

    test_getIso8601Duration: function(){
    
    },
    
    test_durationHashToIso8601: function (){
        var d1 = {year: 1, month: 2, day: 3, hour: 4, minute: 5, second: 6};
        var ds = cosmo.datetime.durationHashToIso8601(d1);
        jum.assertEquals("first duration hash wrong", "P1Y2M3DT4H5M6S", ds);

        var d2 = {year: 1, month: 2, day: 3};
        ds = cosmo.datetime.durationHashToIso8601(d2);
        jum.assertEquals("first duration hash wrong", "P1Y2M3D", ds);

        var d3 = {hour: 4, minute: 5, second: 6};
        ds = cosmo.datetime.durationHashToIso8601(d3);
        jum.assertEquals("first duration hash wrong", "PT4H5M6S", ds);

    },
    
    test_parseIso8601Duration: function(){
        var d1 = {year: 1, month: 2, day: 3, hour: 4, minute: 5, second: 6};
        var d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
          );
        
        cosmotest.datetime.test_serialize.durationsEqual("1", d1, d2);

        // Make sure things are numbers
        jum.assertEquals(2, d2.year + 1);

        d1 = {year: 1, month: 2, day: 3};
        d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
          );
        
        cosmotest.datetime.test_serialize.durationsEqual("2", d1, d2);

        d1 = {hour: 4, minute: 5, second: 6};
        d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
          );
        
        cosmotest.datetime.test_serialize.durationsEqual("3", d1, d2);
        
        d1 = {week:1};
        d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
        );

        cosmotest.datetime.test_serialize.durationsEqual("4", d1, d2);
    },
    
    
    /* Makes sure properties specified in the expected match the actual.
     * Also, makes sure properties not specified in the expected
     * will resolve to boolean false.
     */
    durationsEqual: function(id, d1, d2){
        if (d1.year) jum.assertEquals(id + ": year", d1.year, d2.year);
        else jum.assertFalse(id + ": year", !!d2.year);
        if (d1.month) jum.assertEquals(id + ": month", d1.month, d2.month);
        else jum.assertFalse(id + ": month", !!d2.month);
        if (d1.day) jum.assertEquals(id + ": day", d1.day, d2.day);
        else jum.assertFalse(id + ": day", !!d2.day);
        if (d1.hour) jum.assertEquals(id + ": hour", d1.hour, d2.hour);
        else jum.assertFalse(id + ": hour", !!d2.hour);
        if (d1.minute) jum.assertEquals(id + ": minute", d1.minute, d2.minute);
        else jum.assertFalse(id + ": minute", !!d2.minute);
        if (d1.second) jum.assertEquals(id + ": second", d1.second, d2.second);
        else jum.assertFalse(id + ": second", !!d2.second);
    }
}
