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
    tabs: [{ label: 'Tab Number One', content: 'This is the content for tab number 1.' }, 
        { label: 'Tab Number Two', content: 'This is the content for tab number 2.'  },
        { label: 'Tab Number Three', content: 'This is the content for tab number 3.'  }
        ],
    tabNodes: [],
    contentNodes: [],
    selectedTabIndex: 0,
    
    // Attach points
    tabArea: null,
    contentArea: null,
    
    // Public methods
    getTab: function (index, tabObj, sel) {
        var self = this;
        var o = tabObj;
        var d = null;
        var t = _createElem('div');
        d = _createElem('div');
        d.className = sel ? 'tabSelected' : 'tabUnselected';
        d.appendChild(_createText(o.label));
        d.onclick = function () { self.showTab(index); };
        this.tabNodes.push(d);
        t.appendChild(d);
        d = _createElem('div');
        d.className = 'tabSpacer';
        d.appendChild(_createText('\u00A0'));
        t.appendChild(d);
        
        if (typeof o.content == 'string') {
            var n = _createElem('div');
            n.innerHTML = o.content;
        }
        else {
            n = o.content;
        }
        n.style.position = 'absolute';
        n.style.top = '0px';
        n.style.left = '0px';
        n.style.display =  sel ? 'block' : 'none';
        this.contentNodes.push(n);
        
        return {tab: t, content: n };
    },
    showTab: function (index) {
        var tabs = this.tabs;
        for (var i = 0; i < tabs.length; i++) {
            if (i == index) {
                this.tabNodes[i].className = 'tabSelected'; 
                this.contentNodes[i].style.display = 'block';
            }
            else {
                this.tabNodes[i].className = 'tabUnselected';
                this.contentNodes[i].style.display = 'none';
            }
        }
    },
    
    // Private methods
    
    // Lifecycle
    fillInTemplate: function () {
        var node = this.domNode;
        var tabPanel = null;
        var tabContent = null;
        var t = null;
        var tabs = this.tabs;
        
        tabPanel = _createElem('div');
        this.tabArea = tabPanel;
        tabPanel.id = this.widgetId + '_tabPanel';
        tabPanel.className = 'tabPanel';
        tabContent = _createElem('div');
        this.contentArea = tabContent;
        tabContent.id = this.widgetId + '_contentArea';
        tabContent.className = 'tabContent';
        for (var i =0; i < tabs.length; i++) {
            var sel = i == this.selectedTabIndex ? true : false;
            t = this.getTab(i, tabs[i], sel);
            tabPanel.appendChild(t.tab);
            tabContent.appendChild(t.content);
        }
        var s = _createElem('div');
        s.className = 'tabSpacer';
        s.appendChild(_createText('\u00A0'));
        s.appendChild(_createText('\u00A0'));
        s.appendChild(_createText('\u00A0'));
        s.appendChild(_createText('\u00A0'));
        s.appendChild(_createText('\u00A0'));
        tabPanel.appendChild(s);
        node.appendChild(tabPanel);
        node.appendChild(tabContent);
    }
} );

