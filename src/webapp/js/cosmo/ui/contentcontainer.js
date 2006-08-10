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

// Generic content container class
// ==============================
ContentContainer = function(id) {
    this.id = id;
    this.elem = document.getElementById(this.id);
    this.style = this.elem.style;

    this.cleanup =  function() {
        this.elem = null; // Mark DOM elem reference for GC
    };
    this.clearAll =  function() {
        while (this.elem.hasChildNodes()) {
            this.elem.removeChild(this.elem.firstChild);
        }
        this.elem.innerHTML = '';
    };
    this.setPosition =  function(top, left) {
        this.setTop(top);
        this.setLeft(left);
    };
    this.setSize =  function(width, height) {
        this.setWidth(width);
        this.setHeight(height);
    };
    this.setTop =  function(top) {
        this.style.top = parseInt(top) + 'px';
    };
    this.setLeft =  function(left) {
        this.style.left = parseInt(left) + 'px';
    };
    this.setWidth =  function(width) {
        var w = width.toString();
        var w = w.indexOf('%') > -1 ? w : parseInt(w) + 'px';
        this.style.width = w; 
    };
    this.setHeight =  function(height) {
        var h = height.toString();
        h = h.indexOf('%') > -1 ? h : parseInt(h) + 'px';
        this.style.height = h; 
    };
    this.hide =  function() {
        this.style.display = 'none';
    };
    this.show =  function() {
        this.style.display = 'block';
    };
    this.appendInputText =  function(name, id, size, maxlength, disabled, value) {
        elem = document.getElementById(this.id);
        elemInput = document.createElement('input');
        elemInput.name = name;
        elemInput.id = id;
        elemInput.size = size;
        elemInput.maxLength = maxlength;
        elemInput.disabled = disabled;
        elemInput.value = value;
        elemInput.className = 'inputText';
        elem.appendChild(elemInput);
    };
}

