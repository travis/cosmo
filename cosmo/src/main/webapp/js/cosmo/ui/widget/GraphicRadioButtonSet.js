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
 * @fileoverview GraphicRadioButtonSet.js -- set of graphical toggles
 *    that act like radio buttons -- clickable elements are DOM nodes
 *    that have graphical backgrounds, allowing loading of one large
 *    master image to pull the various button state images from
 * @author Matthew Eernisse mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.GraphicRadioButtonSet");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.env");

dojo.widget.defineWidget("cosmo.ui.widget.GraphicRadioButtonSet", dojo.widget.HtmlWidget, {

    templateString: '<span></span>',

    // Props from template or set in constructor
    selectedButtonIndex: null, // No default selected button
    height: 0,

    // Define these here so they don't end up as statics
    initializer: function () {
        this.buttons = [];
        this.buttonNodes = [];
    },

    // Private
    _getButtonIndex: function (td) {
        var n = td.id.replace(this.widgetId + '_button', '');
        return parseInt(n);
    },
    _morphButton: function (td, over){
        var n = this._getButtonIndex(td);
        var b = this.buttons[n];
        var pos = over ? b.mouseoverImgPos : 
            (n == this.selectedButtonIndex) ? b.downStateImgPos : b.defaultImgPos;
            td.style.backgroundPosition = 
                pos[0] + 'px ' + pos[1] + 'px';
    },
    _handleMouseover: function (e) {
        var td = e.target;
        if (td && td.id) {
            this._morphButton(td, true);
        }
    },
    _handleMouseout: function (e) {
        var td = e.target;
        if (td && td.id) {
            this._morphButton(td, false);
        }
    },
    _handleClick: function (e) {
        var td = e.target;
        if (td && td.id) {
            var n = this._getButtonIndex(td);
            var buttons = this.buttons;
            this.selectedButtonIndex = n; 
            for (var i = 0; i < buttons.length; i++) {
                this._morphButton(this.buttonNodes[i], false);
            }
            buttons[n].handleClick();
        }
    },
    // Public

    // Lifecycle
    fillInTemplate: function () {

        var d = this.domNode;
        var table = null;
        var body = null;
        var tr = null;
        var td = null;
        var t = {};
        var s = null;
        var buttons = this.buttons;

        d.style.visibility = 'hidden';
        table = _createElem('table');
        //table.border = '1';
        table.cellPadding = '0';
        table.cellSpacing = '0';
        body = _createElem('tbody');
        table.appendChild(body);
        tr = _createElem('tr');
        body.appendChild(tr);
        table.id = this.widgetId + '_buttonSet';
        table.className = 'buttonSet';
        this.buttonNodes = [];
        for (var i = 0; i < buttons.length; i++) {
            var b = buttons[i];
            var td = _createElem('td');
            td.id = this.widgetId + '_button' + i;
            td.style.width = b.width + 'px';
            td.style.backgroundImage = 'url(' + cosmo.env.getImageUrl('image_grid.png')+')';
            var pos = i == this.selectedButtonIndex ? b.downStateImgPos : b.defaultImgPos;
            td.style.backgroundPosition = pos[0] + 'px ' + pos[1] + 'px';
            td.style.cursor = 'pointer';
            td.style.fontSize = '1px';
            td.style.height = this.height + 'px';
            td.appendChild(cosmo.util.html.nbsp());
            dojo.event.connect(td, 'onmouseover', this, '_handleMouseover');
            dojo.event.connect(td, 'onmouseout', this, '_handleMouseout');
            dojo.event.connect(td, 'onclick', this, '_handleClick');
            tr.appendChild(td);
            this.buttonNodes.push(td);
        }
        d.appendChild(table);
        d.style.visibility = 'visible';
    }
} );

cosmo.ui.widget.GraphicRadioButtonSet.Button = function (p) {
    params = p || {};
    this.width = 0;
    this.defaultImgPos = [];
    this.mouseoverImgPos = [];
    this.downStateImgPos = [];
    for (var n in params) { this[n] = params[n]; }
}
