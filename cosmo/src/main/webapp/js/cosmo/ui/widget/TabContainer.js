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
 * @fileoverview TabContainer.js -- panel of buttons allowing three
 *      clusters of buttons: left, center, right.
 * @author Matthew Eernisse mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.TabContainer");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");

dojo.widget.defineWidget("cosmo.ui.widget.TabContainer", dojo.widget.HtmlWidget, {

    templateString: '<span></span>',

    // Props from template or set in constructor
    selectedTabIndex: 0,

    // Define these here so they don't end up as statics
    initializer: function () {
        this.tabs = [];
        this.tabNodes = [];
        this.contentNodes = [];
    },

    // Attach points
    tabArea: null,
    contentArea: null,

    // Public methods
    getTab: function (index, tabObj, sel) {
        var self = this;
        var o = tabObj;
        var d = null;
        d = _createElem('td');
        d.id = this.widgetId + '_tab' + index;
        d.className = sel ? 'tabSelected' : 'tabUnselected';
        d.appendChild(_createText(o.label));
        d.onclick = function () { self.showTab(index); };
        this.tabNodes.push(d);

        if (typeof o.content == 'string') {
            var n = _createElem('div');
            n.id = this.widgetId + '_content' + index;
            n.innerHTML = o.content;
            this.contentNodes.push(n);
        }
        else {
            if (o.content instanceof dojo.widget.HtmlWidget) {
                n = o.content.domNode;
                // For widgets, keep the actual widget as the canonical reference
                // We can grab the domNode of it as needed
                this.contentNodes.push(o.content);
            }
            else {
                n = o.content;
                this.contentNodes.push(n);
            }
        }
        n.style.position = 'absolute';
        n.style.top = '0px';
        n.style.left = '0px';
        n.style.visibility =  sel ? 'visible' : 'hidden';

        return {tab: d, content: n };
    },
    showTab: function (index) {
        var tabs = this.tabs;
        for (var i = 0; i < tabs.length; i++) {
            var tab = this.tabNodes[i];
            var content = this.contentNodes[i];
            // If the content is a widget, point us as the DOM node
            if (content instanceof dojo.widget.HtmlWidget) {
                content = content.domNode;
            }
            if (i == index) {
                tab.className = 'tabSelected';
                content.style.visibility = 'visible';
                content.style.top = '0px';
                content.style.left = '0px';
            }
            else {
                tab.className = 'tabUnselected';
                content.style.visibility = 'hidden';
                content.style.top = '-50000px';
                content.style.left = '-50000px';
            }
        }
    },

    // Private methods

    // Lifecycle
    fillInTemplate: function () {

        var tabMain = null;
        var tabPanelTable = null;
        var tabPanelTBody = null;
        var tabPanelTr = null;
        var tabContent = null;
        var t = {};
        var s = null;
        var tabs = this.tabs;

        tabMain = _createElem('div');
        tabMain.style.visibility = 'hidden';
        tabPanelTable = _createElem('table');
        tabPanelTable.style.width = '100%';
        tabPanelTable.cellPadding = '0';
        tabPanelTable.cellSpacing = '0';
        tabPanelTBody = _createElem('tbody');
        tabPanelTable.appendChild(tabPanelTBody);
        tabPanelTr = _createElem('tr');
        tabPanelTBody.appendChild(tabPanelTr);
        this.tabArea = tabPanelTable;
        tabPanelTable.id = this.widgetId + '_tabPanel';
        tabPanelTable.className = 'tabPanel';
        tabContent = _createElem('div');
        this.contentArea = tabContent;
        tabContent.id = this.widgetId + '_contentArea';
        tabContent.className = 'tabContent';
        s = _createElem('td');
        s.className = 'tabSpacer';
        s.appendChild(_createText('\u00A0'));
        s.appendChild(_createText('\u00A0'));
        tabPanelTr.appendChild(s);
        for (var i =0; i < tabs.length; i++) {
            var sel = i == this.selectedTabIndex ? true : false;

            t = this.getTab(i, tabs[i], sel);

            tabPanelTr.appendChild(t.tab);
            s = _createElem('td');
            s.className = 'tabSpacer';
            s.appendChild(_createText('\u00A0'));
            tabPanelTr.appendChild(s);

            tabContent.appendChild(t.content);
        }
        s = _createElem('td');
        s.className = 'tabSpacer';
        s.style.width = '99%';
        s.appendChild(_createText('\u00A0'));
        tabPanelTr.appendChild(s);
        tabMain.appendChild(tabPanelTable);
        tabMain.appendChild(tabContent);
        this.domNode.appendChild(tabMain);
        tabMain.style.visibility = 'visible';
        this.showTab(0);
    },
    // Clean up any sub-widgets in any of the tabs
    destroyChildren: function () {
        n = this.contentNodes;
        for (var i = 0; i < n.length; i++) {
            var c = n[i];
            if (c instanceof dojo.widget.HtmlWidget) {
                c.destroy();
            }
        }

    }
} );

