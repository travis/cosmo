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

dojo.provide("cosmotest.service.translators.test_eim");

dojo.require("cosmo.service.translators.eim");
dojo.require("cosmo.service.common");
dojo.require("cosmo.datetime.serialize");
dojo.require("cosmo.util.html");
//Initialization.
//TODO - once Dojo implements setUp() and tearDown() move this code there.

cosmotest.service.translators.test_eim = {
    translator: new cosmo.service.translators.Eim(
       new cosmo.service.UrlCache()
    ),
    
    test_getTicketType: function(){
        var t = cosmotest.service.translators.test_eim.translator;
        var doc = cosmotest.service.translators.test_eim.toXMLDocument("<?xml version='1.0' encoding='UTF-8'?>" +
                                                                        '<feed xmlns:cosmo="http://osafoundation.org/cosmo/Atom" ' +
                                                                        'xmlns="http://www.w3.org/2005/Atom" xml:base="http://localhost:8080/chandler/atom/">' +
                                                                        '  <cosmo:ticket cosmo:type="read-write">fjgsm7key0</cosmo:ticket>' +
                                                                       '</feed>');
        var ticketEl = cosmo.util.html.getElementsByTagName(doc, "cosmo", "ticket")[0];
        jum.assertTrue("ticket exists", !!ticketEl);
        jum.assertEquals("tag name", "cosmo:ticket", ticketEl.tagName);
        jum.assertEquals("type is read-write", "read-write", t.getTicketType(ticketEl));
    },
    
    test_parseRecordSet: function (){
        var uid = "12345";
        var title = "o1";
        var triageStatus = "100";
        var autoTriage = "1";
        var rank = "-12345.67";
        var createdOn = 1178053319;
        var dtstartDateString = "20001231T000000";
        var dtstart = ";VALUE=DATE-TIME:" + dtstartDateString;
        var a = cosmotest.service.translators.test_eim.generateAtom(
            cosmotest.service.translators.test_eim.getEimRecordset({
              uuid: uid,
              title: title,
              triage: triageStatus + " " + rank + " " + autoTriage,
              createdOn: createdOn,
              dtstart: dtstart
              
            }));
        var objectList = cosmotest.service.translators.test_eim.translator.translateGetItems(a);
        var o1 = objectList[0];
        jum.assertEquals("uid does not match", uid, o1.getUid());
        jum.assertEquals("display name does not match title", title, o1.getDisplayName());
        jum.assertEquals("triage status does not match", triageStatus, o1.getTriageStatus());
        jum.assertEquals("auto triage does not match", autoTriage, o1.getAutoTriage());
        jum.assertEquals("triage rank does not match", rank, o1.getRank());
        jum.assertEquals("creation date does not match created on", createdOn, o1.getCreationDate() / 1000);

        var e1 = o1.getStamp("event");
        jum.assertTrue("dtstart does not match start date", 
              cosmo.datetime.fromIso8601(dtstartDateString).equals(e1.getStartDate()));
              
    },

    test_parseRRuleFreq: function (){
        var rule = "FREQ=DAILY;UNTIL=20000131T090000Z;"
        var startDate = cosmo.datetime.fromIso8601("20000101T090000Z");

        var rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule, startDate);
       
        jum.assertEquals("wrong frequency", 
                         cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY, 
                         rrule.getFrequency());

        var until = cosmo.datetime.fromIso8601("20000131T000000Z");
        jum.assertTrue("wrong date", until.equals(rrule.getEndDate()));
                         
        jum.assertTrue("wrong supported value", rrule.isSupported());

        rule = "FREQ=WEEKLY;UNTIL=20000131T090000Z;"
        rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule, startDate);

        jum.assertEquals("wrong frequency",
                         cosmo.model.RRULE_FREQUENCIES.FREQUENCY_WEEKLY, 
                         rrule.getFrequency());

        rule = "FREQ=WEEKLY;INTERVAL=2;UNTIL=20000131T090000Z;"
        rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule, startDate);

        jum.assertEquals("wrong frequency", 
                         cosmo.model.RRULE_FREQUENCIES.FREQUENCY_BIWEEKLY, 
                         rrule.getFrequency());

        rule = "FREQ=MONTHLY;UNTIL=20000131T090000Z;"
        rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule, startDate);

        jum.assertEquals("wrong frequency", 
                         cosmo.model.RRULE_FREQUENCIES.FREQUENCY_MONTHLY, 
                         rrule.getFrequency());

        rule = "FREQ=YEARLY;UNTIL=20000131T090000Z;"
        rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule, startDate);

        jum.assertEquals("wrong frequency", 
                         cosmo.model.RRULE_FREQUENCIES.FREQUENCY_YEARLY, 
                         rrule.getFrequency());
    },
    
    test_parseRRuleUnsupported: function (){
        var rule = "FREQ=DAILY;UNTIL=20000131T090000Z;BYMONTH=1"
        var rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule);
        
        jum.assertFalse("bymonth not supported", rrule.isSupported());
        jum.assertTrue("unsupported rule not saved", !!rrule.getUnsupportedRule());
        jum.assertEquals("bymonth wrong in unsupported rule", 1, 
                         rrule.getUnsupportedRule().bymonth);

        rule = "FREQ=WEEKLY;COUNT=10"
        rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule);
        
        jum.assertFalse("count not supported", rrule.isSupported());
        jum.assertTrue("unsupported rule not saved", !!rrule.getUnsupportedRule());
        jum.assertEquals("count wrong in unsupported rule", 10, 
                         rrule.getUnsupportedRule().count);
        
        rule = "FREQ=WEEKLY;INTERVAL=2;UNTIL=19971224T000000Z;WKST=SU;BYDAY=MO,WE,FR"
        rrule = cosmotest.service.translators.test_eim.translator.parseRRule(rule);
        
        jum.assertFalse("byday not supported", rrule.isSupported());
        jum.assertTrue("unsupported rule not saved", !!rrule.getUnsupportedRule());
        jum.assertEquals("wkst wrong in unsupported rule", "SU", 
                         rrule.getUnsupportedRule().wkst);
        jum.assertEquals("count wrong in unsupported rule", ["MO","WE", "FR"].toString(), 
                         rrule.getUnsupportedRule().byday.toString());
        
        
    },
    
    test_itemToRecordSet: function(){
        var note = new cosmo.model.Note({
            uid: "12345",
            displayName: "test calendar",
            triageStatus: 200,
            autoTriage: true,
            rank: -12345.67
        });
        var recordSet = cosmotest.service.translators.test_eim.translator.noteToRecordSet(note);
        jum.assertEquals("12345", recordSet.uuid);
        jum.assertEquals("test calendar", recordSet.records.item.fields.title[1]);
        jum.assertEquals("200 -12345.67 1", recordSet.records.item.fields.triage[1]);
        
        note.getStamp("event", true, {
            startDate: new cosmo.datetime.Date(2007, 5, 7, 12, 30, 45),
            duration: new cosmo.model.Duration({
                hour: 2,
                minute: 30,
                second: 45
            }),
            anyTime: true,
            allDay: false,
            location: "home",
            rrule: new cosmo.model.RecurrenceRule({
                frequency: cosmo.model.RRULE_FREQUENCIES.FREQUENCY_DAILY,
                endDate: new cosmo.datetime.Date(2007, 5, 14)
            })
        });
        var recordSet = cosmotest.service.translators.test_eim.translator.noteToRecordSet(note);
        jum.assertEquals("location", "home", recordSet.records.event.fields.location[1]);
        jum.assertEquals("duration", "PT2H30M45S", recordSet.records.event.fields.duration[1]);
        jum.assertEquals("dtstart", ";X-OSAF-ANYTIME=TRUE;VALUE=DATE:20070607", 
                        recordSet.records.event.fields.dtstart[1]);                         
        // Test null duration

        note.getEventStamp().setDuration(null);
        recordSet = cosmotest.service.translators.test_eim.translator.noteToRecordSet(note);
        jum.assertEquals("duration", "PT0S", recordSet.records.event.fields.duration[1]);

        var occurrenceDate = new cosmo.datetime.Date(2007, 5, 8, 12, 30, 45);
        note.getNoteOccurrence(occurrenceDate);
        
        var modification = new cosmo.model.Modification(
        {
            recurrenceId: occurrenceDate
        }
        );
        note.addModification(modification);
        
        //TODO: add translation and asserts
    },
  
    generateAtom: function (/*Object*/ content){
       var uuid = content.uuid;
        return cosmotest.service.translators.test_eim.toXMLDocument('<?xml version=\'1.0\' encoding=\'UTF-8\'?>' +
        '<feed xmlns="http://www.w3.org/2005/Atom" xmlns:xml="http://www.w3.org/XML/1998/namespace" xml:base="http://localhost:8080/cosmo/atom/">' +
        '<id>urn:uuid:56599b95-6676-4823-8c88-1eec17058f48</id>' +
        '<title type="text">Cosmo</title>' +
        '<generator uri="http://cosmo.osafoundation.org/" version="0.7.0-SNAPSHOT">Cosmo Sharing Server</generator>' +
        '<author><name>root</name><email>root@localhost</email></author>' +
        '<link rel="self" type="application/atom+xml" href="collection/56599b95-6676-4823-8c88-1eec17058f48/full/eim-json" />' +
        '<link rel="alternate" type="text/html" href="http://localhost:8080/cosmo/pim/collection/56599b95-6676-4823-8c88-1eec17058f48" />' +
        '<entry><id>urn:uuid:' + uuid + '</id>' +
        '<title type="text">Welcome to Cosmo</title>' +
        '<updated>2007-05-01T21:01:59.535Z</updated><published>2007-05-01T21:01:59.535Z</published>' +
        '<link rel="self" type="application/atom+xml" href="item/' + uuid + '/full/eim-json" />' +
        '<content type="text/eim+json">' + 
        dojo.json.serialize(content) + 
        '</content>' +
        '<link rel="edit" type="application/atom+xml" href="item/' + uuid + '"/>' +
        '<link rel="parent" type="application/atom+xml" href="collection/56599b95-6676-4823-8c88-1eec17058f48/full/eim-json" />' +
        '</entry></feed>')

    },
    
    toXMLDocument: function (/*String*/ text){
        if (window.ActiveXObject)
          {
          var doc=new ActiveXObject("Microsoft.XMLDOM");
          doc.async="false";
          doc.loadXML(text);
          }
        // code for Mozilla, Firefox, Opera, etc.
        else
          {
          var parser=new DOMParser();
          var doc=parser.parseFromString(text,"text/xml");
          }
          return doc;
    },
    
    getEimRecordset: function (/*Object*/ props){
        props = props || {};
        
        var recordSet = 
        {"uuid":props.uuid || "0017e507-e087-487a-8eae-63afa7d865b5",
         "records":{
            "item":{"ns":"http://osafoundation.org/eim/item/0",
                    "key":{"uuid":["text", props.uuid || "0017e507-e087-487a-8eae-63afa7d865b5"]},
                    "fields":{"title":["text", props.title || "Welcome to Cosmo"],
                              "triage":["text", props.triage || "100 -1178053319.00 1"],
                              "hasBeenSent":["integer", props.hasBeenSent || "0"],
                              "needsReply":["integer", props.needsReply || "0"],
                              "createdOn":["decimal", props.createdOn || "1178053319"]}},
            "modby":{"ns":"http://osafoundation.org/eim/modifiedBy/0",
                     "key":{"uuid":["text", props.uuid || "0017e507-e087-487a-8eae-63afa7d865b5"],
                            "userid":["text", props.userid || "root@localhost"],
                            "timestamp":["decimal", props.timestamp || "1178053319"],
                            "action":["integer", props.action || "500"]}},
            "note":{"ns":"http://osafoundation.org/eim/note/0",
                    "key":{"uuid":["text", props.uuid || "0017e507-e087-487a-8eae-63afa7d865b5"]},
                    "fields":{"body":["clob", props.body || "Welcome to Cosmo"],
                              "icalUid":["text", props.icalUid || "dfad540e-eae5-4792-9d2d-d9642fcfc411"]}},
            "event":{"ns":"http://osafoundation.org/eim/event/0",
                     "key":{"uuid":["text", props.uuid || "0017e507-e087-487a-8eae-63afa7d865b5"]},
                     "fields":{"dtstart":["text", props.dtstart || ";=DATE-TIME:00021231T000000"],
                               "duration":["text", props.duration || "PT0S"],
                               "location":["text", props.location || ""],
                               "rrule":["text", props.rrule || null],
                               "exrule":["text",props.exrule || null],
                               "rdate":["text", props.rdate || null],
                               "exdate":["text", props.exdate || null],
                               "lastPastOccurrence":["text", props.lastPastOccurrence || null],
                               "status":["text", props.status || null]}}}};
        return recordSet;
        
    }
}