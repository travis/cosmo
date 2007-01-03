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

dojo.provide("cosmo.util.html");

cosmo.util.html.createSelect = function (id, name, size, 
    multi, selOptions, className, elem) {

    var sel = document.createElement('select');
    var o = {};
    var options = [];
    var appendElem = null;
    
    // Also accept a keyword obj as a param -- options are passed
    // as a keyword prop, appendElem is a second arg after the obj
    // e.g, createSelect({ name: 'foo', id: 'foo', multi: true,
    //    options: [{ text: 'Howdy', value: '123' }, 
    //    { text: 'Hey', value: 'ABC' }], className: 'fooFoo' }, 
    //    nodeToAppendTo);
    if (typeof arguments[0] == 'object') {
        o = arguments[0];
        appendElem = arguments[1];
        options = o.options;
        for (var p in o) {
            if (p != 'options') {
                sel[p] = o[p];
            }
        }
    }
    // Normal order-based param invocation
    else {
        options = selOptions;
        appendElem = elem;
        sel.id = id
        sel.name = name;
        if (size) {
            sel.size = size;
        }
        if (multi) {
            sel.multiple = 'multiple';
        }
        if (className) {
            sel.className = className;
        }
    }
    // Add the options for the select
    cosmo.util.html.setSelectOptions(sel, options);
    
    // Append the select if passed somewhere to put it
    if (appendElem) {
        appendElem.appendChild(sel);
    }
    return sel;
};
    
cosmo.util.html.setSelectOptions = function (selectElement, options){
    while (selectElement.firstChild){
       selectElement.removeChild(selectElement.firstChild);
    }
    for (var i = 0; i < options.length; i++) {
        var opt = document.createElement('option');
        opt.value = options[i].value;
        opt.appendChild(document.createTextNode(options[i].text));
        selectElement.appendChild(opt);
    }
};

cosmo.util.html.setSelect = function (sel, val) {
    for (var i = 0; i < sel.options.length; i++) {
        if (sel.options[i].value == val) {
            sel.selectedIndex = i;
        }
    }
};

cosmo.util.html.createInput = function (type, id, name,
    size, maxlength, value, className, elem) {

    var formElem = null;
    var str = '';

    // IE falls down on DOM-method-generated
    // radio buttons and checkboxes
    // Old-skool with conditional branching and innerHTML
    if (document.all && (type == 'radio' || type == 'checkbox')) {
        str = '<input type="' + type + '"' +
            ' name="' + name + '"' +
            ' id ="' + id + '"';
        if (size) {
            str += ' size="' + size + '"';
        }
        if (maxlength) {
            str += ' maxlength="' + maxlength + '"';
        }
        if (className) {
            str += ' class="' + className + '"';
        }
        str += '>';
        if (elem) {
            elem.innerHTML += str;
        }
        else {
            formElem = document.createElement('span');
            formElem.innerHTML = str;
            return formElem.firstChild;
        }
    }
    // Standards-compliant browsers -- all intputs
    // IE -- everything but radio button and checkbox
    else {
        formElem = document.createElement('input');
        
        formElem.type = type;
        formElem.name = name;
        formElem.id = id;
        if (size) {
            formElem.size = size;
        }
        if (maxlength) {
            //formElem.maxlength = maxlength; // Setting the prop directly is broken in FF
            formElem.setAttribute('maxlength', maxlength);
        }
        if (value) {
            formElem.value = value;
        }
        if (className) {
            formElem.className = className;
        }
        if (elem) {
            elem.appendChild(formElem);
        }
        return formElem;
    }
};

cosmo.util.html.appendNbsp = function (elem) {
    elem.appendChild(document.createTextNode('\u00A0'));
};

