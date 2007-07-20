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
dojo.require("cosmo.view.service");
dojo.require("cosmo.convenience");

cosmo.view.dialog = new function() {

    var self = this;
    var props = {}; // Props for each modal dialog
    var allEventsMsg = 'All Events';
    var AllFutureMsg = 'All Future Events';
    var OnlyThisMsg = 'Only This Event';
    var btnWideWidth = 84;
    var btnWiderWidth = 120;
    
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');

    // All available buttons for the dialog
    var buttons = {
        'removeAllEvents': function(){
           dojo.debug("create removeAllEvents button called.");
           return new Button('allButtonDialog', btnWideWidth,
                function() { doPublish('remove', 
                cosmo.view.service.recurringEventOptions.ALL_EVENTS); }, 
                allEventsMsg, true);
        },

        'removeFutureEvents': function(){
            dojo.debug("create removeFutureEvents button called.");
            return new Button('allFutureButtonDialog', btnWiderWidth,
                function() { doPublish('remove', 
                cosmo.view.service.recurringEventOptions.ALL_FUTURE_EVENTS); }, 
                AllFutureMsg, true);
        },

        'removeOnlyThisEvent': function(){
            dojo.debug("create removeOnlyThisEvent button called.");
            return new Button('onlyThisButtonDialog', btnWiderWidth,
                function() { 
                    doPublish('remove', 
                    cosmo.view.service.recurringEventOptions.ONLY_THIS_EVENT); 
                }, 
                OnlyThisMsg, true);
        },

        'saveAllEvents': function(saveItem, delta){
            return new Button('allButtonDialog', btnWideWidth,
                function() {  
                    doPublishSave(
                        cosmo.view.service.recurringEventOptions.ALL_EVENTS,
                        saveItem, delta)
                }, 
                allEventsMsg, 
                true);
        },

        'saveFutureEvents': function(saveItem, delta){
            return new Button('allFutureButtonDialog', btnWiderWidth,
                function () { 
                    doPublishSave(
                        cosmo.view.service.recurringEventOptions.ALL_FUTURE_EVENTS,
                        saveItem, delta); 
                },
                AllFutureMsg, true);
        },

        'saveOnlyThisEvent': function(saveItem, delta){
            return new Button('onlyThisButtonDialog', btnWiderWidth,
                function () { 
                    doPublishSave(
                        cosmo.view.service.recurringEventOptions.ONLY_THIS_EVENT,
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
        dojo.debug("removeConfirm dialog called.");
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74, cosmo.app.hideDialog,
                _('App.Button.Cancel'), true)],
            'btnsRight': [new Button('removeButtonDialog', 74, function() { doPublish('remove'); },
                _('App.Button.Remove'), true)],
            'defaultAction': function() { doPublish('remove'); },
            'content': _('Main.Prompt.ItemRemoveConfirm')
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
                doCancelSave,
                _('App.Button.Cancel'), true)],
            'btnsRight': [],
            'defaultAction': function() {},
            'width': 480,
            'content': _('Main.Prompt.RecurSaveConfirm')
        };
    };

    // Publish via topics
    function doPublish(act, qual) {
        var selEv = this.item;
        dojo.event.topic.publish('/calEvent', { 
            action: act, qualifier: qual, data: self.item });
        closeSelf();
    }

    function doPublishSave(qual, saveItem, delta) {
        dojo.event.topic.publish('/calEvent', { 
            action: 'save', qualifier: qual, 
            data: saveItem, delta: delta });
        closeSelf();
    }

    // Call a method on the currently selected event
    // FIXME: Use topics
    function doCancelSave() {
        self.item.cancelSave();
        closeSelf();
    }
    function closeSelf() {
        if (cosmo.app.modalDialog) {
            cosmo.app.hideDialog();
        }
    }

    // Public members
    // ********************
    this.item = null;

    // Public methods
    // ********************
    this.handlePub = function (cmd) {
        var act = cmd.action;
        var item = cmd.data;
        switch (act) {
            case 'setSelected':
                this.item = item;
                break;
        }
    }
    this.getProps = function(key, opts) {
        var OPTIONS = cosmo.view.service.recurringEventOptions;
        var p = props[key]();
        var opts = opts || {};
        if (key == 'saveRecurConfirm') {
            var changeTypes = opts.changeTypes;
            var delta = opts.delta;
            var saveItem = opts.saveItem  // CalItem/ListItem
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
           var item = this.item;
            p.btnsRight = [];
            p.btnsRight.push(buttons.removeAllEvents());
            if (!item.data.isFirstOccurrence()) {
                p.btnsRight.push(buttons.removeFutureEvents());
            }
            p.btnsRight.push(buttons.removeOnlyThisEvent());

        }
        return p;
    };
};
