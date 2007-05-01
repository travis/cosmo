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

dojo.provide("cosmo.service.atom");

dojo.require("cosmo.model");
dojo.require("cosmo.service.eim");
dojo.require("cosmo.util.auth");
dojo.require("dojo.date.serialize");
dojo.require("dojo.date.common");
dojo.require("dojo.json");

dojo.declare("ParseError", [Error]);

cosmo.service.atom = {

    _wrapXMLHandlerFunctions: function (/*Object*/ hDict,
                                        /*function*/ xmlParseFunc){
            var self = this;
            var handlerDict = dojo.lang.shallowCopy(hDict);
            if (handlerDict.load){
                var old_load = handlerDict.load;
                handlerDict.load = function(type, data, evt){

                    var xmlDoc = evt.responseXML;
                    if (document.all){
                        xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
                        xmlDoc.async="false";
                        xmlDoc.loadXML(evt.responseText);
                    }
                    var parsedCMPXML = xmlParseFunc.apply(self, [xmlDoc])

                    old_load(type, parsedCMPXML, evt);
                }
            }

			return handlerDict;
        },


    getDefaultEIMRequest: function (/*Object*/ handlerDict,
                                    /*boolean?*/ sync){

        var request = cosmo.util.auth.getAuthorizedRequest()

        request.load = handlerDict.load;
        request.handle =  handlerDict.handle;
        request.error = handlerDict.error;
        request.transport = "XMLHTTPTransport";
        request.contentType =  'text/xml';
        request.sync = handlerDict.sync;
        request.headers["Cache-Control"] = "no-cache";
        request.headers["Pragma"] = "no-cache";
        request.preventCache = true;

        return request;
    },

    getEvents: function(uid, kwArgs){
       if (!kwArgs){
           kwArgs = {};
       }

       var returnValue;
       if (kwArgs.sync){

           kwArgs.handle = kwArgs.load = function(type, data, xhr){
                               returnValue = data;
                           };
       }
       kwArgs = this._wrapXMLHandlerFunctions(kwArgs, this.atomToItemList);

       var request = this.getDefaultEIMRequest(kwArgs);
       request.url = "/cosmo/atom/collection/" + uid + "/full";
       request.method = "GET";

       dojo.io.bind(request);
       if (kwArgs.sync){
           return returnValue;
       }
    },

    saveEvent: function(event, kwArgs){
       if (!kwArgs){
           kwArgs = {};
       }
       var requestId = Math.floor(Math.random() * 100000000000000000);

       var oldLoad = kwArgs.load;
       kwArgs.load = function(){
           oldLoad(event.id, null, requestId)};
       kwArgs.error = function(){
           dojo.debug("save failed")
       }

       if (navigator.userAgent.indexOf('Safari') > -1 ||
           document.all){
           kwArgs = this._wrap204Bandaid(kwArgs);
       }

       var request = this.getDefaultEIMRequest(kwArgs);
       var content = '<?xml version="1.0" ?>' +
                       this.objectToAtomEntry(event)

       request.url = event.data.editLink;
       request.method = "POST";
       request.contentType = "application/atom+xml";
       request.headers['X-Http-Method-Override'] = "PUT";
       request.postContent = content;

       dojo.io.bind(request);
       return requestId;

    },

    atomToItemList: function(atomXml){
        if (!atomXml){
            throw new ParseError("Cannot parse null, undefined, or false");
        }

        var entries = atomXml.getElementsByTagName("entry");
        var items = [];
        for (var i = 0; i < entries.length; i++){
            var c = entries[i].getElementsByTagName("content")[0];
            var content = c.firstChild.nodeValue;
            var item = this.recordSetToObject(eval("(" + content + ")"))

            var links;
            // Safari doesn't find paging links with non NS call
            if (dojo.render.html.safari){
               links = entries[i].getElementsByTagNameNS('*', "link");
            } else {
               links = entries[i].getElementsByTagName("link");
            }

            for (var j = 0; j < links.length; j++){
                var link = links[j];
                if (link.getAttribute('rel') == 'edit'){
                    item.editLink = link.getAttribute('href');
                };
            }

            items.push(item);
        }
        return items;
    },

    objectToAtomEntry: function(object){

         var jsonObject = this.objectToRecordSet(object);



         return '<entry xmlns="http://www.w3.org/2005/Atom">' +
         '<title>' + object.data.title + '</title>' +
         '<id>urn:uuid:' + object.data.id + '</id>' +
         '<updated>' + dojo.date.toRfc3339(new Date()) + '</updated>' +
         '<author><name>' + cosmo.util.auth.getUsername() + '</name></author>' +
         '<content type="application/eim+json">' + dojo.json.serialize(jsonObject) + '</content>' +
         '</entry>'
    },

    objectToRecordSet: function(obj){

        var object = obj.data;
        with (cosmo.service.eim.constants){
            return {
                uuid: object.id,
                records: {
                    item: this.objectToItemRecord(object),
                    note: this.objectToNoteRecord(object),
                    event: this.objectToEventRecord(object),
                    modby: this.objectToModbyRecord(object)
                }
            }
        }
    },

    objectToItemRecord: function(object){

        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.ITEM,
                ns: ns.ITEM,
                keys: {
                    uuid: [type.TEXT, object.id]
                },
                fields: {
                    title: [type.TEXT, object.title || null]
                }
            }
        }
    },

    objectToNoteRecord: function(object){

        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.NOTE,
                ns: ns.NOTE,
                keys: {
                    uuid: [type.TEXT, object.id]
                },
                fields: {

                    body: [type.CLOB, object.description || ""]
                }
            }
        }
    },

    objectToEventRecord: function(object){

        with (cosmo.service.eim.constants){
            var fields = {
               dtstart: [type.TEXT, ";VALUE=" +
                        (object.allDay || object.anyTime? "DATE" : "DATE-TIME") +
                        (object.anyTime? ";X-OSAF-ANYTIME=TRUE" : "") +
                        ":" +
                        (object.allDay || object.anyTime?
                         object.start.strftime("%Y%m%d"):
                         object.start.strftime("%Y%m%dT%H%M%S"))],

                //anytime: [type.INTEGER, object.anyTime? 1 : 0],
                //rrule: [type.TEXT, ""],//FIXME
                status: [type.TEXT, object.status || null],
                location: [type.TEXT, object.location || null]

            };
            if (!(object.allDay || object.anyTime)){
                fields.duration =
                    [type.TEXT,
                     cosmo.datetime.getIso8601Duration(
                         object.start, object.end)
                    ]
            }


            return {
                prefix: prefix.EVENT,
                ns: ns.EVENT,
                keys: {
                    uuid: [type.TEXT, object.id]
                },
                fields: fields

            }
        }

    },

    objectToModbyRecord: function(object){
        with (cosmo.service.eim.constants){
            return {
                prefix: prefix.MODBY,
                ns: ns.MODBY,
                keys:{
                    uuid: [type.TEXT, object.id],
                    userid: [type.TEXT, ""],
                    action: [type.INTEGER, 100],
                    timestamp: [type.DECIMAL, new Date().getTime()]
                }
            }
        }
    },

    recordSetToObject: function(recordSet){

        var uid  = recordSet.uuid;
        var title;
        var description;
        var start;
        var end;
        var allDay;
        var pointInTime;
        var anyTime;
        var recurrenceRule;
        var status;
        var masterEvent;
        var instance;
        var instanceDate;
        var loc;

        for (recordName in recordSet.records){
        with (cosmo.service.eim.constants){

           var record = recordSet.records[recordName]

           if (recordName == prefix.ITEM){
               title = record.fields.title[1];
           }

           if (recordName == prefix.NOTE){
               description = record.fields.body[1];
           }

           if (recordName == prefix.EVENT){
               var dateParams = this.dateParamsFromEimDate(record.fields.dtstart[1]);

               start = this.fromEimDate(record.fields.dtstart[1]);

               end = dojo.date.addIso8601Duration(start.clone(),
                                                  record.fields.duration[1]);

               anyTime = dateParams.anyTime;

               allDay = (!anyTime) && dateParams.value== "date";

               recurrenceRule = null; //FIXME

               status = record.fields.status[1];

               masterEvent = null; //FIXME

               instance = null; //FIXME

               instanceDate = null; //FIXME

               loc = record.fields.location[1];
           }
           }
        }

        return calEventData = new cosmo.model.CalEventData(
           uid, title, description, start, end, allDay,
           pointInTime, anyTime, recurrenceRule, status,
           masterEvent, instance, instanceDate, loc);

    },

    fromEimDate: function(dateString){
        var date = dateString.split(":")[1];
        return new cosmo.datetime.Date(dojo.date.fromIso8601(date));

    },

    dateParamsFromEimDate: function(dateString){
        var returnVal = {};
        var params = dateString.split(":")[0].split(";");
        for (var i = 0; i < params.length; i++){
            var param = params[i].split("=");
            if (param[0].toLowerCase() == "x-osaf-anytime") {
                returnVal.anyTime = true;
            }
            if (param[0].toLowerCase() == "value") {
                returnVal.value = param[1].toLowerCase();
            }
        }
        return returnVal;
    },

    anyTimeFromEimDate: function(dateString){

        return false;

    },

       _wrap204Bandaid: function(hDict){
            var handlerDict = dojo.lang.shallowCopy(hDict);

            if (handlerDict.load){
                handlerDict.load = this._204Bandaid(handlerDict.load);
            }
            if (handlerDict.error){
                handlerDict.error = this._204Bandaid(
                    handlerDict.error, handlerDict.load);
            }
            if (handlerDict.handle){
                handlerDict.handle = this._204Bandaid(handlerDict.handle);
            }

            return handlerDict;
        },

        _204Bandaid: function(originalFunc, handle204Func){
            // Use original function if handle204Func is not specified.
            handle204Func = handle204Func? handle204Func: originalFunc;
            return function(type, data, evt){
                if (navigator.userAgent.indexOf('Safari') > -1 &&
                    !evt.status) {

                    var newEvt = dojo.lang.shallowCopy(evt);
                    newEvt.status = 204;
                    newEvt.statusText = "No Content";
                    handle204Func('load', '', newEvt);

                }

                // If we're Internet Explorer
                else if (document.all &&
                         evt.status == 1223){
                    // apparently, shallow copying the XHR object in ie
                    // causes problems.
                    var newEvt = {};
                    newEvt.status = 204;
                    newEvt.statusText = "No Content";
                    handle204Func('load', '', newEvt);
                } else {
                    originalFunc(type, data, evt);
                }
            }
        },


    test: function(){
        return this.getEvents("1ea37892-a753-403e-b641-7d19594860e7", {sync: true});
    }
}




dojo.date.addIso8601Duration = function (date, duration){

    var r = "^P(?:(?:([0-9\.]*)Y)?(?:([0-9\.]*)M)?(?:([0-9\.]*)D)?(?:T(?:([0-9\.]*)H)?(?:([0-9\.]*)M)?(?:([0-9\.]*)S)?)?)(?:([0-9/.]*)W)?$"
    var dateArray = duration.match(r).slice(1);
    with(cosmo.datetime.dateParts){
        dateArray[0]? date.add(YEAR, dateArray[0]) : null;
        dateArray[1]? date.add(MONTH, dateArray[1]) : null;
        dateArray[2]? date.add(DAY, dateArray[2]) : null;
        dateArray[3]? date.add(HOUR, dateArray[3]) : null;
        dateArray[4]? date.add(MINUTE, dateArray[4]) : null;
        dateArray[5]? date.add(SECOND, dateArray[5]) : null;
    }

    return date;
}

cosmo.datetime.getIso8601Duration = function(dt1, dt2){

   return "P" +// + this.Date.diff('yyyy', dt1, dt2) + "Y" +
          //this.Date.diff('m', dt1, dt2) + "M" +
          //this.Date.diff('d', dt1, dt2) + "D" +
          'T' +
          //this.Date.diff('h', dt1, dt2) + "H" +
          //this.Date.diff('n', dt1, dt2) + "M" +
          this.Date.diff('s', dt1, dt2) + "S";
}
