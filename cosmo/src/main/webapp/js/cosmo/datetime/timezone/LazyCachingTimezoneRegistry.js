/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.datetime.timezone.LazyCachingTimezoneRegistry");

dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.timezone");
dojo.require("cosmo.datetime.timezone.SimpleTimezoneRegistry");

dojo.declare("cosmo.datetime.timezone.LazyCachingTimezoneRegistry",
    cosmo.datetime.timezone.SimpleTimezoneRegistry,
    function(timezoneFileRoot){
    this.timezoneFileRoot = timezoneFileRoot || null;
        var tzsByRegion = {};
        this._tzsByRegion = tzsByRegion;
        var regions = cosmo.datetime.timezone.REGIONS;
        for (var x = 0; x < regions.length; x++ ){
            var region = regions[x];
            tzsByRegion[region] = [];
        }
    },
    {

    _prefixToFileMap: {"EST":"northamerica","MST":"northamerica","HST":"northamerica","EST5EDT":"northamerica","CST6CDT":"northamerica","MST7MDT":"northamerica","PST8PDT":"northamerica","America":"northamerica","Pacific":"australasia","Atlantic":"europe","Africa":"africa","Indian":"africa","Antarctica":"antarctica","Asia":"asia","Australia":"australasia","Europe":"europe","WET":"europe","CET":"europe","MET":"europe","EET":"europe"},
    _exceptionsMap: {"Pacific/Honolulu":"northamerica","Atlantic/Bermuda":"northamerica","Atlantic/Cape_Verde":"africa","Atlantic/St_Helena":"africa","Indian/Kerguelen":"antarctica","Indian/Chagos":"asia","Indian/Maldives":"asia","Indian/Christmas":"australasia","Indian/Cocos":"australasia","America/Danmarkshavn":"europe","America/Scoresbysund":"europe","America/Godthab":"europe","America/Thule":"europe","Asia/Yekaterinburg":"europe","Asia/Omsk":"europe","Asia/Novosibirsk":"europe","Asia/Krasnoyarsk":"europe","Asia/Irkutsk":"europe","Asia/Yakutsk":"europe","Asia/Vladivostok":"europe","Asia/Sakhalin":"europe","Asia/Magadan":"europe","Asia/Kamchatka":"europe","Asia/Anadyr":"europe","Africa/Ceuta":"europe","America/Argentina/Buenos_Aires":"southamerica","America/Argentina/Cordoba":"southamerica","America/Argentina/Tucuman":"southamerica","America/Argentina/La_Rioja":"southamerica","America/Argentina/San_Juan":"southamerica","America/Argentina/Jujuy":"southamerica","America/Argentina/Catamarca":"southamerica","America/Argentina/Mendoza":"southamerica","America/Argentina/Rio_Gallegos":"southamerica","America/Argentina/Ushuaia":"southamerica","America/Aruba":"southamerica","America/La_Paz":"southamerica","America/Noronha":"southamerica","America/Belem":"southamerica","America/Fortaleza":"southamerica","America/Recife":"southamerica","America/Araguaina":"southamerica","America/Maceio":"southamerica","America/Bahia":"southamerica","America/Sao_Paulo":"southamerica","America/Campo_Grande":"southamerica","America/Cuiaba":"southamerica","America/Porto_Velho":"southamerica","America/Boa_Vista":"southamerica","America/Manaus":"southamerica","America/Eirunepe":"southamerica","America/Rio_Branco":"southamerica","America/Santiago":"southamerica","Pacific/Easter":"southamerica","America/Bogota":"southamerica","America/Curacao":"southamerica","America/Guayaquil":"southamerica","Pacific/Galapagos":"southamerica","Atlantic/Stanley":"southamerica","America/Cayenne":"southamerica","America/Guyana":"southamerica","America/Asuncion":"southamerica","America/Lima":"southamerica","Atlantic/South_Georgia":"southamerica","America/Paramaribo":"southamerica","America/Port_of_Spain":"southamerica","America/Montevideo":"southamerica","America/Caracas":"southamerica"},
    _loadedFiles: {},
    _loadedRegions: {},

    getTimezone: function(tzid){
        var tz = this._timezones[tzid];
        if (tz){
            return tz;
        }

        //check the exceptions map
        var file = this._exceptionsMap[tzid];
        if (!file){
            file = this._prefixToFileMap[tzid.split("/")[0]];
        }

        if (file && this.loadFile(file)){
            return this.getTimezone(tzid);
        }

        var link = this.getLink(tzid);

        if (link){
            return this.getTimezone(link);
        }

        return null;
    },

    loadFile: function(file){
        if (!this._loadedFiles[file]){
            this._parseUri(file);
            this._loadedFiles[file] = true;
            return true;
        }
        return false;
    },

    addTimezone: function(timezone){
        this._timezones[timezone.tzId] = timezone;
        this._addTzToTzsByRegion(timezone.tzId);
    },

    getTzIdsForRegion: function(region){
        if (!this._loadedRegions[region]){
            this.loadFile(this._prefixToFileMap[region]);
            for (var exception in this._exceptionsMap){
                if (dojo.string.startsWith(exception, region)){
                    this.loadFile(this._exceptionsMap[exception]);
                }
            }
            this._loadedRegions[region] = true;
        }
        return this._tzsByRegion[region] || [];
    },

    getLink: function(tzid){
        this.loadFile("backward");
        return this._links[tzid];
    }


});
