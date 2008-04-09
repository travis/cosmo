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

/*
 * @license Apache License 2.0
 */
dojo.provide("cosmo.ui.widget.NavButtonSet");
dojo.require("cosmo.env");
dojo.require("dijit._Templated");

dojo.declare(
    "cosmo.ui.widget.NavButtonSet", [dijit._Widget, dijit._Templated],
    {
        templatePath: dojo.moduleUrl("cosmo", "ui/widget/templates/NavButtonSet.html"),
        bgImgUrl: cosmo.env.getImageUrl("button_bgs.png"),
        leftImgUrl: cosmo.env.getImageUrl("nav_arrow_left.gif"),
        rightImgUrl: cosmo.env.getImageUrl("nav_arrow_right.gif"),
        _morphButton: function(e) {
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
            
        }
    }
);
