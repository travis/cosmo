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

cosmo.view.dialog = new function () {
    dojo.event.topic.subscribe('/calEvent', this, 'handlePub');
    // Public members
    // ********************
    this._item = null;

    // Public methods
    // ********************
    this.handlePub = function (cmd) {
        var act = cmd.action;
        var item = cmd.data;
        switch (act) {
            case 'setSelected':
                this._item = item;
                break;
        }
    };
    this.getSelectedItem = function () {
        return this._item;
    };
};

// Common functions for both dialog types
cosmo.view.dialog.BaseDialog = function (){};
cosmo.view.dialog.BaseDialog.prototype.doCancelSave = function () {
    var selItem = cosmo.view.dialog.getSelectedItem();
    selItem.cancelSave();
    this.closeSelf();
};
cosmo.view.dialog.BaseDialog.prototype.closeSelf = function () {
    if (cosmo.app.modalDialog) {
        cosmo.app.hideDialog();
    }
};
cosmo.view.dialog.BaseDialog.prototype.doPublishRemove =
    function (qual) {
    var selItem = cosmo.view.dialog.getSelectedItem();
    dojo.event.topic.publish('/calEvent', {
        action: 'remove', qualifier: qual, data: selItem });
    this.closeSelf();
};

cosmo.view.dialog.BaseDialog.prototype.doPublishSave =
    function (qual, saveItem, delta) {
    dojo.event.topic.publish('/calEvent', {
        action: 'save', qualifier: qual,
        data: saveItem, delta: delta });
    this.closeSelf();
};

// Dialog for how to apply changes/removal to
// items
cosmo.view.dialog.RecurrenceDialog = function () {
    var self = this;
    var props = {}; // Props for each modal dialog
    var allEventsMsg = _('Main.Prompt.RecurButtonAll');
    var AllFutureMsg = _('Main.Prompt.RecurButtonFuture');
    var OnlyThisMsg = _('Main.Prompt.RecurButtonOnly');
    var btnWideWidth = 84;
    var btnWiderWidth = 120;

    // All available buttons for the dialog
    var buttons = {
        'removeAllEvents': function () {
           dojo.debug("create removeAllEvents button called.");
           return new Button('allButtonDialog', btnWideWidth,
                function () { self.doPublishRemove(
                    cosmo.view.service.recurringEventOptions.ALL_EVENTS); },
                allEventsMsg, true);
        },

        'removeFutureEvents': function () {
            dojo.debug("create removeFutureEvents button called.");
            return new Button('allFutureButtonDialog', btnWiderWidth,
                function () { self.doPublishRemove(
                cosmo.view.service.recurringEventOptions.ALL_FUTURE_EVENTS); },
                AllFutureMsg, true);
        },

        'removeOnlyThisEvent': function () {
            dojo.debug("create removeOnlyThisEvent button called.");
            return new Button('onlyThisButtonDialog', btnWiderWidth,
                function () {
                    self.doPublishRemove(
                        cosmo.view.service.recurringEventOptions.ONLY_THIS_EVENT);
                },
                OnlyThisMsg, true);
        },

        'saveAllEvents': function (saveItem, delta) {
            return new Button('allButtonDialog', btnWideWidth,
                function () {
                    self.doPublishSave(
                        cosmo.view.service.recurringEventOptions.ALL_EVENTS,
                        saveItem, delta)
                },
                allEventsMsg,
                true);
        },

        'saveFutureEvents': function (saveItem, delta) {
            return new Button('allFutureButtonDialog', btnWiderWidth,
                function () {
                    self.doPublishSave(
                        cosmo.view.service.recurringEventOptions.ALL_FUTURE_EVENTS,
                        saveItem, delta);
                },
                AllFutureMsg, true);
        },

        'saveOnlyThisEvent': function (saveItem, delta) {
            return new Button('onlyThisButtonDialog', btnWiderWidth,
                function () {
                    self.doPublishSave(
                        cosmo.view.service.recurringEventOptions.ONLY_THIS_EVENT,
                        saveItem,
                        delta);
                },
                OnlyThisMsg, true);
        },

        'allEventsDisabled': function () {
            return new Button('allButtonDialog', btnWideWidth,
                null, allEventsMsg, true, true);
        },

        'futureEventsDisabled': function () {
            return new Button('allFutureButtonDialog', btnWiderWidth,
                null, AllFutureMsg, true, true);
        }
    };

    props.removeConfirm = function () {
        dojo.debug("removeConfirm dialog called.");
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74, cosmo.app.hideDialog,
                _('App.Button.Cancel'), true)],
            'btnsRight': [new Button('removeButtonDialog', 74, function () { self.doPublishRemove(); },
                _('App.Button.Remove'), true)],
            'defaultAction': function () { self.doPublishRemove(); },
            'content': _('Main.Prompt.ItemRemoveConfirm')
        };
    };

    props.removeRecurConfirm = function () {
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74, cosmo.app.hideDialog,
                _('App.Button.Cancel'), true)],
            'btnsRight': [],
            'defaultAction': function () {},
            'width': 480,
            'content': _('Main.Prompt.RecurRemoveConfirm')
        };
    };

    props.saveRecurConfirm = function () {
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new Button('cancelButtonDialog', 74,
                function () { self.doCancelSave.apply(self) },
                _('App.Button.Cancel'), true)],
            'btnsRight': [],
            'defaultAction': function () {},
            'width': 480,
            'content': _('Main.Prompt.RecurSaveConfirm')
        };
    };

    this.getProps = function (key, opts) {
        var OPTIONS = cosmo.view.service.recurringEventOptions;
        var p = props[key]();
        var opts = opts || {};
        if (key == 'saveRecurConfirm') {
            var changeTypes = opts.changeTypes;
            var delta = opts.delta;
            var saveItem = opts.saveItem  // CalItem/ListItem
            p.btnsRight = [];
            if (!changeTypes[OPTIONS.ALL_EVENTS]) {
                p.btnsRight.push(buttons.allEventsDisabled());
            } else {
                p.btnsRight.push(buttons.saveAllEvents(saveItem, delta));
            }

            if (changeTypes[OPTIONS.ALL_FUTURE_EVENTS]) {
                p.btnsRight.push(buttons.saveFutureEvents(saveItem, delta));
            }

            if (changeTypes[OPTIONS.ONLY_THIS_EVENT]) {
                p.btnsRight.push(buttons.saveOnlyThisEvent(saveItem, delta));
            }
        }
        else if (key == 'removeRecurConfirm') {
           var item = cosmo.view.dialog.getSelectedItem();
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

cosmo.view.dialog.RecurrenceDialog.prototype =
    new cosmo.view.dialog.BaseDialog();

cosmo.view.dialog.UnsavedChangesDialog = function () {
    var self = this;
    var props = null;
    var unsavedChangesMsg = _('Main.Prompt.RecurButtonAll');
    var btnWidth = 84;
    var btnWidthWide = 120;

    props = function (opts) {
        return {
            'width': 480,
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [
                new Button({
                    text: _('App.Button.Cancel'), 
                    width: btnWidth,
                    handleOnClick: cosmo.app.hideDialog,
                    small: true,
                    enabled: true })
            ],
            'btnsRight': [
                new cosmo.ui.button.Button({ 
                    text: 'Ignore Changes', 
                    width: btnWidthWide,
                    handleOnClick: opts.ignoreFunc,
                    small: true,
                    enabled: true }),
                new cosmo.ui.button.Button({ 
                    text: 'Save Changes', 
                    width: btnWidthWide,
                    handleOnClick: opts.saveFunc,
                    small: true,
                    enabled: true })
            ],
            'defaultAction': null,
            'content': 'You have unsaved changes, dude. How do you want to handle that?'
        };
    };
    this.getProps = function (o) {
        var opts = o || {};
        var p = props(opts);
        return p;
    };
};

cosmo.view.dialog.UnsavedChangesDialog.prototype =
    new cosmo.view.dialog.BaseDialog();


