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

/**
 * @fileoverview Button - creates a push button that can be enabled or disabled, small or
 *                        normal size.
 * @authors: Matthew Eernisse (mde@osafoundation.org), Bobby Rullo (br@osafoundation.org)
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.Button");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.html");

dojo.widget.defineWidget("cosmo.ui.widget.Button", dojo.widget.HtmlWidget, {

    // Constants
    DISABLED_OPACITY : 0.8,
    templateString: '<input type="button" />',

    // Properties to be set by tag or constructor
    enabled: true,
    small: false,
    text: "",
    i18nText: "",
    width: 0,
    handleOnClick: "",

    fillInTemplate: function() {
        if (typeof(this.handleOnClick) == "string") {
         eval("this.handleOnClick = function() {" + this.handleOnClick + ";}");
        }
        // Mouse effects and onclick
        this._attachHandlers();
        // DOM handles
        this.domNode.id = this.widgetId;
        this.domNode.name = this.widgetId;

        // Finish setting up
        this.setText(this.text);
        if (this.i18nText != "") this.setI18nText(this.i18nText);

        this.setWidth(this.width);
        this.setEnabled(this.enabled);
    },

    setText: function(text) {
        this.text = text;
        this.domNode.value = this.text;
    },

    setI18nText: function(text){
        this.i18nText = text;
        this.setText(_(text));
    },

    setWidth: function(width) {
        this.width = width;
        if (this.width) {
            this.domNode.style.width = parseInt(width) + "px";
        }
    },
    setEnabled: function(enabled) {
        var suf = this.small ? 'Sm' : '';
        this.enabled = enabled;
        if (this.enabled) {
            if (this.handleOnClickOrig) {
                this.handleOnClick = this.handleOnClickOrig;
            }
            this.domNode.className = 'btnElemBase' + suf;
            cosmo.util.html.setOpacity(this.domNode, 1);

        }
        else {
            this.handleOnClickOrig = this.handleOnClick;
            this.handleClick = null;
            this.domNode.className = 'btnElemBase' + suf + ' btnElemDisabled' + suf;
            cosmo.util.html.setOpacity(this.domNode, this.DISABLED_OPACITY);
        }
    },
    _attachHandlers: function () {
        dojo.event.connect(this.domNode, 'onmouseover', this, '_morphButton');
        dojo.event.connect(this.domNode, 'onmouseout', this, '_morphButton');
        dojo.event.connect(this.domNode, 'onmousedown', this, '_morphButton');
        dojo.event.connect(this.domNode, 'onmouseup', this, '_morphButton');
        dojo.event.connect(this.domNode, 'onclick', this, '_handleOnClick');
    },
    _morphButton: function(e) {
        if (this.enabled) {
            var s = e.type;
            var suf = this.small ? 'Sm' : '';
            var states = {
                mouseover: 'btnElemBase' + suf + ' btnElemMouseover' + suf,
                mouseout: 'btnElemBase' + suf,
                mousedown: 'btnElemBase' + suf + ' btnElemMousedown' + suf,
                mouseup: 'btnElemBase' + suf
            }
            this.domNode.className = states[s];
        }
    },
    _handleOnClick: function() {
        if (this.enabled) {
           this.handleOnClick();
        }
    }
  } );
