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
dojo.require("cosmo.ui.widget.ButtonPanel");
dojo.require("cosmo.ui.widget.Button");

dojo.widget.defineWidget("cosmo.ui.widget.ModalDialog", 
dojo.widget.HtmlWidget, {
        // Template stuff
        templatePath:dojo.uri.dojoUri(
            '../../cosmo/ui/widget/templates/ModalDialog/ModalDialog.html'),
        
        // Attach points
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
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.width = s;
        },
        setHeight: function (n) {
            var s = n.toString();
            s = s.indexOf('%') > -1 ? s : parseInt(s) + 'px';
            this.domNode.style.height = s; 
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
            this.contentNode.style.height = (this.domNode.offsetHeight - spacer) + 'px';
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
        _removeChildren: function(node){
            while(node.firstChild) {
                node.removeChild(node.firstChild);
            }
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
                // In IE6 have to use special alpha filter thingie
                if (document.all && (navigator.appVersion.indexOf('MSIE 6') > -1)) {
                    m.style.filter = 'alpha(opacity=60)';
                }
                else {
                    m.style.opacity = 0.6;
                }
                this.uiFullMask = m;
                document.body.appendChild(this.uiFullMask);
            }
            this.uiFullMask.style.display = 'block';
            return true;
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
                this.setWidth(this.width);
                this.setHeight(this.height);
                
                var waitForIt = this.render() && this.center();
                this.renderUiMask();
                this.domNode.style.display = 'block';
                this.domNode.style.zIndex = 2000;

                // Have to measure for content area height once div is actually on the page
                this.setContentAreaHeight();
                
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
                this.content = null;
                this.btnsLeft = [];
                this.btnsCenter = [];
                this.btnsRight = [];
                this.width = null;
                this.height = null;
                this.uiFullMask.style.display = 'none';
                this.domNode.style.display = 'none';
                this.isDisplayed = false;
                if (this.content instanceof dojo.widget.HtmlWidget){
                    this.content.destroy();
                }
                
            };
        },
        
        // Toggling visibility
        toggle: 'plain' } );
