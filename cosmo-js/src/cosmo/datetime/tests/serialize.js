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

dojo.provide("cosmo.datetime.tests.serialize");

dojo.require("cosmo.tests.jum");
dojo.require("cosmo.env");
dojo.require("cosmo.datetime.timezone.LazyCachingTimezoneRegistry");
dojo.require("cosmo.datetime.serialize");

(function(){
var registry = new cosmo.datetime.timezone.LazyCachingTimezoneRegistry(cosmo.env.getBaseUrl() + "/js/olson-tzdata/");
cosmo.datetime.timezone.setTimezoneRegistry(registry);

/* Makes sure properties specified in the expected match the actual.
 * Also, makes sure properties not specified in the expected
 * will resolve to boolean false.
 */
function assertDurationsEqual(id, d1, d2){
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

doh.register("cosmo.datetime.tests.serialize", [
    function fromICalDate(t){
        var f = cosmo.datetime.fromICalDate;
        var d = f(";VALUE=DATE-TIME:20080515T100000")[0];
        t.is(new Date(2008, 4, 15, 10, 0, 0).getTime(), d.getTime());
        d = f(";VALUE=DATE-TIME;TZID=Pacific/Honolulu:20080515T100000")[0];
        t.is(1210881600000, d.getTime());
        d = f(";VALUE=DATE-TIME;TZID=America/Los_Angeles:20080515T100000")[0];
        t.is(1210870800000, d.getTime());
        d = f(";VALUE=DATE-TIME:20080515T200000Z")[0];
        t.is(1210881600000, d.getTime());
    },

    function fromIso8601(t){
        var fI8 = cosmo.datetime.fromIso8601;
        var d = fI8("20080515T100000")[0];
        t.is(new Date(2008, 4, 15, 10, 0, 0).getTime(), d.getTime());
        d = fI8("20080515T200000Z");
        t.is(1210881600000, d.getTime());
    },

    function fromIso8601Date(){

    },

    function fromIso8601Time(){

    },

    function fromRfc3339(){

    },

    function addIso8601Duration(){

    },

    function getIso8601Duration(){

    },

    function durationHashToIso8601(){
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

    function parseIso8601Duration(){
        var d1 = {year: 1, month: 2, day: 3, hour: 4, minute: 5, second: 6};
        var d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
          );

        assertDurationsEqual("1", d1, d2);

        // Make sure things are numbers
        jum.assertEquals(2, d2.year + 1);

        d1 = {year: 1, month: 2, day: 3};
        d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
          );

        assertDurationsEqual("2", d1, d2);

        d1 = {hour: 4, minute: 5, second: 6};
        d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
          );

        assertDurationsEqual("3", d1, d2);

        d1 = {week:1};
        d2 = cosmo.datetime.parseIso8601Duration(
          cosmo.datetime.durationHashToIso8601(d1)
        );

        assertDurationsEqual("4", d1, d2);
    },

   function dojoFromIso8601(){
       //we monkey-patched dojo.date.fromIso8601 to fix a bug that occurs when
       //parsing dates near DST switchover time. But then we switched over to dojo
       //1.0 which didn't have quite the same function, so we wrote our own.
       //This verifies that this is no longer a problem.
       var string = "20071104T190000Z";
       var jsDate = cosmo.datetime.fromIso8601(string);
       //should be 19, but unpatched gives 20!
       jum.assertEquals("Should be 19", 19,jsDate.getUTCHours());
   },

    function parseIso8601(){
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
    }
]);
})();