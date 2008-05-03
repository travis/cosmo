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

/**
 * @fileoverview functions to do background styling for images in image grid
 * @author Matthew Eernisse mailto:mde@osafoundation.org, Travis Vachon mailto:travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.imagegrid");

dojo.require("cosmo.convenience");
dojo.require("cosmo.util.html");

cosmo.ui.imagegrid.DISABLED_OPACITY = 0.3;

cosmo.ui.imagegrid.createImageIcon = function (p) {
    //domNode, iconState
    var params = p || {};
    params.defaultState = p.iconState;
    params.isButton = false;
    return cosmo.ui.imagegrid._createImageBox(params);
};

cosmo.ui.imagegrid.createImageButton = function (p) {
    //domNode, defaultState, rolloverState
    var params = p || {};
    params.isButton = true;
    return cosmo.ui.imagegrid._createImageBox(params);
};

cosmo.ui.imagegrid.keyToSelector = function(key){
    return 'cosmo' + key[0].toUpperCase() + key.slice(1);
};

cosmo.ui.imagegrid._createImageBox = function(p) {
    //domNode, defaultState, rolloverState, isButton
    var d = p.domNode;
    var enabled = typeof p.enabled == 'boolean' ? p.enabled : true;
    dojo.addClass(d, this.keyToSelector(p.defaultState));
    if (enabled) {
        // Give buttons the hand
        if (p.isButton) {
            d.style.cursor = 'pointer';
        }
        // Selected/default state -- default state gets the rollover
        if (p.selected) {
          dojo.addClass(d, cosmo.ui.imagegrid.keyToSelector(p.rolloverState));
        }
        else if (p.rolloverState) {
            var over = function (e) {
                // Do this look up locally so the value doesn't persist
                // in the closure
                dojo.addClass(d, cosmo.ui.imagegrid.keyToSelector(p.rolloverState));
                // Handle any extra mouseover function specified
                if (p.handleMouseOver) {
                  p.handleMouseOver(e);
                }
            };
            var out = function (e) {
                dojo.removeClass(d, cosmo.ui.imagegrid.keyToSelector(p.rolloverState));
                // Handle any extra mouseout function specified
                if (p.handleMouseOut) {
                  p.handleMouseOut(e);
                }
            };
            dojo.connect(d, 'onmouseover', over);
            dojo.connect(d, 'onmouseout', out);
        }
        // Attach click handler is there is one
        if (typeof p.handleClick == 'function') {
            dojo.connect(d, 'onclick', p.handleClick);
        }
    }
    else {
        cosmo.util.html.setOpacity(d,
            cosmo.ui.imagegrid.DISABLED_OPACITY);
    }
    return d;
};
