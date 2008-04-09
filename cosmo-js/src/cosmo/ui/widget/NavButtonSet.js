if(!dojo._hasResource["cosmo.ui.widget.NavButtonSet"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.ui.widget.NavButtonSet"] = true;
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
        templateString:"<table cellpadding=\"0px\" cellspacing=\"0px\" style=\"width: 45px; height: 16px;\">\n  <tbody>\n    <tr>\n      <td id=\"${id}CenterLeft\" class=\"btnElemBaseSm\" dojoAttachPoint=\"leftButtonNode\"\n          dojoAttachEvent=\"onmouseover: _morphButton, onmouseout: _morphButton, onmousedown: _morphButton, onmouseup: _morphButton, onclick: leftClickHandler\"\n          style=\"width: 13px; height: 15px; line-height: 0px; text-align: left; background-image: url(${bgImgUrl}); border-width: 1px 0 1px 1px;\">\n        <img src=\"${leftImgUrl}\"/ \n             style=\"padding: 2px 5px 0 5px; \">\n      </td>\n      <td id=\"${id}ButtonDivider\" style=\"width: 1px; height: 16px; line-height: 1px\"\n          class=\"btnSetDividerBase\"></td>\n      <td id=\"${id}CenterRight\" class=\"btnElemBaseSm\" dojoAttachPoint=\"rightButtonNode\"\n          dojoAttachEvent=\"onmouseover: _morphButton, onmouseout: _morphButton, onmousedown: _morphButton, onmouseup: _morphButton, onclick: rightClickHandler\"\n          style=\"width: 13px; height: 15px; line-height: 0px; text-align: right; background-image: url(${bgImgUrl}); border-width: 1px 1px 1px 0;\">\n        <img src=\"${rightImgUrl}\"/ \n             style=\"padding: 2px 5px 0 5px; \">\n      </td>\n    </tr>\n  </tbody>\n</table>\n",
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

}
