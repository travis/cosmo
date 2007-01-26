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
dojo.require("dojo.widget.*");

function buttonPreload() {
    var btnSizes = ['', '_sm'];
    var btnSides = ['left', 'center', 'right'];
    var btnTypes = ['', '_dim', '_lit'];
    var btnPreload = {};
    var f = function(base, suffix, size) {
        var p = 'button_' + base + suffix + size;
        btnPreload[p] = new Image();
        btnPreload[p].src = BUTTON_DIR_PATH + p + '.gif';
    }
    for (var h in btnSizes) {
        for (var i in btnSides) {
            for (var j in btnTypes) {
                f(btnSides[i], btnTypes[j], btnSizes[h]);
            }
        }
    }
}
buttonPreload();

/**
 * @object Button -- creates a button with a text label, and images
 * for endcaps and a stretchable, tiling middle section. Comes in
 * two sizes, normal and small.
 */
cosmo.ui.button.Button = function(elemId, width, onClickHandler, displayText, isSmall) {
    //if you don't do this dummyDom business, the page reflows and scrollbars
    //are reset
    var dummyDom = document.createElement('span');
    var widget = null;
    
    // Allow instantiation from obj/keyword param
    if (typeof arguments[0] == 'object') {
        widget =  dojo.widget.createWidget("cosmo:Button", arguments[0], dummyDom, 'last');
    }
    // Normal params in order
    else {
        // Note: This throws away elemId in favor of the incremented widget ID
        widget =  dojo.widget.createWidget("cosmo:Button", { text: displayText, 'width':width,
            handleOnClick: onClickHandler, small: isSmall }, dummyDom, 'last');
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

    // Private method to create the button-set table
    var doButtonTable = function() {
        var nbTable = document.createElement('table');
        var nbTBody = document.createElement('tbody');
        var nbRow = document.createElement('tr');
        var nbDivider = null;

        nbTable.style.width = '45px';
        nbTable.style.height = '18px';
        nbTable.cellPadding = '0px'; // Note camelCaps here, fun!! :)
        nbTable.cellSpacing = '0px'; // More camelCaps, yay!
        nbTable.appendChild(nbTBody);
        nbTBody.appendChild(nbRow);
        // Left endcap
        nbRow.appendChild(doButtonSetCap('Left', leftHandler));
        // Left arrow
        nbRow.appendChild(doButtonSetCenter('Left', leftHandler));
        // Divider cell
        nbDivider = document.createElement('td');
        nbDivider.id = id + 'ButtonDivider';
        nbDivider.style.width = '1px';
        nbDivider.style.height = '18px';
        nbDivider.style.lineHeight = '1px';
        nbDivider.style.background = '#aaaaaa';
        nbRow.appendChild(nbDivider);
        // Right arrow
        nbRow.appendChild(doButtonSetCenter('Right', rightHandler));
        // Right endcap
        nbRow.appendChild(doButtonSetCap('Right', rightHandler));
        return nbTable;
    }
    // Private method to make the endcaps
    var doButtonSetCap = function(side, clickHandler) {
        var nbData = document.createElement('td');
        var lowerCaseSide = side.toLowerCase();

        nbData.id = id + side;
        nbData.style.width = '9px';
        nbData.style.height = '18px';
        nbData.style.lineHeight = '18px';
        nbData.style.cursor = 'pointer';
        nbData.style.background = 'url(' + BUTTON_DIR_PATH +
            'button_' + lowerCaseSide + '_sm.gif)';
        nbData.onmouseover = function() { self.rolloverHandler(side, 'lit'); }
        nbData.onmouseout = function() { self.rolloverHandler(side, ''); }
        nbData.onclick = clickHandler;
        return nbData;
    }
    // Private method to make the center arrow cells
    var doButtonSetCenter = function(side, clickHandler) {
        var nbData = document.createElement('td');
        var nbImg = document.createElement('img');
        var lowerCaseSide = side.toLowerCase();

        nbData.id = id + 'Center' + side;
        nbData.style.width = '13px';
        nbData.style.height = '18px';
        nbData.style.lineHeight = '18px';
        nbData.style.textAlign = lowerCaseSide;
        nbData.style.background = 'url(' + BUTTON_DIR_PATH +
            'button_center_sm.gif)';
        nbData.style.backgroundRepeat = 'repeat-x';
        nbData.style.cursor = 'pointer';
        nbImg.src = BUTTON_DIR_PATH + 'nav_arrow_' +
            lowerCaseSide + '.gif';
        nbData.appendChild(nbImg);
        nbData.onmouseover = function() { self.rolloverHandler(side, 'lit'); }
        nbData.onmouseout = function() { self.rolloverHandler(side, ''); }
        nbData.onclick = clickHandler;
        return nbData;
    }

    // Main properties and methods
    // ========
    this.id = id;
    this.domNode = doButtonTable();
    this.rolloverHandler = function(side, state) {
        // Do the rollover for the entire button set together
        // Doing just one side at a time looks freaky
        this.doRollover(side, state);
    };
    this.doRollover = function(side, state) {
        var btnSide = side.toLowerCase();
        var btnState = state ? '_' + state : '';
        var dividerColor = state == 'lit' ? '#000000' : '#aaaaaa';
        document.getElementById(this.id + 'Center' + side).style.background =
            'url(' + BUTTON_DIR_PATH + 'button_center' + btnState + '_sm.gif)';
        document.getElementById(this.id + side).style.background =
            'url(' + BUTTON_DIR_PATH + 'button_' + btnSide + btnState + '_sm.gif)';
        document.getElementById(this.id + 'ButtonDivider').style.background = dividerColor;
    };
}
