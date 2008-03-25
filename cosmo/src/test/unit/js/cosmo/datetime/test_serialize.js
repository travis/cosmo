/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
    
   test_dojoFromIso8601: function(){
       //we monkey-patched dojo.date.fromIso8601 to fix a bug that occurs when 
       //parsing dates near DST switchover time. But then we switched over to dojo
       //1.0 which didn't have quite the same function, so we wrote our own. 
       //This verifies that this is no longer a problem.
       var string = "20071104T190000Z";
       var jsDate = cosmo.datetime.fromIso8601(string);
       //should be 19, but unpatched gives 20!
       jum.assertEquals("Should be 19", 19,jsDate.getUTCHours())
   },

    test_parseIso8601: function(){
        var p = cosmo.datetime.util.dateParts;
        
        var tests = [["20000101", 2000, 0, 1],
                     ["20000131", 2000, 0, 31],
                     ["20080229", 2008, 1, 29],
                     ["20000101T000000", 2000, 0, 1],
                     ["20000505T000000", 2000, 4, 5],
                     ["20000101T050505", 2000, 0, 1, 5, 5, 5],
                     ["20000101T050505", 2000, 0, 1, 5, 5, 5],
                     ["20080108T123045", 2008, 0, 8, 12, 30, 45]
                     ];

        for (var i in tests){
            var test = tests[i];
            var dateParts = cosmo.datetime.parseIso8601(test[0]);
            jum.assertEquals(test[0] + " year", test[1], dateParts[p.YEAR]);
            jum.assertEquals(test[0] + " month", test[2], dateParts[p.MONTH]);
            jum.assertEquals(test[0] + " day", test[3], dateParts[p.DAY]);
            jum.assertEquals(test[0] + " hour", test[4] || 0, dateParts[p.HOUR]);
            jum.assertEquals(test[0] + " minute", test[5] || 0, dateParts[p.MINUTE]);
            jum.assertEquals(test[0] + " second", test[6] || 0, dateParts[p.SECOND]);
        }
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
