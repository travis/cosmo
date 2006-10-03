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
    var opts = cosmo.view.cal.recurringEventOptions;
    
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
        'btnsRight': [
            new Button('allButtonDialog', 84, 
                function() { doPublish('remove', opts.ALL_EVENTS); }, 'All Events', true), 
            new Button('allFutureButtonDialog', 120, 
                function() { doPublish('remove', opts.ALL_FUTURE_EVENTS); }, 'All Future Events', true),
            new Button('onlyThisButtonDialog', 120, 
                function() { doPublish('remove', opts.ONLY_THIS_EVENT); }, 'Only This Event', true)
            ],
        'defaultAction': function() {},
        'width': 480,
        'msg': 'This is a recurring event. Which occurrences do you wish to delete?'
    };
    props.saveRecurConfirm = {
        'type': Cal.dialog.CONFIRM,
        'btnsLeft': [new Button('cancelButtonDialog', 74, 
            function() { doEvMethod('cancelSave') },
            getText('App.Button.Cancel'), true)],
        'btnsRight': [
            new Button('allButtonDialog', 84, 
                function() { doPublish('save', opts.ALL_EVENTS); }, 'All Events', true), 
            new Button('allFutureButtonDialog', 120, 
                function() { doPublish('save', opts.ALL_FUTURE_EVENTS); }, 'All Future Events', true),
            new Button('onlyThisButtonDialog', 120, 
                function() { doPublish('save', opts.ONLY_THIS_EVENT); }, 'Only This Event', true)
            ],
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
    this.getProps = function(key) {
        return props[key];
    };
};
cosmo.view.cal.dialog.constructor = null;



