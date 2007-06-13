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

dojo.provide('cosmo.view.list.canvas');

dojo.require('dojo.event.*');
dojo.require("cosmo.view.list.common");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.ContentBox");

cosmo.view.list.canvas.Canvas = function (p) {
    var params = p || {};
    this.domNode = null;
    this.id = '';
    for (var n in params) { this[n] = params[n]; }
    
    // Interface methods
    this.renderSelf = function () {
        this._updateSize();
        this.setPosition(0, CAL_TOP_NAV_HEIGHT);
        this.setSize();
        
        cosmo.view.list.sort(cosmo.view.list.itemRegistry);
        this._displayTable(cosmo.view.list.itemRegistry);
    }

    // Private methods
    this._updateSize = function () {
        if (this.parent) {
            this.width = this.parent.width - 2; // 2px for borders
            this.height = this.parent.height - CAL_TOP_NAV_HEIGHT;
        }
    };
    // innerHTML will be much faster for table display with
    // lots of rows
    this._displayTable = function (hash) {
        var map = cosmo.view.list.triageStatusCodeNumberMappings;
        var t = '<table cellpadding="0" cellspacing="0" style="width: 100%;">';
        var r = '';
        r += '<tr>';
        r += '<td class="headerCell">Title</td>';
        r += '<td class="headerCell">Start</td>';
        r += '<td class="headerCell" style="border-right: 0px;">Triage</td>';
        r += '</tr>';
        t += r;
        var getRow = function (key, val) {
            var item = val.data;
            var eventStamp = item.getEventStamp();
            var start = eventStamp ? eventStamp.getStartDate() : '';
            var triage = item.getTriageStatus();
            if (start) {
                start = start.strftime('%b %d, %Y %I:%M%p');
            }
            triage = map[triage];
            r = '';
            r += '<tr>';
            r += '<td class="dataCell">' + item.getDisplayName() + '</td>';
            r += '<td class="dataCell">' + start + '</td>';
            r += '<td class="dataCell">' + triage + '</td>';
            r += '</tr>';
            t += r;
        }
        hash.each(getRow);
        t += '</table>';
        this.domNode.innerHTML = t;
    };
};

cosmo.view.list.canvas.Canvas.prototype =
  new cosmo.ui.ContentBox();

