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

/**
 * @fileoverview ButtonPanel -- panel of buttons allowing three
 *      clusters of buttons: left, center, right.
 * @author Matthew Eernisse mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.ButtonPanel");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("cosmo.env");
dojo.require("cosmo.ui.widget.Button");

dojo.declare(
    "cosmo.ui.widget.ButtonPanel", 
    [dijit._Widget, dijit._Templated],
    {

        templatePath: dojo.moduleUrl("cosmo",  "ui/widget/templates/ButtonPanel.html"),

    // Attach points
    leftContainer: null,
    centerContainer: null,
    rightContainer: null,
    panelContainer: null,
    btnsLeft: [],
    btnsCenter: [],
    btnsRight: [],

    // Props set by tag or constructor
    width: null,

    postCreate: function () {
        function sectionCell(area, btns) {
            // Insert table of buttons for this section
            if (btns.length) {
                var areaLowerCase = area.toLowerCase();
                var tbl = document.createElement('table');
                var bdy = document.createElement('tbody');
                var row = document.createElement('tr');
                var cell = null;
                tbl.setAttribute('cellpadding', '0');
                tbl.setAttribute('cellspacing', '0');
                div = this[areaLowerCase + 'Container'];
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
        }
        this.setWidth(this.width);
        sectionCell.apply(this, ['left', this.btnsLeft]);
        sectionCell.apply(this, ['center', this.btnsCenter]);
        sectionCell.apply(this, ['right', this.btnsRight]);
    },

    setWidth: function (width) {
        this.width = width;
        if (width) {
            this.panelContainer.style.width = parseInt(width) + 'px';
        } else {
            this.panelContainer.style.width = '100%';
        }
    }
  } 
);
