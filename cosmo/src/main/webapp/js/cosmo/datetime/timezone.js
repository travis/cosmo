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

dojo.provide("cosmo.datetime.timezone");

dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.string.extras");

cosmo.datetime.HOURS_IN_DAY = 24;
cosmo.datetime.MINUTES_IN_HOUR = 60;
cosmo.datetime.SECONDS_IN_MINUTE = 60;

cosmo.datetime.timezone.REGIONS = ["Africa", "America", "Asia", "Antarctica", "Atlantic", "Australia", "Europe", "Indian", "Pacific" ];

cosmo.datetime.timezone._MONTH_MAP = { 'jan': 0, 'feb': 1, 'mar': 2, 'apr': 3,'may': 4, 'jun': 5,
                                       'jul': 6, 'aug': 7, 'sep': 8, 'oct': 9, 'nov': 10, 'dec': 11 };

cosmo.datetime.timezone._DAY_MAP =  {'sun': 0,'mon' :1, 'tue': 2, 'wed': 3, 'thu': 4, 'fri': 5, 'sat': 6 };
cosmo.datetime.timezone._RULE_OP_LAST = "LAST";
cosmo.datetime.timezone._RULE_OP_GTR = "GTR";
cosmo.datetime.timezone._RULE_OP_LESS = "LSS";

cosmo.datetime.timezone.Timezone = function(tzId, zoneItems){
    // summary: Represents a single olson timezone
    // description: Basically consists of a bunch ZoneItems and the tzId

    // tzId : String
    //    the time zone ID (eg. "America/Los_Angeles")
    this.tzId = tzId;

    // zoneItems: Array
    //    An array with teh ZoneItems for this timezone
    this.zoneItems = zoneItems || [];
};

cosmo.datetime.timezone.Timezone.prototype.toString = function(){
    var s = "Timezone: " + this.tzId + "\n";
    for (var x = 0; x < this.zoneItems.length; x++){
        s +=  this.zoneItems[x].toString(x == 0);
    }
    return s;
};

cosmo.datetime.timezone.Timezone.prototype.addZoneItem = function(zoneItem){
    this.zoneItems.push(zoneItem);
};

cosmo.datetime.timezone.Timezone.prototype.getOffsetInMinutes = function(/*Date*/ date){
    // summary: returns the offset from GMT in minutes for this timezone
    // for the given date.

    //first get the right ZoneItem
    var zoneItem = this._getZoneItemForDate(date);
    var originalOffset = zoneItem.offsetInMinutes;

    //if there is no rulename, just return the timezone's normal offset
    if (!zoneItem.ruleName){
        return originalOffset;
    }

    //now find the right rule for this date.
    var ruleSet = cosmo.datetime.timezone.getRuleSet(zoneItem.ruleName);
    var rule = ruleSet._getRuleForDate(date);

    if (rule == null){
        return zoneItem.offsetInMinutes;
    } else {
        return zoneItem.offsetInMinutes + rule.addMinutes;
    }
};

cosmo.datetime.timezone.Timezone.prototype.getAbbreviatedName = function(/*Date*/ date){
    // summary: gets the short name for this timezone during the given date.
    // description: The short name can be dependent on the date, eg. EST vs. EDT,
    // standard time versus daylight-savings
    var zoneItem = this._getZoneItemForDate(date);
    if (!zoneItem.ruleName){
        return zoneItem.format;
    }

    var ruleSet = cosmo.datetime.timezone.getRuleSet(zoneItem.ruleName);
    var rule = ruleSet._getRuleForDate(date);

    if (!rule || !rule.letter){
        return zoneItem.format;
    }

    return zoneItem.format.replace("%s", rule.letter);

}

cosmo.datetime.timezone.Timezone.prototype._getZoneItemForDate = function(/*Date*/ date){
    // summary: retruns the correct zoneItem for the given date
    var compareDates = cosmo.datetime.timezone._compareDates;
    for (var x = 0; x < this.zoneItems.length; x++){
        var zoneItem = this.zoneItems[x];
        var untilDate = zoneItem.untilDate;
        if (!untilDate){
            return zoneItem;
        }

        if (compareDates(date, untilDate) <= 0){
            return zoneItem;
        }
    }
};

cosmo.datetime.timezone._compareDates = function(d1, d2){
    // summary: comparator ("compare function" in JS parlance) for comparing dates
    // or things that look like dates - works with cosmo.datetime.Date, native JS dates
    // and raw objects with similarlry named properties.
    var getDateField = cosmo.datetime.timezone._getDateField;
    var fields = ["year", "month", "date", "hours", "minutes", "seconds"];

    for (var x = 0; x < fields.length; x++){
        var diff = getDateField(d1, fields[x]) - getDateField(d2, fields[x]);
        if (diff != 0) {
            return diff;
        }
    }

    //fell through, so they must be equal
    return 0;
};

cosmo.datetime.timezone._getLastDayForMonthAndYear = function(day, month, year){
    // summary: returns the day of the month of the last time the given day of the week occurs in a
    // given month and year.
    var lastDayOfMonth = new Date(year, month + 1,1, -24);
    var lastDayDay = lastDayOfMonth.getDay();
    var diff = (day > lastDayDay) ? (day - lastDayDay - 7) : (day - lastDayDay);
    return lastDayOfMonth.getDate() + diff;
};

cosmo.datetime.timezone._getDayGreaterThanNForMonthAndYear = function(n, day, month, year){
    // summary: returns the first date (day of month) after (or on) the given date "n" in the
    // given month and year for which the given day occurs
    // description: Confusing eh? Here's some help: if there params were "10, 1, 0, 2007" this
    // function would return the first Monday (1) that occurs after the 10th, in January(0), 2007
    var startingDate = new Date(year, month, n);
    var date = startingDate.getDate();
    var startingDay = startingDate.getDay();
    date += (day >= startingDay) ? day - startingDay : (day + 7) - startingDay;
    return date;
};

cosmo.datetime.timezone._getDayLessThanNForMonthAndYear = function(n, day, month, year){
    // summary: returns the first date (day of month) before (or on) the given date "n" in the
    // given month and year for which the given day occurs
    // description: Example: if there params were "10, 1, 0, 2007" this
    // function would return the first Monday (1) that occurs before (or on) the 10th, in January(0), 2007
    var startingDate = new Date(year, month, n);
    var date = startingDate.getDate();
    var startingDay = startingDate.getDay();
    date += (day <= startingDay) ? day - startingDay : (day - 7) - startingDay;
    return date;
};

cosmo.datetime.timezone._getDateField = function(date, field){
    if (field == "year"){
        return date.getFullYear ? date.getFullYear() : date.year;
    }
    if (field == "month"){
        var ret = date.getMonth ? date.getMonth() : date.month;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
    if (field == "date"){
        var ret = date.getDate ? date.getDate() : date.date;
        return typeof(ret) == "undefined" ? 1 : ret;
    }
    if (field == "hours"){
        var ret = date.getHours ? date.getHours() : date.hours;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
    if (field == "minutes"){
        var ret = date.getMinutes ? date.getMinutes() : date.minutes;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
    if (field == "seconds"){
        var ret = date.getSeconds ? date.getSeconds() : date.seconds;
        return typeof(ret) == "undefined" ? 0 : ret;
    }
}

cosmo.datetime.timezone.ZoneItem = function(){
    // summary: represents one Zone line in a olson timezone

    this.offsetInMinutes = null;
    this.ruleName = null;
    this.format = null;
    this.untilDate = null;
};

cosmo.datetime.timezone._hasOrElse = function(obj, prop, orElse){
   return dojo.lang.has(obj, prop) && obj[prop] != null ? obj[prop] : orElse;
}

cosmo.datetime.timezone.ZoneItem.prototype.toString = function(/*boolean (optional)*/ showHeader){
    var hasOrElse = cosmo.datetime.timezone._hasOrElse;
    if (typeof(showHeader) == "undefined"){
        showHeader = true;
    }

    var s = "";
    if (showHeader){
        s += "Offset(Minutes)\tRule\tFormat\tUntilDate\n";
    }

    s += Math.round(this.offsetInMinutes) + "\t"
      + (hasOrElse(this,"ruleName", "-")) + "\t"
      + (hasOrElse(this,"format", "-")) + "\t";
      if (this.untilDate){
        s += this.untilDate.year + "/"
        + (this.untilDate.month + 1) + "/"
        + this.untilDate.date + " "
        + this.untilDate.hours + "h "
        + this.untilDate.minutes +"m "
        + this.untilDate.seconds + "s \n";
      }
    return s;
}

cosmo.datetime.timezone.RuleSet = function(name, rules){
  // summary: holds all the Rules with the same name
    this.name = name;
    this.rules = rules || [];

};

cosmo.datetime.timezone.RuleSet.prototype.toString = function(){
    var s = "Rule: " + this.name+ "\n";
    for (var x = 0; x < this.rules.length; x++){
        s +=  this.rules[x].toString(x == 0);
    }
    return s;
};

cosmo.datetime.timezone.RuleSet.prototype.addRule = function(rule){
    this.rules.push(rule);
};

cosmo.datetime.timezone.RuleSet.prototype._getRulesForYear = function(year){
    // summary: returns all the rules that are used in a given year
     var result = [];
     var rules = this.rules;
     for (var x = 0; x < this.rules.length;x++){
         var rule = rules[x];
         if ( (year >= rule.startYear) && (year <= rule.endYear) ){
             result.push(rule);
         }
     }
     return result;
};

cosmo.datetime.timezone.RuleSet.prototype._getLastOccurringRule = function(){
    //sumamry: returns the rule with the latest possible date. In other words
    //if the date was an infinite amount of years from now, this would be 
    //the last rule that occurred.

    if (this.rules == null || this.rules.length == 0){
        return null;
    }

    //make a copy of the rules array;
    var rules = this.rules.slice()

    rules.sort(function(a,b){
        if (a.endYear > b.endYear){
            return -1;
        }

        if (a.endYear < b.endYear){
            return 1;
        }

        if (a.startMonth > b.startMonth){
            return -1;
        }

        return 1;
    });
    
    return rules[0];
}

cosmo.datetime.timezone.RuleSet.prototype._getRuleForDate = function(date){
    // summary: returns the appropriate Rule given the date
    var rules = this._getRulesForYear(date.getFullYear());
    if (!rules || !rules.length){
        //if there are no rules for that year, then we must get the last rule that
        //ever occurred
        return this._getLastOccurringRule();
    }

    rules.sort(function(a,b){return a.startMonth - b.startMonth});

    var lastApplicableRule = null;
    if (rules.length > 1){
        lastApplicableRule = rules[rules.length - 1];
    }

    for (var x = 0; x < rules.length; x++){
        var rule = rules[x];
        if (rule._applicable(date)){
            lastApplicableRule = rule;
        }
    }
    return lastApplicableRule;
}

cosmo.datetime.timezone.Rule = function(){
    // summary: one rule line in an olson timezone
    this.startYear = null;
    this.endYear = null;
    this.type = null;
    this.startMonth = null;
    this.startDate = null;
    this.startDay = null;;
    this.startOperator = null;
    this.startTime = null;
    this.addMinutes = null;
    this.letter = null;

    this._startDatesByYear = {};
}

cosmo.datetime.timezone.Rule.prototype.toString = function(/*boolean (optional)*/ showHeader){
    var hasOrElse = cosmo.datetime.timezone._hasOrElse

    if (typeof(showHeader) == "undefined"){
        showHeader = true;
    }

    var s = "";
    if (showHeader){
        s += "sYr\tenYr\tTp\tsMth\tsDte\tsDay\tsOp\tsTime\tAddMin\tLetter\n";
    }

    s += this.startYear + "\t"
      +  (hasOrElse(this, "endYear", "-")) + "\t"
      +  (hasOrElse(this, "type", "-")) + "\t"
      +  (hasOrElse(this, "startMonth", "-")) + "\t"
      +  (hasOrElse(this, "startDate", "-")) + "\t"
      +  (hasOrElse(this, "startDay", "-")) + "\t"
      +  (hasOrElse(this, "startOperator", "-")) + "\t";

    if (this.startTime){
      s += this.startTime.hours + "h "
        +  this.startTime.minutes + "m "
        +  this.startTime.seconds + "s" + "\t";
    }

    s += (hasOrElse(this, "addMinutes") ? Math.round(this.addMinutes): "-") + "\t";
    s += (hasOrElse(this, "letter", "-"));
    return s + "\n";
}

cosmo.datetime.timezone.Rule.prototype._applicable = function(date){
    // summary: returns whether or not this rule might be applicable for the given date
    // description: this does NOT return whether or not this is the right rule to use, just whether
    // or not the date is after this rules start date (hence "might")

    var ruleStartDate  = this._getStartDateForYear(date.getFullYear());
    return cosmo.datetime.timezone._compareDates(date, ruleStartDate) >= 0 ;
}

cosmo.datetime.timezone.Rule.prototype._getStartDateForYear = function(year){
    // summary: returns the date (naked object with date props not a "real" date) when this rule becomes
    // active
    // description: this is sort of an expensive call, so once it is calculated, it is cached.

    var startDate = this._startDatesByYear[year];
    if (startDate){
        return startDate;
    }

    startDate = { year: year,
                  month: this.startMonth,
                  hours: this.startTime.hours,
                  minutes: this.startTime.minutes,
                  seconds: this.startTime.seconds };

    if (this.startDay != null){
        if (this.startOperator == cosmo.datetime.timezone._RULE_OP_LAST){
            var date = cosmo.datetime.timezone._getLastDayForMonthAndYear(this.startDay, this.startMonth, year);
            startDate.date = date;
        } else if (this.startOperator == cosmo.datetime.timezone._RULE_OP_GTR){
            var date = cosmo.datetime.timezone._getDayGreaterThanNForMonthAndYear(this.startDate, this.startDay, this.startMonth, year);
            startDate.date = date;
        } else {
            var date = cosmo.datetime.timezone._getDayLessThanNForMonthAndYear(this.startDate, this.startDay, this.startMonth, year);
            startDate.date = date;
        }
    } else {
        startDate.date = this.startDate;
    }

    this._startDatesByYear[year] = startDate;
    return startDate;
}

cosmo.datetime.timezone.setTimezoneRegistry =  function(/*cosmo.datetime.timezone.SimpleTimezoneRegistry*/timezoneRegistry){
    // summary: sets the timezone registry to use for retrieving timezones
    cosmo.datetime.timezone._timezoneRegistry = timezoneRegistry;
}

cosmo.datetime.timezone.getTimezone = function(tzId){
    // summary: returns the timezone with the given id
    return this._timezoneRegistry.getTimezone(tzId);
}

cosmo.datetime.timezone.getRuleSet = function(ruleName){
    // summary: returns the ruleset with the given rule name
    return this._timezoneRegistry.getRuleSet(ruleName);
}

cosmo.datetime.timezone.getTzIdsForRegion = function(region){
    // summary: returns all the tzIds in a given region
    return this._timezoneRegistry.getTzIdsForRegion(region);
}

cosmo.datetime.timezone.parse = function(str, timezoneCallback, rulesetCallback, linkCallback){
        // summary: parses the given string as olson data, creating ZoneItems, Rules and link entries
        // passing them to the appropriate given call back

        var ruleSets = new dojo.collections.Dictionary();
        var zones = new dojo.collections.Dictionary();
        var links = {};
        var lines = str.split('\n');

        //the current zone
        var zone = null;

        for (var i = 0; i < lines.length; i++) {
            var line = lines[i];

            //Skip comments
            if (dojo.string.startsWith(dojo.string.trim(line), "#") || line.length < 3){
                continue;
            }

            if (line.match(/^\s/)) {
                //we do this because zone lines after the first don't have a zone in it.
                line = "Zone " + zone.tzId + line;
            }

            //in case there are any inline comments
            line = line.split("#")[0];

            //split on whitespace
            var arr = line.split(/\s+/);

            //is this a zone, a rule, a link or what?
            var lineType = arr.shift();
            switch(lineType) {
                case 'Zone':
                    var tzId = arr.shift();
                    var zoneItem = this._parseZoneLine(arr);
                    if (!zones.containsKey(tzId)) {
                        zone =  new cosmo.datetime.timezone.Timezone(tzId);
                        zones.add(tzId, zone);
                    } else {
                        zone = zones.item(tzId);
                    }
                    zone.addZoneItem(zoneItem);
                    break;
                case 'Rule':
                    var ruleName = arr.shift();
                    var rule = this._parseRuleLine(arr);
                    if (!ruleSets.containsKey(ruleName)) {
                        ruleSet = new cosmo.datetime.timezone.RuleSet(ruleName);
                        ruleSets.add(ruleName, ruleSet);
                    } else {
                        ruleSet = ruleSets.item(ruleName);
                    }
                    ruleSet.addRule(rule);
                    break;
                case 'Link':
                    var newName = arr.shift();
                    var oldName = arr.shift();
                    links[oldName] = newName;
                    break;
                case 'Leap':
                    break;
                default:
                    // Fail silently
                    break;
            }//end switch
        }//end for
        dojo.lang.map(zones.getValueList(),timezoneCallback);
        dojo.lang.map(ruleSets.getValueList(), rulesetCallback);
        for (var oldName in links){
            linkCallback(oldName, links[oldName])
        }
        return true;
    };//end function

cosmo.datetime.timezone._arrayPrinter = function(starter, arrayToPrint){
    var stringy = starter;
    for (var index = 0; index < arrayToPrint.length; index++){
        stringy += index + "->'" + arrayToPrint[index] + "'    ";
    }
};

cosmo.datetime.timezone._parseZoneLine = function(array){
    // summary: parses a olson 'Zone' line into a ZoneItem
    var zoneItem = new cosmo.datetime.timezone.ZoneItem();

    //The Format: zoneLine --> 0->'-10:30' 1->'-' 2->'HST' 3->'1933' 4->'Apr' 5->'30' 6->'2:00'
    //                    GMTOFF      RULE   FORMAT   UNTIL-YR  MONTH    DAY     TIME

    //first set the offset:
    zoneItem.offsetInMinutes = this._parseTimeIntoMinutes(array[0]);

    //set the rule name
    zoneItem.ruleName = array[1] == "-" ? null : array[1];

    //set the format.
    zoneItem.format = array[2];

    if (!array[3]){
        zoneItem.untilDate = null;
    } else {
        var until = {};
        until.year = array[3];
        if (array[4]){
           until.month = this._MONTH_MAP[array[4].substr(0,3).toLowerCase()];
           until.date = parseInt(array[5]);
        } else {
           until.month = 0;
           until.date = 1;
        }

        if (!array[6]){
            until.hours = 0;
            until.minutes = 0;
            until.seconds = 0;
        } else {
            var hms = this._parseTimeString(array[6]);
            until.hours = hms.hours;
            until.minutes = hms.minutes;
            until.seconds = hms.seconds;
        }
        zoneItem.untilDate = until;
    }
    return zoneItem;
};

cosmo.datetime.timezone._parseRuleLine = function(array){
    // sumamry: parses an olson 'Rule' line into a Rule object

    var rule = new cosmo.datetime.timezone.Rule();

    //The Format: DEBUG: Rule --> 0->'1942' 1->'only' 2->'-' 3->'Feb' 4->'9' 5->'2:00' 6->'1:00' 7->'W' 8->''
    //                            FROM      TO        TYPE   IN       ON     AT        SAVE      LETTER

    //set the start year
    rule.startYear = parseInt(array[0]) || (dojo.string.startsWith(array[0], "min") ? -99999 : 99999);

    //set the end Year
    rule.endYear = parseInt(array[1])
        || (dojo.string.startsWith(array[1], "o") ? rule.startYear : (dojo.string.startsWith(array[1], "min") ? -99999 : 99999));

    //set the type
    rule.type = array[2];

    //set the startmonth
    rule.startMonth = this._MONTH_MAP[array[3].substr(0,3).toLowerCase()];

    //set the start date
    var rawOn = array[4]; //the raw "ON" data
    var parsedOn = parseInt(rawOn);
    if (!isNaN(parsedOn)){
        rule.startDate = parseInt(rawOn);
    } else {
        if (dojo.string.startsWith(rawOn, "last")){
            rule.startDate = null;
            rule.startDay = this._DAY_MAP[rawOn.substr(4,3).toLowerCase()];
            rule.startOperator = this._RULE_OP_LAST;
        } else {
            rule.startOperator = (rawOn.indexOf(">") > -1) ? this._RULE_OP_GTR : (rawOn.indexOf("<") > -1 ? this._RULE_OP_LESS :null);
            rule.startDay = this._DAY_MAP[rawOn.substr(0,3).toLowerCase()];
            var parsedDate = parseInt(rawOn.substr(5, (rawOn.length-5)));
            rule.startDate = parsedDate;
        }
    }

    //set the startTime
    //TODO get that extra letter for wallclock, standard, etc.
    rule.startTime = this._parseTimeString(array[5]);

    //the amount of mintues to add
    rule.addMinutes = this._parseTimeIntoMinutes(array[6]);

    //the letter
    rule.letter = array[7];

    //TODO set properties...
    return rule;
};

cosmo.datetime.timezone._parseTimeString = function(str) {
    // summary: parses an olsom time string into an object with time properties
    var pat = /(\d+)(?::0*(\d*))?(?::0*(\d*))?([wsugz])?$/;
    var matchArray =  str.match(pat);
    var result = {};
    result.hours = parseInt(matchArray[1]);
    result.minutes = matchArray[2] ? parseInt(matchArray[2]) : 0;
    result.seconds = matchArray[3] ? parseInt(matchArray[3]) : 0;
    result.negative = dojo.string.startsWith(str, "-");
    return result;
};

 cosmo.datetime.timezone._parseTimeIntoMinutes = function(str){
    // summary: returns the time in minutes of an olson time string
    var hms = this._parseTimeString(str);
    var millis = ((((hms.hours * cosmo.datetime.MINUTES_IN_HOUR) + hms.minutes) * cosmo.datetime.SECONDS_IN_MINUTE) + hms.seconds) * 1000;
    var minutes = millis/1000/cosmo.datetime.SECONDS_IN_MINUTE;
    minutes = minutes * (hms.negative ? -1 : 1);
    return minutes;
 };
