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
dojo.require("cosmo.env");

dojo.declare("cosmo.ui.widget.GraphicRadioButtonSet", [dijit._Widget, dijit._Templated], {

    templateString: '<span><table cellpadding="0" cellspacing="0" id="${id}_buttonSet class="buttonSet"><tbody><tr dojoAttachPoint="mainRow"></tr></tbody></table></span>',

    mainRow: null,

    buttons: null,
    buttonProps: null,
    // Props from template or set in constructor
    selectedButtonIndex: null, // No default selected button

    constructor: function () {
        this.buttons = [];
    },

    // Private
    // Public

    // Lifecycle
    postCreate: function () {
        var tr = this.mainRow;
        for (var i = 0; i < this.buttonProps.length; i++) {
            var button = new cosmo.ui.widget.GraphicRadioButtonSet.Button(dojo.mixin(this.buttonProps[i],
                {parent: this, otherButtons: this.buttons, index: i, selectedIndex: this.selectedButtonIndex}));
            this.buttons.push(button);
            tr.appendChild(button.domNode);
        }
    }
} );

dojo.declare("cosmo.ui.widget.GraphicRadioButtonSet.Button", [dijit._Widget, dijit._Templated], {
    parentId: null,
    otherButtons: null,
    index: null,
    selectedIndex: null,
    templateString: "<td id='${parent.id}_button${index}' class='cosmoGraphicRadioButton ${defaultImgSel}' "
                    + "dojoAttachEvent='onmouseover: _handleMouseover, onmouseout: _handleMouseout, onclick: _handleClick'>&nbsp;</td>",
    postCreate: function () {
        if (this.index == this.selectedIndex) dojo.addClass(this.domNode, this.downStateImgSel);
    },
    _handleMouseover: function (e) {
        dojo.addClass(this.domNode, this.mouseoverStateImgSel);
    },
    _handleMouseout: function (e) {
        dojo.removeClass(this.domNode, this.mouseoverStateImgSel);
    },
    _handleClick: function (e) {
        parent.selectedButtonIndex = this.index;
        var buttons = this.otherButtons;
        for (var i = 0; i < buttons.length; i++) {
            dojo.removeClass(buttons[i].domNode, buttons[i].downStateImgSel);
        }
        dojo.addClass(this.domNode, this.downStateImgSel);
        this.handleClick();
    }
});
