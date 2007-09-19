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
dojo.provide("cosmotest.datetime.test_date");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.timezone.SimpleTimezoneRegistry");
dojo.require("cosmo.model.common");

//Initialization.
//TODO - once Dojo implements setUp() and tearDown() move this code there.
var registry = new cosmo.datetime.timezone.SimpleTimezoneRegistry(cosmo.env.getBaseUrl() + "/js/lib/olson-tzdata/");
var D = cosmo.datetime.Date;

registry.init(["northamerica"]);
cosmo.datetime.timezone.setTimezoneRegistry(registry);

cosmotest.datetime.test_date = {
test_dateConstructor: function () {
    var dt = null;
    var dtComp = new Date(2006, 9, 23);
    
    // Floating
    dt = new D(2006, 9, 23);
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(dtComp.getTimezoneOffset(), dt.getTimezoneOffset());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6);
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(22, dt.getHours());
    jum.assertEquals(12, dt.getMinutes());
    jum.assertEquals(55, dt.getSeconds());
    jum.assertEquals(6, dt.getMilliseconds());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(dtComp.getTimezoneOffset(), dt.getTimezoneOffset());
    
    dt = new D('10/23/2006');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(dtComp.getTimezoneOffset(), dt.getTimezoneOffset());
    
    dt = new D(1161659575006);
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(dtComp.getTimezoneOffset(), dt.getTimezoneOffset());

    // New York
    dt = new D(2006, 9, 23, 'America/New_York');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals('America/New_York', dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(240, dt.getTimezoneOffset());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6, 'America/New_York');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(22, dt.getHours());
    jum.assertEquals(12, dt.getMinutes());
    jum.assertEquals(55, dt.getSeconds());
    jum.assertEquals(6, dt.getMilliseconds());
    jum.assertEquals('America/New_York', dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(240, dt.getTimezoneOffset());
    
    dt = new D('10/23/2006', 'America/New_York');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals('America/New_York', dt.tzId);
    jum.assertEquals(false, dt.utc);
    jum.assertEquals(240, dt.getTimezoneOffset());
    
    // UTC, by timezone 
    dt = new D(2006, 9, 23, 'Etc/UTC');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals('Etc/UTC', dt.tzId);
    jum.assertEquals(true, dt.utc);
    jum.assertEquals(0, dt.getTimezoneOffset());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6, 'Etc/UTC');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(22, dt.getHours());
    jum.assertEquals(12, dt.getMinutes());
    jum.assertEquals(55, dt.getSeconds());
    jum.assertEquals(6, dt.getMilliseconds());
    jum.assertEquals('Etc/UTC', dt.tzId);
    jum.assertEquals(true, dt.utc);
    jum.assertEquals(0, dt.getTimezoneOffset());
    
    dt = new D('10/23/2006', 'Etc/UTC');
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals('Etc/UTC', dt.tzId);
    jum.assertEquals(true, dt.utc);
    jum.assertEquals(0, dt.getTimezoneOffset());
    
    // UTC, by flag
    dt = new D(2006, 9, 23, null, true);
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(true, dt.utc);
    jum.assertEquals(0, dt.getTimezoneOffset());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6, null, true);
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(22, dt.getHours());
    jum.assertEquals(12, dt.getMinutes());
    jum.assertEquals(55, dt.getSeconds());
    jum.assertEquals(6, dt.getMilliseconds());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(true, dt.utc);
    jum.assertEquals(0, dt.getTimezoneOffset());
    
    dt = new D('10/23/2006', null, true);
    jum.assertEquals(2006, dt.getFullYear());
    jum.assertEquals(9, dt.getMonth());
    jum.assertEquals(23, dt.getDate());
    jum.assertEquals(null, dt.tzId);
    jum.assertEquals(true, dt.utc);
    jum.assertEquals(0, dt.getTimezoneOffset());
},

test_dateGetOffset: function () {
    var dt = null;
    
    dt = new D(2006, 9, 29, 1, 59, 'America/Los_Angeles');
    jum.assertEquals(420, dt.getTimezoneOffset());
    
    dt = new D(2006, 9, 29, 2, 0, 'America/Los_Angeles');
    jum.assertEquals(480, dt.getTimezoneOffset());
},

test_dateSetters: function () {
    var dt = null;
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6);
    dt.setMilliseconds(2112);
    jum.assertEquals(57, dt.getSeconds());
    jum.assertEquals(112, dt.getMilliseconds());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6);
    dt.setSeconds(124);
    jum.assertEquals(14, dt.getMinutes());
    jum.assertEquals(4, dt.getSeconds());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6);
    dt.setMinutes(-20);
    jum.assertEquals(21, dt.getHours());
    jum.assertEquals(40, dt.getMinutes());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6);
    dt.setHours(52);
    jum.assertEquals(25, dt.getDate());
    jum.assertEquals(4, dt.getHours());
    
    dt = new D(2006, 9, 23, 22, 12, 55, 6);
    dt.setDate(35);
    jum.assertEquals(10, dt.getMonth());
    jum.assertEquals(4, dt.getDate());
    
    // Leap year
    dt = new D(2004, 1, 28);
    dt.setDate(29);
    jum.assertEquals(1, dt.getMonth());
    jum.assertEquals(29, dt.getDate());
    
    // Non-leap-year
    dt = new D(2005, 1, 28);
    dt.setDate(29);
    jum.assertEquals(2, dt.getMonth());
    jum.assertEquals(1, dt.getDate());
    
    // Wraparound broken in Safari 2, see WebKit bug 4892
    // http://bugs.webkit.org/show_bug.cgi?id=489
    // Safari 2 == Safari/412 to Safari/419.3
    // http://developer.apple.com/internet/safari/uamatrix.html
    if (navigator.userAgent.indexOf('Safari/41') == -1) {
        dt = new D(2006, 9, 23, 22, 12, 55, 6);
        dt.setMonth(14);
        jum.assertEquals(2007, dt.getYear());
        jum.assertEquals(2, dt.getMonth());
        
        // Date wraparoud -- Set month to Feb with date of 31st
        // should wrap date to March 3rd
        dt = new D(2006, 0, 31);
        dt.setMonth(1);
        jum.assertEquals(2, dt.getMonth());
        jum.assertEquals(3, dt.getDate());
    }
},

test_dateUTCSetters: function () {
    var dt = null;
    
    // UTC date
    dt = new D(2006, 9, 23, 22, 12, 55, 6, 'Etc/UTC');
    dt.setUTCHours(54);
    // Should all be the same -- zero offset
    jum.assertEquals(25, dt.getUTCDate());
    jum.assertEquals(25, dt.getDate());
    jum.assertEquals(6, dt.getUTCHours());
    jum.assertEquals(6, dt.getHours());
    
    // Wraparound broken in Safari 2, see WebKit bug 4892
    // http://bugs.webkit.org/show_bug.cgi?id=489
    // Safari 2 == Safari/412 to Safari/419.3
    // http://developer.apple.com/internet/safari/uamatrix.html
    if (navigator.userAgent.indexOf('Safari/41') == -1) {
        dt = new D(2006, 9, 23, 22, 12, 55, 6, 'America/Chicago');
        dt.setUTCHours(54);
        // Should all be the same -- zero offset
        jum.assertEquals(25, dt.getUTCDate());
        jum.assertEquals(25, dt.getDate());
        jum.assertEquals(6, dt.getUTCHours());
        jum.assertEquals(11, dt.getHours());
    }
},

   test_addDuration: function (){
       var date = new cosmo.datetime.Date(2000,0,1,12,0,0);
       var duration = new cosmo.model.Duration({year:1});
       date.addDuration(duration);
       jum.assertTrue(date.equals(new cosmo.datetime.Date(2001,0,1,12,0,0)));

       var date = new cosmo.datetime.Date(2000,0,1,12,0,0);
       var duration = new cosmo.model.Duration("P1W");
       date.addDuration(duration);
       jum.assertTrue(date.equals(new cosmo.datetime.Date(2000,0,8,12,0,0)));
   },
   
   test_dojoFromIso8601: function(){
       //we monkey-patched dojo.date.fromIso8601 to fix a bug that occurs when 
       //parsing dates near DST switchover time. This verifies that patch.
       var string = "20071104T190000Z";
       var jsDate = dojo.date.fromIso8601(string);
       //should be 19, but unpatched gives 20!
       jum.assertEquals("Should be 19", 19,jsDate.getUTCHours())
   }

}