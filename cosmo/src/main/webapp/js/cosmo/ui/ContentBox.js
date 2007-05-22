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

// Generic content container class
// ==============================
cosmo.ui.ContentBox = function (p) {
    var params = p || {};
    this.id = '';
    this.top = 0;
    this.left = 0;
    this.width = 0;
    this.height = 0;
    this.domNode = null;
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
        n = top;
    }
    else {
        n = this.top;
    }
    this.domNode.style.top = parseInt(n) + 'px';
};
cosmo.ui.ContentBox.prototype.setLeft =  function (left) {
    if (typeof left != 'undefined') {
        n = left;
    }
    else {
        n = this.left;
    }
    this.domNode.style.left = parseInt(n) + 'px';
};
cosmo.ui.ContentBox.prototype.setWidth =  function (width) {
    if (typeof width != 'undefined') {
        n = width;
    }
    else {
        n = this.width;
    }
    n = n.toString();
    n = n.indexOf('%') > -1 ? n : parseInt(n) + 'px';
    this.domNode.style.width = n;
};
cosmo.ui.ContentBox.prototype.setHeight =  function (height) {
    if (typeof height != 'undefined') {
        n = height;
    }
    else {
        n = this.height;
    }
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
    if (typeof this.renderSelf == 'function') {
        this.renderSelf();
    };
    var ch = this.children;
    for (var i = 0; i < ch.length; i++) {
        ch[i].render();
    }
};
cosmo.ui.ContentBox.prototype.addChild = function (c) {
    this.children.push(c);
    c.parent = this;
    this.domNode.appendChild(c.domNode);
    //c.render();
};



