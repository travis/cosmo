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

dojo.provide("cosmo.datetime.timezone");

cosmo.datetime.timezone.Timezone = function(tzId, zoneItems){
    this.tzId = tzId;
    this.zoneItems = zoneItems || [];    
};

cosmo.datetime.timezone.Timezone.prototype.addZoneItem = function(zoneItem){
    this.zoneItems.push(zoneItem);
};

cosmo.datetime.timezone.ZoneItem = function(){
 //TODO 
};

cosmo.datetime.timezone.RuleSet = function(name, rules){
    this.name = name;
    this.rules = rules || [];
};

cosmo.datetime.timezone.RuleSet.prototype.addRule = function(rule){
    this.rules.push(rule);
};

cosmo.datetime.timezone.Rule = function(){
    // TODO
}

cosmo.datetime.timezone.TimezoneRegistry = function(){
    this.init();
};

cosmo.datetime.timezone.TimezoneRegistry.prototype.init = function(){
   //TODO don't hardcode this.
   var files = ["/cosmo/js/lib/olson-tzdata/northamerica"];
   dojo.lang.map(files, this._parseUri);
};

cosmo.datetime.timezone.TimezoneRegistry.prototype._parseUri = function(uri){
    var content = dojo.hostenv.getText(uri);
    cosmo.datetime.timezone.parse(content);
};

cosmo.datetime.timezone.parse = function(str){ 
       //TMP
       var self = {};
       self.ruleSets = [];
       self.zones = [];
       xxxr = self.ruleSets;
       xxxz = self.zones;
        var lines = str.split('\n');
        
        //the current zone
        var zone = null;
      
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i];
            
            if (line.match(/^\s/)) {
                //we do this because zone lines after the first don't have a zone in it.
                line = "Zone " + zone.tzId + line;
            }
            
            //Skip comments
            if (dojo.string.startsWith(line, "#") || line.length < 3){
                continue;
            }
            
            //split on whitespace
	        var arr = line.split(/\s+/);
	        
	        //is this a zone, a rule, a link or what?
	        var lineType = arr.shift();
	        switch(lineType) {
	            case 'Zone':
	                var tzId = arr.shift();
                    var zoneItem = this._parseZoneLine(arr);
	                if (!self.zones[tzId]) {
	                    zone =  new cosmo.datetime.timezone.Timezone(tzId);
	                    self.zones[tzId] = zone;
	                } else {
	                    zone = self.zones[tzId];
	                }
	                zone.addZoneItem(zoneItem);
	                break;
	            case 'Rule':
	                var ruleName = arr.shift();
	                var rule = this._parseRuleLine(arr);
	                if (!self.ruleSets[ruleName]) { 
	                    ruleSet = new cosmo.datetime.timezone.RuleSet(ruleName);
	                    self.ruleSets[ruleName] = ruleSet;
	                } else {
	                    ruleSet = self.ruleSets[ruleName];
	                }
	                ruleSet.addRule(rule);
	                break;
	            case 'Link':
	                // Shouldn't exist
	                /**TODO Handle This
	                if (self.zones[arr[1]]) { 
	                    alert('Error with Link ' + arr[1]); 
	                }
	                self.zones[arr[1]] = arr[0];
	                **/
	                break;
	            case 'Leap':
	                break;
	            default:
	                // Fail silently
	                break;
	        }//end switch
            
        }//end for
        return true;
    };//end function

cosmo.datetime.timezone._parseZoneLine = function(array){
    var zoneItem = new cosmo.datetime.timezone.ZoneItem();

    var arrayPrinter = function(starter, arrayToPrint){
        var stringy = starter;
	    for (var index = 0; index < arrayToPrint.length; index++){
	        stringy += index + "->'" + arrayToPrint[index] + "'    "; 
	    }
	    dojo.debug(stringy);
    }
    xxxarray = array;
    arrayPrinter ("zoneLine --> ", array); 
    
    //DEBUG: zoneLine --> 0->'-10:30' 1->'-' 2->'HST' 3->'1933' 4->'Apr' 5->'30' 6->'2:00'
    //                    GMTOFF      RULE   FORMAT   UNTIL-YR  MONTH    DAY     TIME
	/*
	if (!z[3]) { break; }
	var yea = parseInt(z[3]);
	var mon = 11;
	var dat = 31;
	if (z[4]) {
	    mon = monthMap[z[4].substr(0, 3).toLowerCase()];
	    dat = parseInt(z[5]);
	}
	var t = z[6] ? z[6] : '23:59:59';
	t = parseTimeString(t);
	var d = Date.UTC(yea, mon, dat, t[1], t[2], t[3]);
    */
    //TODO set properties...
    return zoneItem;
};

cosmo.datetime.timezone._parseRuleLine = function(line){
    var rule = new cosmo.datetime.timezone.Rule();
    //TODO set properties...
	return rule;
};


/*
cosmo.datetime.TimezoneParser.MONTH_MAP = 
    var monthMap = { 'jan': 0, 'feb': 1, 'mar': 2, 'apr': 3,'may': 4, 'jun': 5, 
        'jul': 6, 'aug': 7, 'sep': 8, 'oct': 9, 'nov': 10, 'dec': 11 };

cosmo.datetime.TimezoneParser.DAY_MAP =  {'sun': 0,'mon' :1, 'tue': 2, 'wed': 3, 'thu': 4, 'fri': 5, 'sat': 6 };



fleegix = {};
fleegix.tz = new function() {
    
    var self = this;
    var monthMap = { 'jan': 0, 'feb': 1, 'mar': 2, 'apr': 3,'may': 4, 'jun': 5, 
        'jul': 6, 'aug': 7, 'sep': 8, 'oct': 9, 'nov': 10, 'dec': 11 } 
    var dayMap = {'sun': 0,'mon' :1, 'tue': 2, 'wed': 3, 'thu': 4, 'fri': 5, 'sat': 6 }
    
    function parseTimeString(str) {
        var pat = /(\d+)(?::0*(\d*))?(?::0*(\d*))?([wsugz])?$/;
        var hms = str.match(pat);
        hms[1] = parseInt(hms[1]);
        hms[2] = hms[2] ? parseInt(hms[2]) : 0;
        hms[3] = hms[3] ? parseInt(hms[3]) : 0;
        return hms;
    }
    function getZone(dt) {
        var timezone = dt.timezone;
        var zones = self.zones[timezone];
        while (typeof(zones) == "string") {
            timezone = zones;
            zones = self.zones[timezone];
            if (!zones) {
                alert('Cannot figure out the timezone ' + timezone);
            }
        }
        for(var i = 0; i < zones.length; i++) {
            var z = zones[i];
            if (!z[3]) { break; }
            var yea = parseInt(z[3]);
            var mon = 11;
            var dat = 31;
            if (z[4]) {
                mon = monthMap[z[4].substr(0, 3).toLowerCase()];
                dat = parseInt(z[5]);
            }
            var t = z[6] ? z[6] : '23:59:59';
            t = parseTimeString(t);
            var d = Date.UTC(yea, mon, dat, t[1], t[2], t[3]);
            if (dt.getTime() < d) { break; }
        }
        if (i == zones.length) {
           alert('No DST for ' + timezone); 
        }
        // Get basic offset
        else {
            return zones[i]; 
        }
        
    }
    function getBasicOffset(z) {
        var off = parseTimeString(z[0]);
        var adj = z[0].indexOf('-') == 0 ? -1 : 1
        off = adj * (((off[1] * 60 + off[2]) *60 + off[3]) * 1000);
        return -off/60/1000;
    }
    function getRule(dt, str) {
        var currRule = null;
        var year = dt.getUTCFullYear();
        var rules = self.rules[str];
        var ruleHits = [];
        if (!rules) { return null; }
        for (var i = 0; i < rules.length; i++) {
            r = rules[i];
            if ((r[1] < (year - 1)) || 
                (r[0] < (year - 1) && r[1] == 'only') ||
                (r[0] > year)) { 
                continue; 
            };
            var mon = monthMap[r[3].substr(0, 3).toLowerCase()];
            var day = r[4];
            
            if (isNaN(day)) {
                if (day.substr(0, 4) == 'last') {
                    var day = dayMap[day.substr(4,3).toLowerCase()];
                    var t = parseTimeString(r[5]);
                    // Last day of the month at the desired time of day
                    var d = new Date(Date.UTC(dt.getUTCFullYear(), mon+1, 1, t[1]-24, t[2], t[3]));
                    var dtDay = d.getUTCDay();
                    var diff = (day > dtDay) ? (day - dtDay - 7) : (day - dtDay);
                    // Set it to the final day of the correct weekday that month
                    d.setUTCDate(d.getUTCDate() + diff);
                    if (dt < d) {
                        // If no match found, check the previous year if rule allows
                        if (r[0] < year) {
                            d.setUTCFullYear(d.getUTCFullYear()-1);
                            if (dt >= d) {
                                ruleHits.push({ 'rule': r, 'date': d });
                            }
                        }
                    }
                    else {
                        ruleHits.push({ 'rule': r, 'date': d });
                    }
                }
                else {
                    day = dayMap[day.substr(0, 3).toLowerCase()];
                    if (day != 'undefined') {
                        if(r[4].substr(3, 2) == '>=') {
                            var t = parseTimeString(r[5]);
                            // The stated date of the month
                            var d = new Date(Date.UTC(dt.getUTCFullYear(), mon, 
                                parseInt(r[4].substr(5)), t[1], t[2], t[3]));
                            var dtDay = d.getUTCDay();
                            var diff = (day < dtDay) ? (day - dtDay + 7) : (day - dtDay);
                            // Set to the first correct weekday after the stated date
                            d.setUTCDate(d.getUTCDate() + diff);
                            if (dt < d) {
                                // If no match found, check the previous year if rule allows
                                if (r[0] < year) {
                                    d.setUTCFullYear(d.getUTCFullYear()-1);
                                    if (dt >= d) {
                                        ruleHits.push({ 'rule': r, 'date': d });
                                    }
                                }
                            }
                            else {
                                ruleHits.push({ 'rule': r, 'date': d });
                            }
                        }
                        else if (day.substr(3, 2) == '<=') {
                            var t = parseTimeString(r[5]);
                            // The stated date of the month
                            var d = new Date(Date.UTC(dt.getUTCFullYear(), mon, 
                                parseInt(r[4].substr(5)), t[1], t[2], t[3]));
                            var dtDay = d.getUTCDay();
                            var diff = (day > dtDay) ? (day - dtDay - 7) : (day - dtDay);
                            // Set to first correct weekday before the stated date
                            d.setUTCDate(d.getUTCDate() + diff);
                            if (dt < d) {
                                // If no match found, check the previous year if rule allows
                                if (r[0] < year) {
                                    d.setUTCFullYear(d.getUTCFullYear()-1);
                                    if (dt >= d) {
                                        ruleHits.push({ 'rule': r, 'date': d });
                                    }
                                }
                            }
                            else {
                                ruleHits.push({ 'rule': r, 'date': d });
                            }
                        }
                    }
                }
            }
            else {
                var t = parseTimeString(r[5]);
                var d = new Date(Date.UTC(dt.getUTCFullYear(), mon, day, t[1], t[2], t[3]));
                d.setUTCHours(d.getUTCHours() - 24*((7 - day + d.getUTCDay()) % 7));
                if (dt < d) { 
                    continue; 
                }
                else {
                    ruleHits.push({ 'rule': r, 'date': d });
                }
            }
        }
        f = function(a, b) { return (a.date.getTime() >= b.date.getTime()) ?  1 : -1; }
        ruleHits.sort(f);
        currRule = ruleHits.pop().rule;
        return currRule;
    }
    function getAdjustedOffset(off, rule) {
        var save = rule[6];
        var t = parseTimeString(save);
        var adj = save.indexOf('-') == 0 ? -1 : 1;
        var ret = (adj*(((t[1] *60 + t[2]) * 60 + t[3]) * 1000));
        ret = ret/60/1000;
        ret -= off
        ret = -Math.ceil(ret);
        return ret;
    }
    
    this.files = ['africa', 'antarctica', 'asia', 'australasia', 
        'backward', 'etcetera', 'europe', 
        'northamerica', 'pacificnew', 'southamerica'];
    this.zones = {};
    this.rules = {};
    this.parseZones = function(str) {
        var s = '';
        var lines = str.split('\n');
        var arr = [];
        var chunk = '';
        var zone = null;
        var rule = null;
        for (var i = 0; i < lines.length; i++) {
            l = lines[i];
            if (l.match(/^\s/)) {
                l = "Zone " + zone + l;
            }
            l = l.split("#")[0];
            if (l.length > 3) {
                arr = l.split(/\s+/);
                chunk = arr.shift();
                switch(chunk) {
                    case 'Zone':
                        zone = arr.shift();
                        if (!self.zones[zone]) { self.zones[zone] = [] }
                        self.zones[zone].push(arr);
                        break;
                    case 'Rule':
                        rule = arr.shift();
                        if (!self.rules[rule]) { self.rules[rule] = [] }
                        self.rules[rule].push(arr);
                        break;
                    case 'Link':
                        // Shouldn't exist
                        if (self.zones[arr[1]]) { alert('Error with Link ' + arr[1]); }
                        self.zones[arr[1]] = arr[0];
                        break;
                    case 'Leap':
                        break;
                    default:
                        // Fail silently
                        break;
                }
            }
        }
        return true;
    };
    this.getOffset = function(dt) {
        var zone = getZone(dt);
        var rule = getRule(dt, zone[1]);
        var off = getBasicOffset(zone);
        if (rule) {
            off = getAdjustedOffset(off, rule);
        }
        return off;
    }
}

function print(str) {
  var printDiv = document.getElementById('printDiv');
  printDiv.innerHTML += str;
}

function println(str) {
  print(str + '<br/>');
}

function init() {
    var url = 'tz/' + 'northamerica.txt';
    var f = setup;
    fleegix.xhr.doGet(f, url);
}

function setup(str) {
    var s = '';
    s += '<div style="padding-bottom:8px;">';
    s += '<select id="tzName" name="tzName">';
    if (fleegix.tz.parseZones(str)) {
        for (var i in fleegix.tz.zones) {
            if (i.indexOf('America') > -1 || i.indexOf('Pacific') > -1) {
                s += '<option value="' + i + '">' + i + '</option>'
            }
        }
        s += '</select>';
        s += '</div>';
        $('selectElemDiv').innerHTML += s;
    }
}

window.onload = init;

function getTZOffset() {
    var form = $('tzForm');
    var str = form.inputDate.value;
    var pat = /^(\d{1,2})(\/|-)(\d{1,2})\2(\d{4})$/;
    var mat = str.match(pat);
    if (!mat) {
        alert('Please enter a date in a valid format.');
        return false;
    }
    var dt = new Date(str);
    var tz = form.tzName.value;
    dt = new Date(Date.UTC(dt.getFullYear(), dt.getMonth(), dt.getDate()));
    dt.timezone = tz;
    var off = fleegix.tz.getOffset(dt);
    println(off);
}*/