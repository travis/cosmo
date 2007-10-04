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

dojo.provide("cosmotest.datetime.test_timezone");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.timezone");
dojo.require("cosmo.datetime.timezone.LazyCachingTimezoneRegistry");
//Initialization.
//TODO - once Dojo implements setUp() and tearDown() move this code there.

var registry = new cosmo.datetime.timezone.LazyCachingTimezoneRegistry(cosmo.env.getBaseUrl() + "/js/lib/olson-tzdata/");

cosmo.datetime.timezone.setTimezoneRegistry(registry);

cosmotest.datetime.test_timezone = {
    getNyTz: function (){
        var timezone = cosmo.datetime.timezone.getTimezone("America/New_York");
        return timezone;
    },

    getUsRs: function (){
        var rs = cosmo.datetime.timezone.getRuleSet("US");
        return rs;
    },

    test_getTimezone: function (){
        var timezone = cosmotest.datetime.test_timezone.getNyTz();
        jum.assertTrue(timezone != null);
    },

    test_getDateField: function (){
        var getDateField = cosmo.datetime.timezone._getDateField;
        var scoobyDate = new ScoobyDate(2006, 11, 10, 12, 33, 30);
        jum.assertEquals(2006, getDateField(scoobyDate, "year"));
        jum.assertEquals(11, getDateField(scoobyDate, "month"));
        jum.assertEquals(10, getDateField(scoobyDate, "date"));
        jum.assertEquals(12, getDateField(scoobyDate, "hours"));
        jum.assertEquals(33, getDateField(scoobyDate, "minutes"));
        jum.assertEquals(30, getDateField(scoobyDate, "seconds"));
    
        var jsDate = new Date(2006, 11, 10, 12, 33, 30);
        jum.assertEquals(2006, getDateField(jsDate, "year"));
        jum.assertEquals(11, getDateField(jsDate, "month"));
        jum.assertEquals(10, getDateField(jsDate, "date"));
        jum.assertEquals(12, getDateField(jsDate, "hours"));
        jum.assertEquals(33, getDateField(jsDate, "minutes"));
        jum.assertEquals(30, getDateField(jsDate, "seconds"));
    
        var fullHashDate = { year: 2006,
                             month: 11,
                             date: 10,
                             hours: 12,
                             minutes: 33,
                             seconds: 30};
    
        jum.assertEquals(2006, getDateField(fullHashDate, "year"));
        jum.assertEquals(11, getDateField(fullHashDate, "month"));
        jum.assertEquals(10, getDateField(fullHashDate, "date"));
        jum.assertEquals(12, getDateField(fullHashDate, "hours"));
        jum.assertEquals(33, getDateField(fullHashDate, "minutes"));
        jum.assertEquals(30, getDateField(fullHashDate, "seconds"));
    
        var sparseHashDate = { year: 2006,
                               month: 11 };
    
        jum.assertEquals(2006, getDateField(sparseHashDate, "year"));
        jum.assertEquals(11, getDateField(sparseHashDate, "month"));
        jum.assertEquals(1, getDateField(sparseHashDate, "date"));
        jum.assertEquals(0, getDateField(sparseHashDate, "hours"));
        jum.assertEquals(0, getDateField(sparseHashDate, "minutes"));
        jum.assertEquals(0, getDateField(sparseHashDate, "seconds"));
    },

    test_compareDates: function (){
        var compareDates = cosmo.datetime.timezone._compareDates;
        var jsDate1 = new Date(2006, 11, 10, 12, 33, 30);
        var jsDate2 = new Date(2007, 11, 10, 12, 33, 30);
        jum.assertTrue(compareDates(jsDate1, jsDate2) < 0);
    
        jsDate1 = new Date(2006, 11, 10, 12, 33, 30);
        jsDate2 = new Date(2006, 11, 10, 12, 33, 30);
        jum.assertTrue(compareDates(jsDate1, jsDate2) == 0);
    
        jsDate1 = new Date(2006, 11, 10, 12, 33, 31);
        jsDate2 = new Date(2006, 11, 10, 12, 33, 30);
        jum.assertTrue(compareDates(jsDate1, jsDate2)  > 0);
    
        jsDate1 = new Date(2006, 11, 10, 13, 33, 31);
        jsDate2 = new Date(2006, 11, 10, 12, 33, 31);
        jum.assertTrue(compareDates(jsDate1, jsDate2)  > 0);
    
        var sparseHashDate = { year: 2006,
                               month: 11 };
        jsDate2 = new Date(2006, 11, 1, 1, 1, 1, 1);
        jum.assertTrue(compareDates(sparseHashDate, jsDate2) < 0);
    },

    test_getZoneItemForDate: function (){
        var tz = cosmotest.datetime.test_timezone.getNyTz();
        var date = new Date(2006, 1, 1);
        var zoneItem = tz._getZoneItemForDate(date);
        jum.assertEquals(null, zoneItem.untilDate);
    
        date = new Date(1966, 11, 31);
        zoneItem = tz._getZoneItemForDate(date);
        jum.assertEquals(1967, zoneItem.untilDate.year);
    
        date = new Date(1800, 1, 1);
        zoneItem = tz._getZoneItemForDate(date);
        jum.assertEquals(1883, zoneItem.untilDate.year);
    
        date = new Date(1920, 1, 1);
        zoneItem = tz._getZoneItemForDate(date);
        jum.assertEquals(1942, zoneItem.untilDate.year);
        },
    
        test_getRulesForYear:     function (){
        var rs = cosmotest.datetime.test_timezone.getUsRs();
        var rules = rs._getRulesForYear(1999);
        jum.assertEquals(2, rules.length);
        jum.assertEquals(1967, rules[0].startYear);
    },

    test_DayGreateThanNForMonthAndYear: function (){
        var func = cosmo.datetime.timezone._getDayGreaterThanNForMonthAndYear;
    
        //"get me the date of the first thursday that is greater than or equal to the 8th in November"
        var date = func(8, 4, 10, 2006);
        jum.assertEquals(9, date);
    
        //"get me the date of the first wednesday that is greater than or equal to the 8th in November"
        date = func(8, 3, 10, 2006);
        jum.assertEquals(8, date);
    
        //"get me the date of the first tuesday that is greater than or equal to the 8th in November"
        date = func(8, 2, 10, 2006);
        jum.assertEquals(14, date);
    },

    test_DayLessThanNForMonthAndYear: function (){
        var func = cosmo.datetime.timezone._getDayLessThanNForMonthAndYear;
    
        //"get me the date of the last thursday that is less than or equal to the 8th in November"
        var date = func(8,4,10,2006);
        jum.assertEquals(2, date);
    
        //"get me the date of the last wednesday that is less than or equal to the 8th in November"
        var date = func(8,3,10,2006);
        jum.assertEquals(8, date);
    
        //"get me the date of the last tuesday that is less than or equal to the 8th in November"
        var date = func(8,2,10,2006);
        jum.assertEquals(7, date);
    },

    test_getStartDateForYear: function (){
      //to test: cosmo.datetime.timezone.Rule.prototype._getStartDateForYear = function(year)
      var rs = cosmotest.datetime.test_timezone.getUsRs();
      var sorter = function(a,b){return a.startMonth - b.startMonth};
    
      var rules = rs._getRulesForYear(1967);
      rules.sort(sorter);
      var startDate = rules[0]._getStartDateForYear(2006);
    
      //for sanity's sake, make sure it's APR
      jum.assertEquals(3, startDate.month);
    
      //rule says Apr, lastSun - last sunday in april which is the 30th
      jum.assertEquals(30, startDate.date);
    
      rules = rs._getRulesForYear(1974);
      rules.sort(sorter);
      startDate = rules[0]._getStartDateForYear(1974);
    
      //rule says "jan 6"
      jum.assertEquals(0, startDate.month);
      jum.assertEquals(6, startDate.date);
    
      rules = rs._getRulesForYear(2007);
      rules.sort(sorter);
      startDate = rules[0]._getStartDateForYear(2007);
    
      //rule sun>=8 - first sunday after or on the eighth which is the 11th
      jum.assertEquals(2, startDate.month);
      jum.assertEquals(11, startDate.date);
    },

    test_getOffsetInMinutes: function (){
    var timezone = cosmotest.datetime.test_timezone.getNyTz();
    var date;
    var offset;

    date = new Date(2006, 1, 1);
    offset = timezone.getOffsetInMinutes(date);
    jum.assertEquals(-300, offset);

    date = new Date(2006, 3, 1);
    offset = timezone.getOffsetInMinutes(date);
    jum.assertEquals(-300, offset);

    date = new Date(2006, 3, 2, 1, 59, 69);
    offset = timezone.getOffsetInMinutes(date);
    jum.assertEquals(-300, offset);

    date = new Date(2006, 3, 2, 3, 0, 0);
    offset = timezone.getOffsetInMinutes(date);
    jum.assertEquals(-240, offset);
    },

    test_getRuleForDate: function(){
        //var tz = cosmo.datetime.timezone._timezoneRegistry.getTimezone("America/Barbados");
        var date = new Date(2006, 1, 1);
        var ruleSet = cosmo.datetime.timezone.getRuleSet("Barb");
        var rule = ruleSet._getRuleForDate(date);
        jum.assertTrue(rule != null);
        jum.assertTrue(rule.startYear == 1980);
        jum.assertTrue(rule.letter == "S");
        
    },

    getPrefixes: function (){
    var files = ["northamerica", "africa", "antarctica", "asia", "australasia", "europe", "pacificnew", "southamerica", "backward"];
    var prefixes = {};
    
    function spit(file){
        var content = dojo.hostenv.getText(cosmo.datetime.timezone._timezoneRegistry.timezoneFileRoot + "/" + file);
        cosmo.datetime.timezone.parse(content, 
            function(tz){
                var prefix = tz.tzId.split("\/")[0];
                var prefixRecord = prefixes[prefix];
                if (!prefixRecord){
                   prefixes[prefix] = {};
                   prefixes[prefix][file] = 1;
                } else {
                    if (!prefixRecord[file]){
                        prefixRecord[file] = 1;
                    } else {
                        prefixRecord[file] = prefixRecord[file] + 1;
                    }
                }
               
            }, function(){}, function(){});
    }
    
    for (var x = 0; x < files.length; x++){
        spit(files[x]);
    }
    
    return prefixes;    
},

getPrefixToFileMap: function (){

    function getFileNameWithMostTzids(record){
        var winner = 0;
        var winnerFile = "";
        for (var file in record){
            var num = record[file];
            if (num > winner){
                winner = num;
                winnerFile = file;
            }
        }
        return winnerFile;
    }

    var prefixes = getPrefixes();
    var map = {};
    for (var prefix in prefixes){
        var record = prefixes[prefix];
        map[prefix] = getFileNameWithMostTzids(record);
    }
    
    return map;
},

printPrefixes: function (){
    var prefixes = getPrefixes();
    for (var prefix in prefixes){
        print("Prefix: '" + prefix + "'");
        var record = prefixes[prefix];
        for (var file in record){
            print("    '" + file + "': " + record[file]);
        }
    }
},

getExceptionMap: function (){
   var prefixToFileMap = getPrefixToFileMap();
   var files = ["northamerica", "africa", "antarctica", "asia", "australasia", "europe", "pacificnew", "southamerica", "backward"];
   var map = {};
   
   for (var x = 0; x < files.length; x++){
       var file = files[x];
       var content = dojo.hostenv.getText(cosmo.datetime.timezone._timezoneRegistry.timezoneFileRoot + "/" + file);
       cosmo.datetime.timezone.parse(content, function(timezone){
           var prefix = timezone.tzId.split("\/")[0];
           if (prefixToFileMap[prefix] != file ){
              map[timezone.tzId] = file;               
           }
       }, function(){}, function(){});
   }
   
   return map;
}
}