/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
dojo.require("cosmo.ui.widget.Button");

cosmo.view.dialog = new function () {
    // Public members
    // ********************
    this._item = null;

    dojo.subscribe('cosmo:calSetSelected', 
                   dojo.hitch(this, function(cmd){this._item = cmd.data}));
    // Public methods
    // ********************
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
        dojo.publish('cosmo:calRemove', [{qualifier: qual, data: selItem }]);
        this.closeSelf();
    };

cosmo.view.dialog.BaseDialog.prototype.doPublishSave =
    function (qual, saveItem, delta) {
        dojo.publish('cosmo:calSave', [{qualifier: qual, data: saveItem, delta: delta }]);
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
        'removeAllEvents': function (only) {
            var btnText = only ? _('App.Button.Remove') : allEventsMsg;
            console.debug("create removeAllEvents button called.");
            return new cosmo.ui.widget.Button(
                {id: 'allButtonDialog', width: btnWideWidth,
                 handleOnClick: function () { self.doPublishRemove(
                    cosmo.view.service.recurringEventOptions.ALL_EVENTS); },
                 text: btnText, small: true});
        },

        'removeFutureEvents': function () {
            console.debug("create removeFutureEvents button called.");
            return new cosmo.ui.widget.Button(
                {id: 'allFutureButtonDialog', 
                 width: btnWiderWidth,
                 handleOnClick: function () { self.doPublishRemove(
                cosmo.view.service.recurringEventOptions.ALL_FUTURE_EVENTS); },
                 text: AllFutureMsg, small: true});
        },

        'removeOnlyThisEvent': function () {
            console.debug("create removeOnlyThisEvent button called.");
            return new cosmo.ui.widget.Button(
                {id: 'onlyThisButtonDialog', width: btnWiderWidth,
                 handleOnClick: function () {
                    self.doPublishRemove(
                        cosmo.view.service.recurringEventOptions.ONLY_THIS_EVENT);
                },
                 text: OnlyThisMsg, small: true});
        },

        'saveAllEvents': function (saveItem, delta, only) {
            var btnText = only ? _('App.Button.Save') : allEventsMsg;
            return new cosmo.ui.widget.Button(
                {id: 'allButtonDialog', width: btnWideWidth,
                 handleOnClick: function () {
                    self.doPublishSave(
                        cosmo.view.service.recurringEventOptions.ALL_EVENTS,
                        saveItem, delta)
                },
                 text: btnText, small: true});
        },

        'saveFutureEvents': function (saveItem, delta) {
            return new cosmo.ui.widget.Button(
                {id: 'allFutureButtonDialog', width: btnWiderWidth,
                 handleOnClick: function () {
                    self.doPublishSave(
                        cosmo.view.service.recurringEventOptions.ALL_FUTURE_EVENTS,
                        saveItem, delta);
                },
                 text: AllFutureMsg, small: true});
        },

        'saveOnlyThisEvent': function (saveItem, delta) {
            return new cosmo.ui.widget.Button(
                {id: 'onlyThisButtonDialog', width: btnWiderWidth,
                 handleOnClick: function () {
                    self.doPublishSave(
                        cosmo.view.service.recurringEventOptions.ONLY_THIS_EVENT,
                        saveItem,
                        delta);
                },
                 text: OnlyThisMsg, small: true});
        },

        'allEventsDisabled': function () {
            return new cosmo.ui.widget.Button(
                {id: 'allButtonDialog', width: btnWideWidth,
                 text: allEventsMsg, small: true, enabled: false});
        },

        'futureEventsDisabled': function () {
            return new cosmo.ui.widget.Button(
                {id: 'allFutureButtonDialog', width: btnWiderWidth,
                 text: AllFutureMsg, small: true, enabled: false});
        }
    };

    props.removeConfirm = function () {
        console.debug("removeConfirm dialog called.");
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new cosmo.ui.widget.Button(
                {id: 'removeConfirmCancelButton', width: 74, 
                 handleOnClick: cosmo.app.hideDialog,
                 text: _('App.Button.Cancel'), small: true})],
            'btnsRight': [new cosmo.ui.widget.Button(
                {id: 'removeConfirmRemoveButton', width: 74, 
                 handleOnClick: function () { self.doPublishRemove(); },
                 text: _('App.Button.Remove'), small: true})],
            'defaultAction': function () { self.doPublishRemove(); },
            'content': _('Main.Prompt.ItemRemoveConfirm')
        };
    };

    props.removeRecurConfirm = function () {
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new cosmo.ui.widget.Button(
                {id: 'removeRecurrenceCancelButton', width: 74, 
                 handleOnClick: cosmo.app.hideDialog, text: _('App.Button.Cancel'), 
                 small: true})],
            'btnsRight': [],
            'defaultAction': function () {},
            'width': 480,
            'content': _('Main.Prompt.RecurRemoveConfirm')
        };
    };

    props.saveRecurConfirm = function () {
        return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new cosmo.ui.widget.Button(
                {id: 'saveRecurConfirmSaveButton', width: 74,
                 handleOnClick: function () { self.doCancelSave.apply(self) },
                 text: _('App.Button.Cancel'), small: true})],
            'btnsRight': [],
            'defaultAction': function () {},
            'width': 480,
            'content': _('Main.Prompt.RecurSaveConfirm')
        };
    };

    props.saveRecurConfirmAllEventsOnly = function () {
         return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new cosmo.ui.widget.Button(
                {id: 'saveRecurConfirmAllEventsCancelButton', width: 74,
                 handleOnClick: function () { self.doCancelSave.apply(self) },
                 text: _('App.Button.Cancel'), small: true})],
            'btnsRight': [],
            'defaultAction': function () {},
            'width': 480,
            'content': _('Main.Prompt.RecurSaveConfirmAllItemsOnly')
        };
    };

    props.removeRecurConfirmAllEventsOnly = function () {
         return {
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [new cosmo.ui.widget.Button(
                {id: 'removeRecurConfirmAllEventsCancelButton', width: 74,
                 handleOnClick: function () { self.doCancelSave.apply(self) },
                 text: _('App.Button.Cancel'), small: true})],
            'btnsRight': [],
            'defaultAction': function () {},
            'width': 480,
            'content': _('Main.Prompt.RecurRemoveConfirmAllItemsOnly')
        };
    };

    this.getProps = function (key, opts) {
        var OPTIONS = cosmo.view.service.recurringEventOptions;
        var p = props[key]();
        var opts = opts || {};
        switch(key) {
            case 'saveRecurConfirm':
                var changeTypes = opts.changeTypes;
                var delta = opts.delta;
                var saveItem = opts.saveItem  // CalItem/ListItem
                p.btnsRight = [];
                if (!changeTypes[OPTIONS.ALL_EVENTS]) {
                    p.btnsRight.push(buttons.allEventsDisabled());
                } else {
                    p.btnsRight.push(buttons.saveAllEvents(saveItem, delta, false));
                }

                if (changeTypes[OPTIONS.ALL_FUTURE_EVENTS]) {
                    p.btnsRight.push(buttons.saveFutureEvents(saveItem, delta));
                }

                if (changeTypes[OPTIONS.ONLY_THIS_EVENT]) {
                    p.btnsRight.push(buttons.saveOnlyThisEvent(saveItem, delta));
                }
                break;
            case 'saveRecurConfirmAllEventsOnly':
                var delta = opts.delta;
                var saveItem = opts.saveItem  
                // Button should say 'Save' instead of 'All Events'
                p.btnsRight = [buttons.saveAllEvents(saveItem, delta, true)];
                break;
            case 'removeRecurConfirm':
                var item = cosmo.view.dialog.getSelectedItem();
                p.btnsRight = [];
                p.btnsRight.push(buttons.removeAllEvents(false));
                if (!item.data.isFirstOccurrence()) {
                    p.btnsRight.push(buttons.removeFutureEvents());
                }
                p.btnsRight.push(buttons.removeOnlyThisEvent());
                break;
            case 'removeRecurConfirmAllEventsOnly':
                var item = cosmo.view.dialog.getSelectedItem();
                // Button should say 'Remove' instead of 'All Events'
                p.btnsRight = [buttons.removeAllEvents(true)];
                break;
        }
        return p;
    };
};

cosmo.view.dialog.RecurrenceDialog.prototype =
    new cosmo.view.dialog.BaseDialog();

cosmo.view.dialog.UnsavedChangesDialog = function () {
    var self = this;
    var props = null;
    var strings = {
        unsavedChangesMsg: _('Main.Prompt.UnsavedChangesConfirm'),
        cancelButtonText: _('App.Button.Cancel'),
        discardButtonText: _('Main.Prompt.UnsavedChangesDiscard'),
        saveButtonText: _('Main.Prompt.UnsavedChangesSave')
    }
    var btnWidth = 84;
    var btnWidthWide = 120;

    props = function (opts) {
        return {
            'width': 480,
            'type': cosmo.app.modalDialog.CONFIRM,
            'btnsLeft': [
                new cosmo.ui.widget.Button({
                    id: "unsavedChangesDialogCancelButton",
                    text: strings.cancelButtonText, 
                    width: btnWidth,
                    handleOnClick: opts.cancelFunc, 
                    small: true,
                    enabled: true })
            ],
            'btnsRight': [
                new cosmo.ui.widget.Button({ 
                    id: "unsavedChangesDialogDiscardButton",
                    text: strings.discardButtonText,
                    width: btnWidthWide,
                    handleOnClick: opts.discardFunc,
                    small: true,
                    enabled: true }),
                new cosmo.ui.widget.Button({ 
                    id: "unsavedChangesDialogSaveButton",
                    text: strings.saveButtonText,
                    width: btnWidthWide,
                    handleOnClick: opts.saveFunc,
                    small: true,
                    enabled: true })
            ],
            'defaultAction': null,
            'content': strings.unsavedChangesMsg
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


