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
 * @fileoverview ModalDialog -- modal dialog, including full-window
 * masking div to prevent user from interacting with underlying
 * doc when dialog is showing. Content area can be a string of
 * HTML or a DOM node to insert. If the title property is not
 * empty, the dialog has a rectangular title bar at the top.
 * the defaultAction prop should be a function object to be
 * executed by default from the Enter key if the dialog is being
 * displayed.
 * @author Matthew Eernisse mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.ModalDialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.widget.ButtonPanel");
dojo.require("cosmo.ui.widget.Button");

dojo.widget.defineWidget("cosmo.ui.widget.ModalDialog", 
dojo.widget.HtmlWidget, {
        // Template stuff
        templateString: '<div id="modalDialog"></div>',
        
        // Attach points
        dropShadowOuterNode: null,
        dropShadowRightNode: null,
        dropShadowBottomNode: null,
        containerNode: null,
        titleNode: null,
        promptNode: null,
        imageNode: null,
        contentNode: null,
        buttonPanelNode: null,
        
        INFO: 'info',
        ERROR: 'error',
        CONFIRM: 'confirm',
        width: null,
        height: null,
        _containerHeight: null,
        title: '',
        prompt: '',
        content: null,
        btnsLeft: [],
        btnsCenter: [],
        btnsRight: [],
        btnPanel: null,
        uiFullMask: null,
        defaultAction: null,
        isDisplayed: false,
        
        // Instance methods
        setTop: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.top = s;
        },
        setLeft: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.left = s;
        },
        setWidth: function (n) {
            if (n) { this.width = n; }
            var s = this.width.toString();
            s = this._rationalizeUnits(s);
            this.dropShadowOuterNode.style.width = s;
            var s = this.dropShadowOuterNode.offsetWidth;
            this.containerNode.style.width = s + 'px';
            s += 16;
            this.dropShadowOuterNode.style.width = s + 'px';
            this.dropShadowBottomNode.style.width = (s - 48) + 'px';
            return true;
        },
        setHeight: function (n) {
            if (n) { this.height = n; }
            var s = this.height.toString();
            s = this._rationalizeUnits(s);
            this.dropShadowOuterNode.style.height = s;
            var s = this.dropShadowOuterNode.offsetHeight;
            // Stash container height since Safari's DOM lookups
            // don't seem to happen in any particular order
            this._containerHeight = s; 
            this.containerNode.style.height = s + 'px';
            s += 16;
            this.dropShadowOuterNode.style.height = s + 'px';
            this.dropShadowRightNode.style.height = (s - 32) + 'px';
            return true;
        },
        _rationalizeUnits: function (s) {
            if (s.indexOf('%') > -1) {
                return s;
            }
            else if (s.indexOf('px') > -1) {
                return s;
            }
            else if (s.indexOf('em') > -1) {
                return s;
            }
            else {
                return parseInt(s) + 'px';
            }
        },
        setContentAreaHeight: function () {
            var spacer = this.buttonPanelNode.offsetHeight;
            spacer += 32;
            if (this.title) {
                spacer += this.titleNode.offsetHeight;
            }
            if (this.prompt) {
                spacer += this.promptNode.offsetHeight;
            }
            var o = this._containerHeight;
            this.contentNode.style.height = (o - spacer) + 'px';
            
            // BANDAID: Hack to get Safari to render the height of the 
            // content area properly
            if (navigator.userAgent.indexOf('Safari') > -1) {
                this.contentNode.style.border = '1px solid #ffffff';
                this.contentNode.style.overflow = 'auto';
            }
        },
        setTitle: function (title) {
            this.title = title || this.title;
            if (this.title) {
                this.titleNode.className = 'dialogTitle';
                this.titleNode.innerHTML = this.title;
            }
            else {
                this.titleNode.className = 'invisible';
                this.titleNode.innerHTML = '';
            }
            return true;
        },
        setPrompt: function (prompt, promptType) {
            this.prompt = prompt || this.prompt;
            if (this.prompt) {
                this.promptNode.className = 'dialogPrompt';
                if (promptType) {
                    if (promptType = 'error') {
                        this.promptNode.className += ' promptTextError';
                    }
                }
                this.promptNode.innerHTML = this.prompt;
            }
            else {
                this.promptNode.className = 'invisible';
                this.promptNode.innerHTML = '';
            }
            return true;
        },
        setContent: function (content) {
            this.content = content || this.content;
            // Content area
            if (typeof this.content == 'string') {
                this.contentNode.innerHTML = this.content;
            } else if (dojo.html.isNode(this.content)) {
                this._removeChildren(this.contentNode);
                this.contentNode.appendChild(this.content);
            } else if (this.content instanceof dojo.widget.HtmlWidget){
                this._removeChildren(this.contentNode);
                this.contentNode.appendChild(this.content.domNode);
            }
            return true;
        },
        setButtons: function (l, c, r) {
            var bDiv = this.buttonPanelNode;
            // Reset buttons if needed
            this.btnsLeft = l || this.btnsLeft;
            this.btnsCenter = c || this.btnsCenter;
            this.btnsRight = r || this.btnsRight;
            
            // Clean up previous panel if any
            if (this.btnPanel) {
                this.btnPanel.destroyButtons();
                if (bDiv.firstChild) {
                    bDiv.removeChild(bDiv.firstChild);
                }
                this.btnPanel.destroy();
            }
           
            // Create and append the panel
            // Append the panel as part of instantiation -- if done without
            // a parent element to append to, the widget parser uses document.body,
            // which causes the doc to reflow -- and
            // scrolling canvas scrollOffset gets reset
            this.btnPanel = dojo.widget.createWidget(
                'cosmo:ButtonPanel', { btnsLeft: this.btnsLeft, btnsCenter: this.btnsCenter,
                btnsRight: this.btnsRight }, bDiv, 'last');
            return true;
        },
        render: function () {
            this.setTitle();
            this.setPrompt();
            this.setContent();
            this.setButtons();
            return true;
        },
        center: function () {
            var w = dojo.html.getViewport().width;
            var h = dojo.html.getViewport().height;
            this.setLeft(parseInt((w - this.width)/2));
            this.setTop(parseInt((h - this.height)/2));
            return true;
        },
        renderUiMask: function () {
            if (!this.uiFullMask) {
                m = document.createElement('div');
                m.style.display = 'none';
                m.style.position = 'absolute';
                m.style.top = '0px';
                m.style.left = '0px';
                m.style.width = '100%';
                m.style.height = '100%';
                m.style.zIndex = 1999;
                m.style.background = '#ffffff';
                cosmo.util.html.setOpacity(m, 0.8);
                this.uiFullMask = m;
                document.body.appendChild(this.uiFullMask);
            }
            this.uiFullMask.style.display = 'block';
            return true;
        },
        _removeChildren: function(node){
            while(node.firstChild) {
                node.removeChild(node.firstChild);
            }
        },
        _setUpDialog: function () {
            var table = _createElem('table');
            var body = _createElem('tbody');
            var row = null; // Temp for row
            var td = null; // Temp for cells
            table.cellPadding = '0';
            table.cellSpacing = '0';
            table.className = 'dropShadowTable';

            row = _createElem('tr');
            
            cell = _createElem('td');
            cell.colSpan = 4;
            cell.className = 'dropShadowTop';
            cell.innerHTML = '&nbsp;';
            row.appendChild(cell);
            
            body.appendChild(row);
            
            row = _createElem('tr');
            
            cell = _createElem('td');
            cell.rowSpan = 3;
            cell.className = 'dropShadowLeft';
            cell.innerHTML = '&nbsp;';
            row.appendChild(cell);
            
            cell = _createElem('td');
            cell.rowSpan = 2;
            cell.colSpan = 2;
            cell.id = 'dialogCenterContent';
            cell.className = 'dropShadowCenter';
            cell.innerHTML = '&nbsp;';
            row.appendChild(cell);
            
            this.containerNode = cell;
            
            var d = _createElem('div');
            d.id = "modalDialogTitle";
            this.titleNode = d;
            cell.appendChild(d);

            var d = _createElem('div');
            d.id = "modalDialogPrompt";
            this.promptNode = d;
            cell.appendChild(d);
            
            var d = _createElem('div');
            d.id = "modalDialogImage";
            this.imageNode = d;
            cell.appendChild(d);
            
            var d = _createElem('div');
            d.id = "modalDialogContent";
            this.contentNode = d;
            cell.appendChild(d);
            
            var d = _createElem('div');
            d.id = "modalDialogButtonPanel";
            d.className = 'dialogButtonPanel';
            this.buttonPanelNode = d;
            cell.appendChild(d);

            cell = _createElem('td');
            cell.className = 'dropShadowTopRightCorner';
            cell.innerHTML = '&nbsp;';
            row.appendChild(cell);
            
            body.appendChild(row);

            row = _createElem('tr');
            
            cell = _createElem('td');
            cell.className = 'dropShadowRight';
            cell.innerHTML = '&nbsp;';
            this.dropShadowRightNode = cell;
            row.appendChild(cell);
            
            body.appendChild(row);

            row = _createElem('tr');
            
            cell = _createElem('td');
            cell.className = 'dropShadowBottomLeft';
            cell.innerHTML = '&nbsp;';
            row.appendChild(cell);
            
            cell = _createElem('td');
            cell.className = 'dropShadowBottom';
            //cell.style.width = (w - 48) + 'px';
            cell.innerHTML = '&nbsp;';
            this.dropShadowBottomNode = cell;
            row.appendChild(cell);
            
            cell = _createElem('td');
            cell.className = 'dropShadowBottomRightCorner';
            cell.innerHTML = '&nbsp;';
            row.appendChild(cell);
            
            body.appendChild(row);
            table.appendChild(body);
            this.dropShadowOuterNode = table;
            
            this.domNode.appendChild(table);
        },
        
        // Lifecycle functions
        postMixInProperties: function () {
            this.toggleObj =
                dojo.lfx.toggle[this.toggle] || dojo.lfx.toggle.plain;
                
            // reference to original show method
            this.showOrig = this.show;
            
            // Do sizing, positioning, content update
            // before calling stock Dojo show
            this.show = function (content, l, c, r, title, prompt) {
                
                // Set style visibility to hidden -- display needs to be
                // block in order to do sizing/positioning, but we don't
                // want to see stuff shifting around after we can see the
                // dialog box
                this.domNode.style.visibility = 'hidden';
                
                // Accommodate either original multiple param or
                // object param input
                // FIXME: 'content' passed could be a DOM node, which is
                // an obj -- however only older code uses the param style,
                // and most older code uses HTML strings for content
                if (typeof arguments[0] == 'object') {
                    var o = arguments[0];
                    for (var i in o) {
                        this[i] = o[i];
                    }
                }
                else {
                    this.content = content || this.content;
                    this.btnsLeft = l || this.btnsLeft;
                    this.btnsCenter = c || this.btnsCenter;
                    this.btnsRight = r || this.btnsRight;
                    this.title = title || this.title;
                    this.prompt = prompt || this.prompt;
                }
                // Sizing
                this.width = this.width || DIALOG_BOX_WIDTH;
                this.height = this.height || DIALOG_BOX_HEIGHT;                
                
                this._setUpDialog();
                
                var waitForIt = this.render() && this.center();
                this.renderUiMask();
                this.domNode.style.display = 'block';
                this.domNode.style.zIndex = 2000;

                // Have to measure for content area height once div is actually on the page
                if (this.setWidth() &&
                this.setHeight()) {
                this.setContentAreaHeight();
                }
                if (this.content instanceof dojo.widget.HtmlWidget 
                    && this.content.appendedToParent){
                    this.content.appendedToParent(this);
                }
                
                // Re-set visibility to visible now that everything is sized/positioned
                this.domNode.style.visibility = 'visible';
                
                this.isDisplayed = true;

            };

            // Clear buttons and actually take the div off the page
            this.hide = function () {
                var bDiv = this.buttonPanelNode;

                // Clean up previous panel if any
                if (this.btnPanel) {
                    if (bDiv.firstChild) {
                        bDiv.removeChild(bDiv.firstChild);
                    }
                    this.btnPanel.destroy();
                }
 
                this.title = '';
                this.prompt = '';
                this.btnsLeft = [];
                this.btnsCenter = [];
                this.btnsRight = [];
                this.width = null;
                this.height = null;
                this.uiFullMask.style.display = 'none';
                this.isDisplayed = false;
                if (this.content instanceof dojo.widget.HtmlWidget) {
                    this.content.destroy();
                }
                this.content = null;
                // Cleanup -- wipe DOM inside container
                this.domNode.innerHTML = '';
                
            };
        },
        
        // Toggling visibility
        toggle: 'plain' } );
