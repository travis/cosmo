/*
 * Copyright 2008 Open Source Applications Foundation
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

dojo.provide("cosmo.model.tests.sort");
dojo.require("cosmo.model.util");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.model.EventStamp");
dojo.require("cosmo.tests.jum");
dojo.require("cosmo.view.list.common");
dojo.require("cosmo.view.list.sort");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.datetime.Date");

// shorten package name for legibility
list = cosmo.view.list;
add = dojo.hitch(cosmo.datetime.Date, cosmo.datetime.Date.add);
DAY_INTERVAL = cosmo.datetime.util.dateParts.DAY;
LATER = cosmo.model.TRIAGE_LATER;
// because rank is quantized to the nearest 10ms, rank won't
// be consistently monotonic, so it needs to be tweaked to let
// tests work
function initNote(rankTweak){
    var note = new cosmo.model.Note();
    note.setRank(note.getRank() - rankTweak);
    return note;
}

function initEvent(rankTweak, daysToAdd){
    var note = initNote(rankTweak);
    var stamp = note.getEventStamp(true);
    var startDate = add(new cosmo.datetime.Date(), DAY_INTERVAL, daysToAdd);
    var endDate = add(startDate, DAY_INTERVAL, 1);
    stamp.setStartDate(startDate);
    stamp.setEndDate(endDate);
    return note;
}

doh.register("cosmo.model.tests.sort", [
    function triageSort (){
        var itemList = [];

        var note1 = initNote(1);
        itemList.push(note1);

        var note2 = initEvent(2, 1);
        itemList.push(note2);

        var note3 = initEvent(3);
        itemList.push(note3);

        var laterNote4 = initEvent(4, 3);
        laterNote4.setTriageStatus(LATER);
        itemList.push(laterNote4);

        var laterNote5 = initEvent(5, 2);
        laterNote5.setTriageStatus(LATER);
        itemList.push(laterNote5);

        var laterNote6 = initEvent(6, 4);
        laterNote6.setTriageStatus(LATER);
        itemList.push(laterNote6);

        var laterNote7 = initNote(7);
        laterNote7.setTriageStatus(LATER);
        itemList.push(laterNote7);

        var itemRegistry = list.createItemRegistry(itemList);
        // sort the item registry with the default sort, Triage, ascending
        list.sort.doSort(itemRegistry, 'Triage');
        jum.assertFalse(note1.getRank() == note2.getRank());
        // my kingdom for array comprehensions that work in our supported browsers
        var expectedOrder = [note3.getUid(), note2.getUid(), note1.getUid(),
                             laterNote5.getUid(), laterNote4.getUid(), laterNote6.getUid(),
                             laterNote7.getUid()];
        jum.assertEquals(expectedOrder, itemRegistry.order);

    }]);

