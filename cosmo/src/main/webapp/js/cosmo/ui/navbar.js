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
dojo.require("cosmo.util.html");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.ui.widget.GraphicRadioButtonSet");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.view.cal");
dojo.require("cosmo.view.list.common");

cosmo.ui.navbar.Bar = function (p) {
    var self = this;
    var params = p || {};
    this.domNode = null;
    this.id = '';
    this.width = 0;
    this.listCanvas = null;
    this.calCanvas = null;
    this.monthHeader = null;
    this.listViewPager = null;
    this.listViewPageNum = null;

    for (var n in params) { this[n] = params[n]; }

    // Subscribe to the '/calEvent' channel
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub_calEvent');

    // Interface methods
    // ===========
    // Handle published messages -- in this case just a poke
    // to let the NavBar know to re-render
    this.handlePub_calEvent = function (cmd) {
        var act = cmd.action;
        var opts = cmd.opts;
        switch (act) {
            case 'loadCollection':
            case 'navigateLoadedCollection':
                self.renderSelf();
                break;
        }
    };
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
        else if (_pim.currentView == _pim.views.LIST) {
            this._showQuickItemEntry();
            this._showListPager();
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
            cosmo.view.list.loadItems({ collection: _pim.currentCollection });
            // If the cal canvas is currently showing, save the scroll
            // offset of the timed canvas
            if (self.calCanvas.domNode.style.display == 'block') {
                cosmo.view.cal.canvas.saveTimedCanvasScrollOffset();
            }
            self.calCanvas.domNode.style.display = 'none';
            self.listCanvas.domNode.style.display = 'block';
        }
        else if (v == _pim.views.CAL) {
            cosmo.view.cal.loadEvents({ collection: _pim.currentCollection,
                viewStart: cosmo.view.cal.viewStart, viewEnd: cosmo.view.cal.viewEnd });
            self.listCanvas.domNode.style.display = 'none';
            self.calCanvas.domNode.style.display = 'block';
            cosmo.view.cal.canvas.resetTimedCanvasScrollOffset();
        }
        else {
            throw(v + ' is not a valid view.');
        }
        // Changing views also needs to re-render the nav bar
        // to show the view-specific widgets in it
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
    this._showMonthHeader = function () {
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
    this._showQuickItemEntry = function () {
        var f = null;
        var d = this.domNode;
        var t = _createElem('div');
        t.className = 'floatLeft';
        t.style.paddingLeft = '12px';
        f = _createElem('form');
        f.onsubmit = function () { return false; };
        t.appendChild(f);
        var o = { type: 'text',
            id: 'listViewQuickItemEntry',
            name: 'listViewQuickItemEntry',
            size: 24,
            className: 'inputText',
            value: ''
        };
        var text = cosmo.util.html.createInput(o);
        f.appendChild(text);
        f.appendChild(cosmo.util.html.nbsp());
        var func = function () {
            var title = f.listViewQuickItemEntry.value;
            cosmo.view.list.createNoteItem(title);
        };
        button = dojo.widget.createWidget("cosmo:Button", {
            text: 'Create',
            handleOnClick: func,
            small: true,
            width: 42,
            enabled: true },
            f, 'last');
        d.appendChild(t);

    }
    this._showListPager = function () {

        var form = null;
        // Go-to page num
        var goToPage = function (e) {
            if (e.target && e.target.id && e.keyCode && e.keyCode == 13) {
                var list = self.listCanvas;
                var n = form[e.target.id].value;
                n = n > list.pageCount ? list.pageCount : n;
                n = n < 1 ? 1 : n;
                list.currPageNum = n;
                list.render();
            }
        };
        var d = this.domNode;
        var t = _createElem('div');
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
        if (self.listCanvas.currPageNum == 1) {
            var fPrev = null;
        }
        else {
            var fPrev = self.listCanvas.goPrevPage;
        }
        button = dojo.widget.createWidget("cosmo:Button", {
            text: 'Prev',
            handleOnClick: fPrev,
            small: true,
            width: 32,
            enabled: (typeof fPrev == 'function') },
            td, 'last');
        tr.appendChild(td);

        var td = _createElem('td');
        td.id = 'listViewPageNum';
        td.style.padding = '0px 8px';
        form = _createElem('form');
        // Suppress submission
        form.onsubmit = function () { return false; };
        td.appendChild(form);
        var o = { type: 'text',
            id: 'listViewGoToPage',
            name: 'listViewGoToPage',
            size: 1,
            className: 'inputText',
            value: this.listCanvas.currPageNum
        };
        var text = cosmo.util.html.createInput(o);
        dojo.event.connect(text, 'onkeyup', goToPage);
        form.appendChild(text);
        td.appendChild(_createText(' of ' + this.listCanvas.pageCount));
        this.listViewPageNum = td;
        tr.appendChild(td);

        var td = _createElem('td');
        if (self.listCanvas.currPageNum == self.listCanvas.pageCount) {
            var fNext = null;
        }
        else {
            var fNext = self.listCanvas.goNextPage;
        }
        button = dojo.widget.createWidget("cosmo:Button", {
            text: 'Next',
            handleOnClick: fNext,
            small: true,
            width: 32,
            enabled: (typeof fNext == 'function') },
            td, 'last');
        tr.appendChild(td);

        t.appendChild(table);
        d.appendChild(t);
        this.listViewPager = t;
    };
}

cosmo.ui.navbar.Bar.prototype = new cosmo.ui.ContentBox();

cosmo.ui.navbar.components = {};



