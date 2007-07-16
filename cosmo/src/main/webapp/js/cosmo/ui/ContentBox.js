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

dojo.provide("cosmo.ui.ContentBox");
dojo.require("cosmo.util.deferred");
dojo.require("dojo.Deferred");
// Generic content container class
// ==============================
cosmo.ui.ContentBox = function (p) {
    var params = p || {};
    this.id = '';
    this.top = 0;
    this.left = 0;
    this.width = 0;
    this.height = 0;
    // If the box contains a simple DOM node
    // this is where it lives
    this.domNode = null;
    // If the box contain a Dojo widget, this is
    // a reference to the widget
    this.widget = null;
    this.parent = null;
    this.children = [];
    this.hasBeenRendered = false;
    for (var i in params) { this[i] = params[i] }
}
cosmo.ui.ContentBox.prototype.cleanup =  function () {
    this.domNode = null;
};
cosmo.ui.ContentBox.prototype.clearAll =  function () {
    while (this.domNode.hasChildNodes()) {
        this.domNode.removeChild(this.domNode.firstChild);
    }
    this.domNode.innerHTML = '';
};
cosmo.ui.ContentBox.prototype.setPosition =  function (left, top) {
    this.setTop(top);
    this.setLeft(left);
};
cosmo.ui.ContentBox.prototype.setSize =  function (width, height) {
    this.setWidth(width);
    this.setHeight(height);
};
cosmo.ui.ContentBox.prototype.setTop =  function (top) {
    if (typeof top != 'undefined') {
        this.top = top;
    }
    n = this.top;
    this.domNode.style.top = parseInt(n) + 'px';
};
cosmo.ui.ContentBox.prototype.setLeft =  function (left) {
    if (typeof left != 'undefined') {
        this.left = left;
    }
    n = this.left;
    this.domNode.style.left = parseInt(n) + 'px';
};
cosmo.ui.ContentBox.prototype.setWidth =  function (width) {
    if (typeof width != 'undefined') {
        this.width = width;
    }
    n = this.width;
    n = n.toString();
    n = n.indexOf('%') > -1 ? n : parseInt(n) + 'px';
    this.domNode.style.width = n;
};
cosmo.ui.ContentBox.prototype.setHeight =  function (height) {
    if (typeof height != 'undefined') {
        this.height = height;
    }
    n = this.height;
    n = n.toString();
    n = n.indexOf('%') > -1 ? n : parseInt(n) + 'px';
    this.domNode.style.height = n;
};
cosmo.ui.ContentBox.prototype.hide =  function () {
    this.domNode.style.display = 'none';
};
cosmo.ui.ContentBox.prototype.show =  function () {
    this.domNode.style.display = 'block';
};
cosmo.ui.ContentBox.prototype.renderSelf =  function () {};
cosmo.ui.ContentBox.prototype.update = function (p) {
    var params = p || {};
    for (var n in params) { this[n] = params[n]; }
};
cosmo.ui.ContentBox.prototype.render = function () {
    var renderDeferred;
    if (typeof this.renderSelf == 'function') {
        renderDeferred = this.renderSelf();
    };
    if (!(renderDeferred instanceof dojo.Deferred)){
        renderDeferred = cosmo.util.deferred.getFiredDeferred(renderDeferred);
    }
    renderDeferred.addCallback(dojo.lang.hitch(this, function () {
        var ch = this.children;
        for (var i = 0; i < ch.length; i++) {
            ch[i].render();
        }
    }));
};
cosmo.ui.ContentBox.prototype.addChild = function (c) {
    this.children.push(c);
    c.parent = this;
    this.domNode.appendChild(c.domNode);
    //c.render();
};



