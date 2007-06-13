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

dojo.provide('cosmo.ui.navbar');

dojo.require("dojo.date.common");
dojo.require("dojo.date.format");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.ui.widget.GraphicRadioButtonSet");
dojo.require("cosmo.view.cal");
dojo.require("cosmo.view.list.common");

cosmo.ui.navbar.Bar = function (p) {
    var self = this;
    var params = p || {};
    this.domNode = null;
    this.id = '';
    this.width = 0;
    this.monthHeader = null;
    for (var n in params) { this[n] = params[n]; }

    // Subscribe to the '/calEvent' channel
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub_calEvent');

    // Interface methods
    this.renderSelf = function () {
        var d = this.domNode;
        var _html = cosmo.util.html;
        var table = _createElem('table');
        var body = _createElem('tbody');
        var tr = _createElem('tr');
        table.cellPadding = '0';
        table.cellSpacing = '0';
        table.appendChild(body);
        body.appendChild(tr);
        var td = _createElem('td');
        td.id = 'viewToggle';
        this.viewToggle = td;

        this.clearAll();

        var _radio = cosmo.ui.widget.GraphicRadioButtonSet.Button;
        var w = 24;
        var btns = [
            new _radio({ width: w,
                defaultImgPos: [-360, 0],
                mouseoverImgPos: [-405, 0],
                downStateImgPos: [-450, 0],
                handleClick: function () {
                    var center = cosmo.app.pim.baseLayout.mainApp.centerColumn;
                    center.calCanvas.domNode.style.display = 'none';
                    cosmo.view.list.loadItems({ collection: cosmo.app.pim.currentCollection });
                    center.listCanvas.domNode.style.display = 'block';
                    $('viewNavButtons').style.display = 'none';
                    $('monthHeaderDiv').style.display = 'none';
                    }
                }),
            new _radio({ width: w,
                defaultImgPos: [-495, 0],
                mouseoverImgPos: [-540, 0],
                downStateImgPos: [-585, 0],
                handleClick: function () {
                    var center = cosmo.app.pim.baseLayout.mainApp.centerColumn;
                    center.calCanvas.domNode.style.display = 'block';
                    center.listCanvas.domNode.style.display = 'none';
                    $('viewNavButtons').style.display = 'block';
                    $('monthHeaderDiv').style.display = 'block';
                    }
                })
        ];
        var vT =  dojo.widget.createWidget("cosmo:GraphicRadioButtonSet", {
            selectedButtonIndex: 1, height: 35, buttons: btns }, td, 'last');
        tr.appendChild(td);
        var td = _createElem('td');
        td.style.width = '16px';
        td.appendChild(_html.nbsp());
        tr.appendChild(td);
        var td = _createElem('td');
        td.id = 'viewNavButtons';
        self.viewNavButtons = td;
        // Add week-to-week navigation
        var back = function back() {
            dojo.event.topic.publish('/calEvent', {
                action: 'loadCollection', opts: { goTo: 'back' }, data: {}
            });
        }
        var next = function next() {
            dojo.event.topic.publish('/calEvent', {
                action: 'loadCollection', opts: { goTo: 'next' }, data: {}
            });
        }
        var navButtons = new cosmo.ui.button.NavButtonSet('viewNav', back, next);
        self.viewNavButtons.appendChild(navButtons.domNode);

        tr.appendChild(td);
        var td = _createElem('td');
        td.appendChild(_html.nbsp());
        td.appendChild(_html.nbsp());
        td.appendChild(_html.nbsp());
        tr.appendChild(td);
        var td = _createElem('td');
        td.id = 'monthHeaderDiv';
        this.monthHeader = td;
        td.className = 'labelTextXL';
        tr.appendChild(td);
        d.appendChild(table);

        if (self.parent) { self.width = self.parent.width; }
        self.setSize(self.width - 2, CAL_TOP_NAV_HEIGHT-1);
        self.setPosition(0, 0);

        this._showMonthHeader();
    };
    this._showMonthHeader = function() {
        var vS = cosmo.view.cal.viewStart;
        var vE =  cosmo.view.cal.viewEnd;
        var mS = vS.getMonth();
        var mE = vE.getMonth();
        var headerDiv = this.monthHeader;
        var str = '';

        // Format like 'March-April, 2006'
        if (mS < mE) {
            str += dojo.date.strftime(vS, '%B-');
            str += dojo.date.strftime(vE, '%B %Y');
        }
        // Format like 'December 2006-January 2007'
        else if (mS > mE) {
            str += dojo.date.strftime(vS, '%B %Y-');
            str += dojo.date.strftime(vE, '%B %Y');
        }
        // Format like 'April 2-8, 2006'
        else {
            str += dojo.date.strftime(vS, '%B %Y');
        }
        if (headerDiv.firstChild) {
            headerDiv.removeChild(headerDiv.firstChild);
        }
        headerDiv.appendChild(document.createTextNode(str));
    };
    this.handlePub_calEvent = function (cmd) {
        var act = cmd.action;
        var opts = cmd.opts;
        var data = cmd.data;
        switch (act) {
            case 'eventsLoadSuccess':
                this._showMonthHeader();
                break;
        }
    };
}

cosmo.ui.navbar.Bar.prototype = new cosmo.ui.ContentBox();

cosmo.ui.navbar.components = {};



