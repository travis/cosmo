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
    
    props.removeConfirm = {
        'type': Cal.dialog.CONFIRM,
        'btnsLeft': [new Button('cancelButtonDialog', 74, Cal.hideDialog,
            getText('App.Button.Cancel'), true)],
        'btnsRight': [new Button('removeButtonDialog', 74, Cal.removeCalEvent,
            getText('App.Button.Remove'), true)],
        'defaultAction': Cal.removeCalEvent,
        'msg': getText('Main.Prompt.EventRemoveConfirm')
    };
    props.saveRecurConfirm = {
        'type': Cal.dialog.CONFIRM,
        'btnsLeft': [new Button('cancelButtonDialog', 74, 
            function() { doEvMethod('cancelSave') },
            getText('App.Button.Cancel'), true)],
        'btnsRight': [new Button('saveButtonDialog', 74, 
            function() { doEvMethod('remoteSave'); },
            getText('App.Button.Save'), true)],
        'defaultAction': function() { doEvMethod('remoteSave'); },
        'msg': 'This is a recurring event. Editing recurring events is not supported in Cosmo,' +
            ' and will probably have effects you do not intend.<br/>&nbsp;<br/>Save this change?'
    };
    
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



