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

dojo.provide('cosmo.view.dialog');

dojo.require("cosmo.util.i18n");

dojo.require("cosmo.convenience");
var _ = cosmo.util.i18n.getText;

cosmo.view.dialog = new function() {

    var self = this;
    var props = {}; // Props for each modal dialog
    var recurOpts = cosmo.view.cal.recurringEventOptions;
    var allEventsMsg = 'All Events';
    var AllFutureMsg = 'All Future Events';
    var OnlyThisMsg = 'Only This Event';
    var btnWideWidth = 84;
    var btnWiderWidth = 120;


    // All available buttons for the dialog
    var buttons = {
        'removeAllEvents': function(){
           return new Button('allButtonDialog', btnWideWidth,
                function() { doPublish('remove', cosmo.view.cal.recurringEventOptions.ALL_EVENTS); }, allEventsMsg, true);
        },

        'removeFutureEvents': function(){
            return new Button('allFutureButtonDialog', btnWiderWidth,
                function() { doPublish('remove', cosmo.view.cal.recurringEventOptions.ALL_FUTURE_EVENTS); }, AllFutureMsg, true);
        },

        'removeOnlyThisEvent': function(){
            return new Button('onlyThisButtonDialog', btnWiderWidth,
                function() { 
                    doPublish('remove', cosmo.view.cal.recurringEventOptions.ONLY_THIS_EVENT); 
                }, 
                OnlyThisMsg, true);
        },

        'saveAllEvents': function(saveItem, delta){
            return new Button('allButtonDialog', btnWideWidth,
                function() {  
                    doPublishSave(cosmo.view.cal.recurringEventOptions.ALL_EVENTS,
                        saveItem, delta)
                }, 
                allEventsMsg, 
                true);
        },

        'saveFutureEvents': function(saveItem, delta){
            return new Button('allFutureButtonDialog', btnWiderWidth,
                function() { 
                    doPublishSave(cosmo.view.cal.recurringEventOptions.ALL_FUTURE_EVENTS,
                        saveItem, delta); 
                },
                AllFutureMsg, true);
        },

        'saveOnlyThisEvent': function(saveItem, delta){
            return new Button('onlyThisButtonDialog', btnWiderWidth,
                function() { 
                    doPublishSave(cosmo.view.cal.recurringEventOptions.ONLY_THIS_EVENT,
                        saveItem, 
                        delta); 
                }, 
                OnlyThisMsg, true);
        },

        'allEventsDisabled': function(){
            return new Button('allButtonDialog', btnWideWidth,
                null, allEventsMsg, true, true);
        },

        'futureEventsDisabled': function(){
            return new Button('allFutureButtonDialog', btnWiderWidth,
                null, AllFutureMsg, true, true);
        }
    };

    props.removeConfirm = function(){
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74, cosmo.app.hideDialog,
                _('App.Button.Cancel'), true)],
            'btnsRight': [new Button('removeButtonDialog', 74, function() { doPublish('remove'); },
                _('App.Button.Remove'), true)],
            'defaultAction': function() { doPublish('remove'); },
            'content': _('Main.Prompt.EventRemoveConfirm')
        };
    };

    props.removeRecurConfirm = function(){
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74, cosmo.app.hideDialog,
                _('App.Button.Cancel'), true)],
            'btnsRight': [],
            'defaultAction': function() {},
            'width': 480,
            'content': _('Main.Prompt.RecurRemoveConfirm')
        };
    };

    props.saveRecurConfirm = function(){
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74,
                function() { doEvMethod('cancelSave') },
                _('App.Button.Cancel'), true)],
            'btnsRight': [],
            'defaultAction': function() {},
            'width': 480,
            'content': _('Main.Prompt.RecurSaveConfirm')
        };
    };

    // Publish via topics
    function doPublish(act, qual) {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        dojo.event.topic.publish('/calEvent', { 'action': act, 'qualifier': qual, data: selEv });
    }

    function doPublishSave(qual, saveItem, delta) {
        dojo.event.topic.publish('/calEvent', { 'action': 'save', 'qualifier': qual, data:saveItem, delta:delta });
    }

    // Call a method on the currently selected event
    // FIXME: Use topics
    function doEvMethod(key) {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        selEv[key]();
    }

    // Public methods
    // ********************
    this.getProps = function(key, opts) {
        var OPTIONS = cosmo.view.cal.recurringEventOptions;
        var p = props[key]();
        var opts = opts || {};
        if (key == 'saveRecurConfirm') {
            var changeTypes = opts.changeTypes;
            var delta = opts.delta;
            var saveItem = opts.saveItem  //a "CalEvent"
            p.btnsRight = [];
            if (!changeTypes[OPTIONS.ALL_EVENTS]){
                p.btnsRight.push(buttons.allEventsDisabled());
            } else {
                p.btnsRight.push(buttons.saveAllEvents(saveItem, delta));
            }
            
            if (changeTypes[OPTIONS.ALL_FUTURE_EVENTS]){
                p.btnsRight.push(buttons.saveFutureEvents(saveItem, delta));
            }
            
            if (changeTypes[OPTIONS.ONLY_THIS_EVENT]){
                p.btnsRight.push(buttons.saveOnlyThisEvent(saveItem, delta));
            }
        }
        else if (key == 'removeRecurConfirm') {
            p.btnsRight = [];
            p.btnsRight.push(buttons.removeAllEvents());
            if (!opts.masterEvent) {
                p.btnsRight.push(buttons.removeFutureEvents());
            }
            p.btnsRight.push(buttons.removeOnlyThisEvent());

        }
        return p;
    };
};
