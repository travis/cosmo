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

dojo.require("cosmo.util.i18n");

dojo.require("cosmo.convenience");
var _ = cosmo.util.i18n.getText;

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
    var buttons = {
        'removeAllEvents': function(){
           return new Button('allButtonDialog', btnWideWidth, 
                function() { doPublish('remove', recurOpts.ALL_EVENTS); }, allEventsMsg, true);
        },
         
        'removeFutureEvents': function(){
            return new Button('allFutureButtonDialog', btnWiderWidth, 
                function() { doPublish('remove', recurOpts.ALL_FUTURE_EVENTS); }, AllFutureMsg, true);
        },

        'removeOnlyThisEvent': function(){
            return new Button('onlyThisButtonDialog', btnWiderWidth, 
                function() { doPublish('remove', recurOpts.ONLY_THIS_EVENT); }, OnlyThisMsg, true);
        },

        'saveAllEvents': function(){
            return new Button('allButtonDialog', btnWideWidth, 
            function() { doPublish('save', recurOpts.ALL_EVENTS); }, allEventsMsg, true);
        },
        
        'saveFutureEvents': function(){
            return new Button('allFutureButtonDialog', btnWiderWidth, 
            function() { doPublish('save', recurOpts.ALL_FUTURE_EVENTS); }, AllFutureMsg, true);
        },
        
        'saveOnlyThisEvent': function(){
            return new Button('onlyThisButtonDialog', btnWiderWidth, 
                function() { doPublish('save', recurOpts.ONLY_THIS_EVENT); }, OnlyThisMsg, true);
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
    // Call a method on the currently selected event
    // FIXME: Use topics
    function doEvMethod(key) {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        selEv[key]();
    }
    
    // Public methods
    // ********************
    this.getProps = function(key, optsParam) {
        var p = props[key]();
        var opts = optsParam || {};
        if (key == 'saveRecurConfirm') {
            p.btnsRight = [];
            if (opts.instanceOnly || opts.recurrenceMod) {
                p.btnsRight.push(buttons.allEventsDisabled());
                p.btnsRight.push(buttons.saveFutureEvents());
            }
            else {
                p.btnsRight.push(buttons.saveAllEvents());
                if (!opts.masterEvent) {
                    p.btnsRight.push(buttons.saveFutureEvents());
                }
            }
            p.btnsRight.push(buttons.saveOnlyThisEvent());
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
cosmo.view.cal.dialog.constructor = null;