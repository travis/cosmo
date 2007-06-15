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
dojo.require("cosmo.ui.button");
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
        var _pim = cosmo.app.pim;
        this.clearAll();
        // Both views -- floated left
        this._addViewToggle();
        // Cal view -- floated right
        if (_pim.currentView == _pim.views.CAL) {
            this._addCalViewNav();
            this._showMonthHeader();
        }
        // Break the float of the different NavBar items
        var t = _createElem('div');
        t.className = 'clearBoth';
        this.domNode.appendChild(t);

        if (self.parent) { self.width = self.parent.width; }
        self.setSize(self.width - 2, CAL_TOP_NAV_HEIGHT-1);
        self.setPosition(0, 0);
    };
    this.displayView = function (v) {
        var _pim = cosmo.app.pim;
        _pim.currentView = v;
        if (v == _pim.views.LIST) {
            var center = _pim.baseLayout.mainApp.centerColumn;
            cosmo.view.list.loadItems({ collection: _pim.currentCollection });
            // If the cal canvas is currently showing, save the scroll
            // offset of the timed canvas
            if (center.calCanvas.domNode.style.display == 'block') {
                cosmo.view.cal.canvas.saveTimedCanvasScrollOffset();
            }
            center.calCanvas.domNode.style.display = 'none';
            center.listCanvas.domNode.style.display = 'block';
        }
        else if (v == _pim.views.CAL) {
            var center = _pim.baseLayout.mainApp.centerColumn;
            cosmo.view.cal.loadEvents({ collection: _pim.currentCollection,
                viewStart: cosmo.view.cal.viewStart, viewEnd: cosmo.view.cal.viewEnd });
            center.listCanvas.domNode.style.display = 'none';
            center.calCanvas.domNode.style.display = 'block';
            cosmo.view.cal.canvas.resetTimedCanvasScrollOffset();
        }
        else {
            throw(v + ' is not a valid view.');
        }
        self.renderSelf();
    };

    // Private methods
    this._addViewToggle = function () {
        var d = this.domNode;
        var _pim = cosmo.app.pim;
        var selButtonIndex = _pim.currentView == _pim.views.LIST ? 0 : 1;
        var _radio = cosmo.ui.widget.GraphicRadioButtonSet.Button;
        var w = 24;
        var btns = [
            new _radio({ width: w,
                defaultImgPos: [-360, 0],
                mouseoverImgPos: [-405, 0],
                downStateImgPos: [-450, 0],
                handleClick: function () { 
                    self.displayView(cosmo.app.pim.views.LIST); }
                }),
            new _radio({ width: w,
                defaultImgPos: [-495, 0],
                mouseoverImgPos: [-540, 0],
                downStateImgPos: [-585, 0],
                handleClick: function () { 
                    self.displayView(cosmo.app.pim.views.CAL); }
                })
        ];
        var t = _createElem('div');
        t.id = 'viewToggle';
        t.className = 'floatLeft';
        this.viewToggle = t;
        var vT =  dojo.widget.createWidget("cosmo:GraphicRadioButtonSet", {
            selectedButtonIndex: selButtonIndex, height: 35, buttons: btns }, t, 'last');
        d.appendChild(t);
    };
    this._addCalViewNav = function () {
        var d = this.domNode;
        var _html = cosmo.util.html;
        var t = _createElem('div');
        t.id = 'calViewNav';
        t.className = 'floatRight';
        t.style.paddingRight = '12px';

        var table = _createElem('table');
        var body = _createElem('tbody');
        var tr = _createElem('tr');
        table.cellPadding = '0';
        table.cellSpacing = '0';
        table.appendChild(body);
        body.appendChild(tr);
        // Month/year header
        var td = _createElem('td');
        td.id = 'monthHeaderDiv';
        this.monthHeader = td;
        td.className = 'labelTextXL';
        tr.appendChild(td);
        // Spacer
        var td = _createElem('td');
        td.style.width = '12px';
        td.appendChild(_html.nbsp());
        tr.appendChild(td);
        // Buttons
        var td = _createElem('td');
        td.id = 'viewNavButtons';
        self.viewNavButtons = td;
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

        t.appendChild(table);
        d.appendChild(t);
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

}

cosmo.ui.navbar.Bar.prototype = new cosmo.ui.ContentBox();

cosmo.ui.navbar.components = {};



