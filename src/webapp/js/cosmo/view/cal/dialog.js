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

dojo.provide('cosmo.view.cal.dialog');

cosmo.view.cal.dialog = new function() {
    
    var self = this;
    var props = {}; // Props for each modal dialog
    var recurOpts = cosmo.view.cal.recurringEventOptions;
    var allEventsMsg = 'All Events';
    var AllFutureMsg = 'All Future Events';
    var OnlyThisMsg = 'Only This Event';
    var btnWideWidth = 84;
    var btnWiderWidth = 120;
    // All available buttons for the dialog
    var b = {
        'removeAllEvents': new Button('allButtonDialog', btnWideWidth, 
            function() { doPublish('remove', recurOpts.ALL_EVENTS); }, allEventsMsg, true), 
        'removeFutureEvents': new Button('allFutureButtonDialog', btnWiderWidth, 
            function() { doPublish('remove', recurOpts.ALL_FUTURE_EVENTS); }, AllFutureMsg, true),
        'removeOnlyThisEvent': new Button('onlyThisButtonDialog', btnWiderWidth, 
            function() { doPublish('remove', recurOpts.ONLY_THIS_EVENT); }, OnlyThisMsg, true),
        'saveAllEvents': new Button('allButtonDialog', btnWideWidth, 
            function() { doPublish('save', recurOpts.ALL_EVENTS); }, allEventsMsg, true), 
        'saveFutureEvents': new Button('allFutureButtonDialog', btnWiderWidth, 
            function() { doPublish('save', recurOpts.ALL_FUTURE_EVENTS); }, AllFutureMsg, true),
        'saveOnlyThisEvent': new Button('onlyThisButtonDialog', btnWiderWidth, 
            function() { doPublish('save', recurOpts.ONLY_THIS_EVENT); }, OnlyThisMsg, true),
        'allEventsDisabled': new Button('allButtonDialog', btnWideWidth, 
            null, allEventsMsg, true), 
        'futureEventsDisabled': new Button('allFutureButtonDialog', btnWiderWidth, 
            null, AllFutureMsg, true)
    };
    
    props.removeConfirm = {
        'type': Cal.dialog.CONFIRM,
        'btnsLeft': [new Button('cancelButtonDialog', 74, Cal.hideDialog,
            getText('App.Button.Cancel'), true)],
        'btnsRight': [new Button('removeButtonDialog', 74, function() { doPublish('remove'); },
            getText('App.Button.Remove'), true)],
        'defaultAction': function() { doPublish('remove'); },
        'msg': getText('Main.Prompt.EventRemoveConfirm')
    };
    props.removeRecurConfirm = {
        'type': Cal.dialog.CONFIRM,
        'btnsLeft': [new Button('cancelButtonDialog', 74, Cal.hideDialog,
            getText('App.Button.Cancel'), true)],
        'btnsRight': [],
        'defaultAction': function() {},
        'width': 480,
        'msg': 'This is a recurring event. Which occurrences do you wish to remove?'
    };
    props.saveRecurConfirm = {
        'type': Cal.dialog.CONFIRM,
        'btnsLeft': [new Button('cancelButtonDialog', 74, 
            function() { doEvMethod('cancelSave') },
            getText('App.Button.Cancel'), true)],
        'btnsRight': [],
        'defaultAction': function() {},
        'width': 480,
        'msg': 'This is a recurring event. Which occurrences do you wish to change?'
    };
    
    // Publish via topics
    function doPublish(act, qual) {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        var obj = {};
        obj.action = act; // Action to publish
        obj.data = selEv || null; // Selected cal event to act on
        if (qual) { obj.qualifier = qual; } // Action qualifier
        dojo.event.topic.publish('/calEvent', obj);
    }
    // Call a method on the currently selected event
    // FIXME: Use topics
    function doEvMethod(key) {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        selEv[key]();
    }
    
    // Public methods
    // ********************
    this.getProps = function(key, optsParam) {
        var p = props[key];
        var opts = optsParam || {};
        var instanceOnly = opts.instanceOnly || false;
        if (key == 'saveRecurConfirm') {
            p.btnsRight = [];
            if (instanceOnly) {
                p.btnsRight.push(b.allEventsDisabled);
                p.btnsRight.push(b.saveFutureEvents);
            }
            else {
                p.btnsRight.push(b.saveAllEvents);
                if (!opts.masterEvent) {
                    p.btnsRight.push(b.saveFutureEvents);
                }
            }
            p.btnsRight.push(b.saveOnlyThisEvent);
        }
        else if (key == 'removeRecurConfirm') {
            p.btnsRight = [];
            p.btnsRight.push(b.removeAllEvents);
            if (!opts.masterEvent) {
                p.btnsRight.push(b.removeFutureEvents);
            }
            p.btnsRight.push(b.removeOnlyThisEvent);
            
        }
        return p;
    };
};
cosmo.view.cal.dialog.constructor = null;



