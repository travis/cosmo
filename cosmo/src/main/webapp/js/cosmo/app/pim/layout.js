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

dojo.provide("cosmo.app.pim.layout");

dojo.require("cosmo.app");
dojo.require("cosmo.app.pim");
dojo.require('cosmo.ui.resize')
dojo.require("cosmo.ui.ContentBox");
dojo.require("dojo.html.common");

// -- Create global vars, do not remove despite lack of refs in code
dojo.require("cosmo.ui.conf");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require('cosmo.convenience');
// --

dojo.require('cosmo.view.loading');
dojo.require('cosmo.view.cal.common');
dojo.require('cosmo.view.cal.canvas');
dojo.require('cosmo.view.list.canvas');

// -- Widget includes, may not always find proper namespaced refs
// -- ie, cosmo:CollectionSelector
dojo.require("cosmo.ui.widget.CollectionSelector");
// --
dojo.require("cosmo.ui.minical");
dojo.require("cosmo.ui.menu");
dojo.require("cosmo.ui.navbar");
dojo.require("cosmo.ui.detail");
dojo.require("cosmo.ui.imagegrid");
dojo.require("cosmo.ui.selector");

cosmo.app.pim.layout = new function () {
    this.baseLayout = null;
    // Min/max vals for the linear interpolation used in
    // the viewport code to do sizing and placement of
    // the left/right sidebars and center column
    this.screenConstraints = {
        MIN_WIDTH: 320,
        MIN_HEIGHT: 240,
        MAX_WIDTH: 12800,
        MAX_HEIGHT: 9600
    };
    this.initBaseLayout = function (node) {
        cosmo.ui.resize.Viewports.initialize();
        this.baseLayout = new cosmo.app.pim.layout.BaseLayout(node);
        this.baseLayout.render();
        this.populateBaseLayout();
        // Hide select boxes in IE before rendering the UI widgets
        // so they don't bleed through the mask
        // cosmo.app.hideMask will show them again after the mask
        // goes away
        cosmo.app.showHideSelectBoxes(false);
        return this.baseLayout;
    };
};

cosmo.app.pim.layout.BaseLayout = function (p) {
    var params = p || {};
    this.top = 0;
    this.left = 0;
    this.width = 0;
    this.height = 0;
    this.domNode = params.domNode;
    this.menuBar = new cosmo.app.pim.layout.MenuBar({ parent: this });
    this.mainApp = new cosmo.app.pim.layout.MainApp({ parent: this });
    this.children = [this.menuBar, this.mainApp];
    this.renderSelf = function () {
        var viewport = dojo.html.getViewport();
        var w = viewport.width;
        var h = viewport.height;
        // Pare width and height down to avoid
        // stupid scrollbars showing up
        w -= 2;
        h -= 2;
        this.width = w;
        this.height = h;
        this.menuBar.update({
            top: 0, left: 0,
            width: this.width, height: (TOP_MENU_HEIGHT - 1) });
        this.mainApp.update({
            top: TOP_MENU_HEIGHT, left: 0,
            width: this.width, height: (this.height - TOP_MENU_HEIGHT) });
        this.setPosition();
        this.setSize();
        cosmo.ui.resize.Viewports.resize();
    }
};
cosmo.app.pim.layout.BaseLayout.prototype =
    new cosmo.ui.ContentBox();

cosmo.app.pim.layout.MenuBar = function (p) {
    var params = p || {};
    for (var n in params) { this[n] = params[n]; }

    var d = _createElem('div');
    d.id = 'menuBar';
    this.parent.domNode.appendChild(d);

    this.domNode = d;
    this.children = [];
    this.renderSelf = function () {
        this.setPosition();
        this.setSize();
        if (!this.hasBeenRendered) {
            this.parent.domNode.appendChild(this.domNode);
            this.hasBeenRendered = true;
        }
    }
};
cosmo.app.pim.layout.MenuBar.prototype =
    new cosmo.ui.ContentBox();

cosmo.app.pim.layout.MainApp = function (p) {

    var params = p || {};
    for (var n in params) { this[n] = params[n]; }

    var d = _createElem('div');
    d.id = 'mainApp';

    this.domNode = d;
    this.centerColumn = new cosmo.app.pim.layout.CenterColumn({ parent: this });
    this.leftSidebar = new cosmo.app.pim.layout.LeftSidebar({ parent: this });
    this.rightSidebar = new cosmo.app.pim.layout.RightSidebar({ parent: this });
    this.children = [this.leftSidebar, this.centerColumn, this.rightSidebar];
    this.renderSelf = function () {
        this.setPosition();
        this.setSize();
        this.leftSidebar.update({ height: this.height });
        this.centerColumn.update({ width:
            (this.width - LEFT_SIDEBAR_WIDTH - RIGHT_SIDEBAR_WIDTH),
            height: this.height });
        if (!this.hasBeenRendered) {
            this.parent.domNode.appendChild(this.domNode);
            this.hasBeenRendered = true;
        }
    }
};
cosmo.app.pim.layout.MainApp.prototype =
    new cosmo.ui.ContentBox();

cosmo.app.pim.layout.LeftSidebar = function (p) {
    var params = p || {};
    for (var n in params) { this[n] = params[n]; }

    // create domNodes
    var d = _createElem('div');
    d.id = 'leftSidebar';
    d.className = "viewport"
    this.parent.domNode.appendChild(d);
    /*
    d.style.paddingRight = "4px";
    var handle = _createElem('a');
    handle.className = "l-rHandle leftSideHandle";
    d.appendChild(handle);
    */
    this.domNode = d;
    this.children = [];
    this.renderSelf = function () {};

    var constr = cosmo.app.pim.layout.screenConstraints;
    var vp = new cosmo.ui.resize.Viewport(d);
    // add scaling
    // (screenwidth, screenheight, left,top,right,botton)
    vp.setMinSize([
        constr.MIN_WIDTH,
        constr.MIN_HEIGHT,
        0,
        0,
        LEFT_SIDEBAR_WIDTH,
        (constr.MIN_HEIGHT - TOP_MENU_HEIGHT)
    ]);
    vp.setMaxSize([
        constr.MAX_WIDTH,
        constr.MAX_HEIGHT,
        0,
        0,
        LEFT_SIDEBAR_WIDTH,
        (constr.MAX_HEIGHT - TOP_MENU_HEIGHT)
    ]);
    vp.addResize("renderSelf",this.renderSelf);
    /*
    var h = new cosmo.ui.resize.Handle(handle);
    h.addViewport(d.id,"right");
    h.addViewport('centerColumn','left');
    h.addCollapse(d.id,"left");
    */
};
cosmo.app.pim.layout.LeftSidebar.prototype =
    new cosmo.ui.ContentBox();

cosmo.app.pim.layout.CenterColumn = function (p) {
    var params = p || {};
    for (var n in params) { this[n] = params[n]; }
    this.children = [];
    var d = _createElem('div');
    d.id = 'centerColumn';
    d.className = "viewport"
    this.parent.domNode.appendChild(d);

    this.domNode = d;
    this.children = [];
    this.renderSelf = function () {
        //add any special child rendering
    }
    //viewport fun
    var constr = cosmo.app.pim.layout.screenConstraints;
    var vp = new cosmo.ui.resize.Viewport(d)
    // add scaling
    vp.setMinSize([
        constr.MIN_WIDTH,
        constr.MIN_HEIGHT,
        (LEFT_SIDEBAR_WIDTH + 1),
        0,
        (constr.MIN_WIDTH - RIGHT_SIDEBAR_WIDTH),
        (constr.MIN_HEIGHT - TOP_MENU_HEIGHT)
    ]);
    vp.setMaxSize([
        constr.MAX_WIDTH,
        constr.MAX_HEIGHT,
        (LEFT_SIDEBAR_WIDTH + 1),
        0,
        (constr.MAX_WIDTH - RIGHT_SIDEBAR_WIDTH),
        (constr.MAX_HEIGHT - TOP_MENU_HEIGHT)
    ]);
    vp.addResize("renderSelf",this.renderSelf)    ;
};
cosmo.app.pim.layout.CenterColumn.prototype =
    new cosmo.ui.ContentBox();

cosmo.app.pim.layout.RightSidebar = function (p) {
    var params = p || {};
    for (var n in params) { this[n] = params[n]; }

    var d = _createElem('div');
    d.id = 'rightSidebar';
    d.className = "viewport";
    this.parent.domNode.appendChild(d);
    /*
    var handle = _createElem('a');
    handle.className = "l-rHandle";
    with (handle.style) {
        left = "0px";
        height = "100%";
        width = "4px";
        backgroundColor = "rgb(216,216,216)";
    }
    d.appendChild(handle);
    */
    this.domNode = d;
    this.children = [];
    this.renderSelf = function () {
        // add special rendering
    }
    this.children = [];
    //viewport fun
    var constr = cosmo.app.pim.layout.screenConstraints;
    var vp = new cosmo.ui.resize.Viewport(d);
    vp.setMinSize([
        constr.MIN_WIDTH,
        constr.MIN_HEIGHT,
        (constr.MIN_WIDTH - RIGHT_SIDEBAR_WIDTH - 1),
        0,
        constr.MIN_WIDTH,
        (constr.MIN_HEIGHT - TOP_MENU_HEIGHT)
    ]);
    vp.setMaxSize([
        constr.MAX_WIDTH,
        constr.MAX_HEIGHT,
        (constr.MAX_WIDTH - RIGHT_SIDEBAR_WIDTH - 1),
        0,
        constr.MAX_WIDTH,
        (constr.MAX_HEIGHT - TOP_MENU_HEIGHT)
    ]);
    vp.addResize("renderSelf",this.renderSelf);
    /*
    var h = new cosmo.ui.resize.Handle(handle);
    h.addViewport(d.id,"left");
    h.addViewport('centerColumn','right');
    h.addCollapse(d.id,"right");
    */
};
cosmo.app.pim.layout.RightSidebar.prototype =
    new cosmo.ui.ContentBox();

cosmo.app.pim.layout.populateBaseLayout = function () {

    var menuBar = this.baseLayout.menuBar;
    var centerColumn = this.baseLayout.mainApp.centerColumn;
    var leftSidebar = this.baseLayout.mainApp.leftSidebar;
    var rightSidebar = this.baseLayout.mainApp.rightSidebar;

    // Main logo graphic
    var logoDiv = _createElem('div');
    logoDiv.id = 'mainLogoContainer';
    logoDiv = cosmo.ui.imagegrid.createImageIcon({ domNode: logoDiv,
        iconState: 'mainLogoGraphic' });
    logoDiv.style.position = 'absolute';
    logoDiv.style.top = '0px';
    logoDiv.style.left = '6px';
    menuBar.domNode.appendChild(logoDiv);

    // Main menu of links at the top of the UI
    var menuDiv = _createElem('div');
    menuDiv.id = 'menuNavItems';
    var cB = new  cosmo.ui.menu.MainMenu({ domNode: menuDiv, id: menuDiv.id, top: 4});
    menuBar.addChild(cB);
    menuBar.mainMenu = cB;
    cB.render(); // Go ahead and render the menubar -- no waiting for data

    // List view canvas
    var listDiv = _createElem('div');
    listDiv.id = 'listViewContainer';
    var list = new cosmo.view.list.canvas.Canvas({ domNode: listDiv, id: listDiv.id,
        width: centerColumn.width - 2, // 2px for borders
        height: centerColumn.height - CAL_TOP_NAV_HEIGHT });
    centerColumn.addChild(list);
    centerColumn.listCanvas = list;

    // Cal view canvas -- namespace singleton and Canvas ContentBox obj
    // are bolted together in an unpleasant way here
    var cal = new cosmo.view.cal.canvas.Canvas({
        viewStart: cosmo.view.cal.viewStart,
        viewEnd: cosmo.view.cal.viewEnd,
        currDate: cosmo.app.pim.currDate
    });
    centerColumn.addChild(cal);
    centerColumn.calCanvas = cal;

    var loadingDiv = _createElem('div');
    loadingDiv.id = 'viewLoading';
    var loading = new cosmo.view.loading.StatusMessage({ domNode: loadingDiv,
        id: loadingDiv.id });
    centerColumn.addChild(loading);
    centerColumn.loading  = loading;
    loading.render();

    // Navbar for the two views -- list and cal
    // Pass in refs to the two view widgets
    var navbarDiv = _createElem('div');
    navbarDiv.id = 'calTopNavDiv';
    var navBar = new cosmo.ui.navbar.Bar({ domNode: navbarDiv, id: navbarDiv.id,
        width: centerColumn.width,
        listCanvas: list,
        calCanvas: cal });
    centerColumn.addChild(navBar);
    centerColumn.navBar = navBar;

    // Collection name for ticket view, or collection selector
    var selectorDiv = _createElem('div');
    selectorDiv.id = 'collectionDisplay';

    // Ticket view -- display the collection name with subscription icon
    if (cosmo.app.pim.ticketKey) {
        var cB = new cosmo.ui.ContentBox({ domNode: selectorDiv, id: selectorDiv.id });
        leftSidebar.addChild(cB);
        leftSidebar.collectionSelector = cB;
        var widget = dojo.widget.createWidget('cosmo:CollectionSelector', {
            collection: cosmo.app.pim.getSelectedCollection(),
            ticketKey: cosmo.app.pim.ticketKey }, selectorDiv, 'last');
        cB.widget = widget;
    }
    // Logged-in view -- use the collection selector
    else {
        var cB = new cosmo.ui.selector.CollectionSelector({ domNode: selectorDiv,
            id: selectorDiv.id });
        leftSidebar.addChild(cB);
        leftSidebar.collectionSelector = cB;
    }

    // Minical -- subclassed ContentBox
    var miniCalDiv = _createElem('div');
    miniCalDiv.id = 'miniCal';
    cB = new cosmo.ui.minical.MiniCal({ domNode: miniCalDiv, currDate:
        cosmo.app.pim.currDate });
    leftSidebar.addChild(cB);
    leftSidebar.minical = cB;
    // Not dependent on data loading -- just render
    cB.render();

    // Detail-view form
    var detailDiv = _createElem('div');
    detailDiv.id = 'detailViewForm';
    var cB = new  cosmo.ui.detail.DetailViewForm({ domNode: detailDiv,
        id: detailDiv.id, top: 0 });
    rightSidebar.addChild(cB);
    rightSidebar.detailViewForm = cB;
    // Not dependent on data loading -- just render
    rightSidebar.render();

};
