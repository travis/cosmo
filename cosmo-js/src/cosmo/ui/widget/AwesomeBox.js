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
dojo.provide("cosmo.ui.widget.AwesomeBox");
dojo.require("cosmo.util.html");

dojo.requireLocalization("cosmo.ui.widget", "AwesomeBox");
dojo.declare("cosmo.ui.widget.AwesomeBox", [dijit._Widget, dijit._Templated],
{

    // Processing lock to avoid duplicate items created
    isProcessing: false,

    parent: null,
    textBox: null,
    button: null,

    l10n: null,

    widgetsInTemplate: true,
    templateString: '<div class="awesomeBoxContainer">'
        + '<input type="text" id="listViewQuickItemEntry" class="awesomeBoxTextInput inputText" dojoAttachPoint="textBox" dojoAttachEvent="onkeyup: onKeyUp"></input>'
        + '<button id="quickEntryCreate" class="awesomeBoxButton" dojoType="dijit.form.Button" dojoAttachPoint="button" label="${l10n.submit}" dojoAttachEvent="onclick: onSubmit"></button>'
        + '</div>',

    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "AwesomeBox");
    },

    getWriteable: function(){
        return cosmo.app.pim.getSelectedCollectionWriteable();
    },

    getValue: function() {
        return this.textBox.value;
    },

    setValue: function(value) {
        this.textBox.value = value;
    },

    updateHint: function() {
        cosmo.util.html.setTextInput(this.textBox, this.l10n.hintText, true);
    },

    disableButton: function () {
        this.button.setAttribute("disabled", true);
    },

    enableButton: function () {
        this.button.setAttribute("disabled", false);
    },

    disableText: function () {
        this.textBox.disabled = true;
    },

    enableText: function () {
        this.textBox.disabled = false;
    },

    onSubmit: function () {
        // Only submit one action at a time
        if (this.isProcessing) { return false; }
        this.isProcessing = true;
        var title = this.getValue();
        this.disableButton();
        this.disableText();
        this.setValue(_('App.Status.Processing'));
        cosmo.view.list.createNoteItem(title);
    },

    onKeyUp: function(e){
        if (this.getWriteable() && e.keyCode == 13) {
            this.onSubmit();
            e.stopPropagation();
        }
    },

    disable: function(){
        this.disableButton();
        this.disableText();
    },

    enable: function(){
        this.enableButton();
        this.enableText();
    },

    reset: function(){
        this.isProcessing = false;
        this.updateHint();
        if (this.getWriteable()){
            this.enable();
            dojo.connect(this.textBox, 'onfocus', cosmo.util.html.handleTextInputFocus);
            dojo.connect(this.textBox, 'onblur', this, 'updateHint');
        } else {
            this.disable();
        }
    },

    render: function(){
        this.reset();
        console.log("cosmo.ui.widget.AwesomeBox.render deprecated");
    }
});

