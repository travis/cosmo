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
 * @fileoverview Calendar events -- links the Block to the CalEventData
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @object Button -- creates a button with a text label, and images
 * for endcaps and a stretchable, tiling middle section. Comes in
 * two sizes, normal and small.
 * TO-DO: Refactor so this big ol' string function doesn't come
 * first
 * TO-DO: Consider doing this all with DOM methods 
 * FIXME: A standard object constructor function should probably not
 * return a DOM node. Is it an object? Or a DOM node? Make it a static
 * method of a Button singleton or something
 */
function Button(elemId, width, onClickHandler, displayText, isSmall) {

    /**
     * The main function that returns the markup for the button
     * TO-DO: Do this all with DOM methods
     */
    this.htmlString = function() {
        var str = '';
        str += '<table cellpadding="0" cellspacing="0"' +
            ' border="0" style="width:' + this.width + 'px;';
        if (this.disable) {
            str += ' opacity:0.6;';
            if (document.all) {
                str += ' filter:alpha(opacity=60);';
            }
        }
        str += '">\n';
        str += '<tr>\n';
        str += '<td id="' + this.elemId + 'Left" style="width:' + 
            this.capWidth + 'px; height:' + this.buttonHeight +
           'px; background:url(' + BUTTON_DIR_PATH + 'button_left';
        if (this.disable) {
            str += '_dim';
        }
        if (this.isSmall) {
            str += '_sm';
        }
        str += '.gif);';
        if (!this.disable) {
            str += ' cursor:pointer;"';
            str += ' onmouseover="Button.over(\'' + this.elemId +
              '\', ' + this.isSmall + ');" onmouseout="Button.out(\'' + 
              this.elemId + '\', ' + this.isSmall + ');"';
        }
        else {
            str += '"';
        }
        str += '></td>\n';
        str += '<td id="' + this.elemId + 'Center" style="height:' + 
            this.buttonHeight +
           'px; text-align:center; background:url(' + BUTTON_DIR_PATH + 
           'button_center';
        if (this.disable) {
            str += '_dim';
        }
        if (this.isSmall) {
            str += '_sm';
        }
        str += '.gif);';
        if (!this.disable) {
            str += ' cursor:pointer;"';
            str += ' onmouseover="Button.over(\'' + this.elemId +
              '\', ' + this.isSmall + ');" onmouseout="Button.out(\'' + 
              this.elemId + '\', ' + this.isSmall + ');"';
        }
        else {
            str += '"';
        }
        str += '>';
        str += '<div id="' + this.elemId + 'Text" class="buttonText';
        if (this.disable) {
           str += 'Disabled';
        }
        if (this.isSmall) {
            str += 'Sm';
        }
        str += '">' + this.displayText + '</div>';
        str += '</td>\n';
        str += '<td id="' + this.elemId + 'Right" style="width:' + 
            this.capWidth + 'px; height:' + this.buttonHeight +
           'px; background:url(' + BUTTON_DIR_PATH + 'button_right';
        if (this.disable) {
            str += '_dim';
        }
        if (this.isSmall) {
            str += '_sm';
        }
        str += '.gif);';
        if (!this.disable) {
                str += ' cursor:pointer;"';
                str += ' onmouseover="Button.over(\'' + this.elemId +
                  '\', ' + this.isSmall + ');" onmouseout="Button.out(\'' + 
                  this.elemId + '\', ' + this.isSmall + ');"';
       }
        else {
            str += '"';
        }
        str += '></td>\n';
        str += '</tr>\n';
        str += '</table>\n';
        return str;
    }
   
    // The DOM node for the button
    this.domNode = null;
    // The id/prefix of the DOM elements for this button
    this.elemId = elemId;
    // Width in pixels of the button
    this.width = width;
    // Handler function
    this.onClickHandler = onClickHandler;
    // Text label for the button
    this.displayText = displayText;
    // Create a normal-sized or small button
    this.isSmall = isSmall ? true : false;
    // Width in pixels of the end-cap graphics
    this.capWidth = isSmall ? 9 : 10;
    // Height in pixels of the button
    this.buttonHeight = isSmall ? 18 : 24;
    // Disable button if there's no handler
    this.disable = onClickHandler ? false : true;

    // Create the surrounding span for the button table
    var but = document.createElement('span');
    var cells = null;
    // Create the button span (inline element)
    but.id = this.elemId;
    but.style.background = 'transparent';
    // The button content table is complex -- use innerHTML
    but.innerHTML = this.htmlString(); 
    // If a handler is defined, attach it to the 
    // three cells of the button (left, center, right)
    if (this.onClickHandler) {
    cells = but.getElementsByTagName('TD');
        for (var i = 0; i < cells.length; i++) {
            cells[i].onclick = this.onClickHandler;
        }
    }
    this.domNode = but;
}

/**
 * Highlight a button on mouseover
 */
Button.over = function(elemId, isSmall) {
    var suf = isSmall ? '_sm' : '';
    document.getElementById(elemId + 'Left').style.background = 
        'url(' + BUTTON_DIR_PATH + 'button_left_lit' + suf + '.gif)';
    document.getElementById(elemId + 'Center').style.background = 
        'url(' + BUTTON_DIR_PATH + 'button_center_lit' + suf + '.gif)';
    document.getElementById(elemId + 'Right').style.background = 
        'url(' + BUTTON_DIR_PATH + 'button_right_lit' + suf + '.gif)';
    //document.getElementById(elemId + 'Text').style.color = '#ccccaa';
}

/**
 * Return button to normal state on mouseout
 */
Button.out = function(elemId, isSmall) {
    var suf = isSmall ? '_sm' : '';
    document.getElementById(elemId + 'Left').style.background = 
        'url(' + BUTTON_DIR_PATH + 'button_left' + suf + '.gif)';
    document.getElementById(elemId + 'Center').style.background = 
        'url(' + BUTTON_DIR_PATH + 'button_center' + suf + '.gif)';
    document.getElementById(elemId + 'Right').style.background = 
        'url(' + BUTTON_DIR_PATH + 'button_right' + suf + '.gif)';
    //document.getElementById(elemId + 'Text').style.color = '#339933';
}

/**
 * Function that creates the panel of buttons for the bottom of a
 * dialog box. Three clusters of buttons -- right-aligned, centered,
 * and left-aligned.
 * TO-DO: Try to get rid of innerHTML usage, try to accomplish
 * with floats and margin:auto
 */
var ButtonPanel = function(btnsLeft, btnsCenter, btnsRight) {

    // Button arrays for each section
    this.btnsLeft = btnsLeft ? btnsLeft : [];
    this.btnsCenter = btnsCenter ? btnsCenter : [];
    this.btnsRight = btnsRight ? btnsRight : [];

    this.sectionCell = function(area, btns) {
        var sectionCell = document.createElement('td');
        var areaLowerCase = area.toLowerCase();
        var tbl = document.createElement('table');
        var bdy = document.createElement('tbody');
        var row = document.createElement('tr');
        var cell = null;

        tbl.setAttribute('cellpadding', '0');
        tbl.setAttribute('cellspacing', '0');

        sectionCell.id = 'buttonPanel' + area;
        sectionCell.style.width = '33%';
        sectionCell.innerHTML = '<div id="buttonPanel' + area + 'Div" align="' + areaLowerCase + '"></div>';
        // Insert table of buttons for this section
        if (btns.length) {
            div = sectionCell.firstChild;
            div.appendChild(tbl);
            tbl.appendChild(bdy);
            bdy.appendChild(row);
            for (var i = 0; i < btns.length; i++) {
                cell = document.createElement('td');
                cell.appendChild(btns[i].domNode);
                row.appendChild(cell);
                // Spacer between buttons
                if (i < btns.length-1) {
                    cell = document.createElement('td');
                    cell.setAttribute('width', '1%');
                    cell.innerHTML = '&nbsp;';
                    row.appendChild(cell);
                }
            }
        }
        return sectionCell;
    }

    var tbl = document.createElement('table');
    var bdy = document.createElement('tbody');
    var row = document.createElement('tr');

    tbl.setAttribute('width', '100%');
    tbl.setAttribute('cellpadding', '0');
    tbl.setAttribute('cellspacing', '0');
    tbl.appendChild(bdy);
    bdy.appendChild(row);

    row.appendChild(this.sectionCell('Left', this.btnsLeft));
    row.appendChild(this.sectionCell('Center', this.btnsCenter));
    row.appendChild(this.sectionCell('Right', this.btnsRight));

    // Return DOM element of the button-panel table
    return tbl;

}

/**
 * @object NavButtonSet -- creates a two-button cluster with a
 * left and right arrow
 * Note that the line-height CSS property needs to be set
 * for the table cells otherwise they inherit from the
 * container the NavButtonSet is sitting in
 */
function NavButtonSet(id, leftHandler, rightHandler) {
    
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
