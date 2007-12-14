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
 * @fileoverview Button -- button-related functions
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */
dojo.provide("cosmo.ui.button");
dojo.require("cosmo.ui.conf");
dojo.require("cosmo.env");
dojo.require("dojo.event");
dojo.require("dojo.widget.*");
dojo.require("cosmo.ui.widget.Button");

/**
 * @object Button -- creates a button with a text label, and images
 * for endcaps and a stretchable, tiling middle section. Comes in
 * two sizes, normal and small.
 */
cosmo.ui.button.Button = function(elemId, width, onClickHandler,
    displayText, isSmall, isDisabled) {

    // If you don't do this dummyDom business, the page reflows and
    // scrollbars are reset
    var dummyDom = document.createElement('span');
    var widget = null;
    var enabled = isDisabled == true ? false : true;

    // Allow instantiation from obj/keyword param
    if (typeof arguments[0] == 'object') {
        widget =  dojo.widget.createWidget("cosmo:Button",
            arguments[0], dummyDom, 'last');
    }
    // Normal params in order
    else {
        widget =  dojo.widget.createWidget("cosmo:Button", {
            widgetId: elemId,
            text: displayText, 'width':width,
            handleOnClick: onClickHandler, small: isSmall,
            enabled: enabled }, dummyDom, 'last');
    }
    dummyDom.removeChild(widget.domNode);
    return widget;
}

Button = cosmo.ui.button.Button;

/**
 * @object NavButtonSet -- creates a two-button cluster with a
 * left and right arrow
 * Note that the line-height CSS property needs to be set
 * for the table cells otherwise they inherit from the
 * container the NavButtonSet is sitting in
 */
cosmo.ui.button.NavButtonSet = function(id, leftHandler, rightHandler) {

    var self = this;
    this.leftButtonNode = null;
    this.rightButtonNode = null;

    // Private method to create the button-set table
    var doButtonTable = function() {
        var nbTable = document.createElement('table');
        var nbTBody = document.createElement('tbody');
        var nbRow = document.createElement('tr');
        var nbDivider = null;

        nbTable.style.width = '45px';
        nbTable.style.height = '16px';
        nbTable.cellPadding = '0px'; // Note camelCaps here, fun!! :)
        nbTable.cellSpacing = '0px'; // More camelCaps, yay!
        nbTable.appendChild(nbTBody);
        nbTBody.appendChild(nbRow);
        // Left arrow
        self.leftButtonNode = doButtonSetCenter('Left', leftHandler);
        nbRow.appendChild(self.leftButtonNode);
        // Divider cell
        nbDivider = document.createElement('td');
        nbDivider.id = id + 'ButtonDivider';
        nbDivider.style.width = '1px';
        nbDivider.style.height = '16px';
        nbDivider.style.lineHeight = '1px';
        nbDivider.className = 'btnSetDividerBase';
        nbRow.appendChild(nbDivider);
        // Right arrow
        self.rightButtonNode = doButtonSetCenter('Right', rightHandler);
        nbRow.appendChild(self.rightButtonNode);
        return nbTable;
    }
    // Private method to make the center arrow cells
    var doButtonSetCenter = function(side, clickHandler) {
        var nbData = document.createElement('td');
        var nbImg = document.createElement('img');
        var lowerCaseSide = side.toLowerCase();

        nbData.id = id + 'Center' + side;
        nbData.style.width = '13px';
        nbData.style.height = '15px';
        nbData.style.lineHeight = '0px';
        nbData.style.textAlign = lowerCaseSide;
        nbData.className = 'btnElemBaseSm';
        nbData.style.backgroundImage = 'url(' + cosmo.env.getImageUrl("button_bgs.png") + ')';
        nbImg.src = BUTTON_DIR_PATH + 'nav_arrow_' +
            lowerCaseSide + '.gif';
        nbImg.style.padding = '2px 5px 0 5px';
        if (side == 'Left') {
            nbData.style.borderWidth = '1px 0 1px 1px';
        }
        else {
            nbData.style.borderWidth = '1px 1px 1px 0';
        }
        nbData.appendChild(nbImg);

        dojo.event.connect(nbData, 'onmouseover', self, '_morphButton');
        dojo.event.connect(nbData, 'onmouseout', self, '_morphButton');
        dojo.event.connect(nbData, 'onmousedown', self, '_morphButton');
        dojo.event.connect(nbData, 'onmouseup', self, '_morphButton');
        dojo.event.connect(nbData, 'onclick', clickHandler);
        return nbData;
    }

    // Main properties and methods
    // ========
    this.id = id;
    this.domNode = doButtonTable();
    this._morphButton = function(e) {
        var s = e.type;
        var t = e.currentTarget;
        var center = $(this.id + 'ButtonDivider');
        if (!t.id) { return false; }
        var states = {
            mouseover: 'btnElemBaseSm' + ' btnElemMouseoverSm',
            mouseout: 'btnElemBaseSm',
            mousedown: 'btnElemBaseSm' + ' btnElemMousedownSm',
            mouseup: 'btnElemBaseSm'
        }
        // On mousedown the separator div may be serving as the
        // right-border of the left button, or vice-versa
        if (s == 'mousedown') {
            if (t.id.indexOf('Left') > -1) {
                center.className = 'btnSetDividerLeftPress';
            }
            else {
                center.className = 'btnSetDividerRightPress';
            }
        }
        else {
            center.className = 'btnSetDividerBase';
        }
        t.className = states[s];

    };
}
