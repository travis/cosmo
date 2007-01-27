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
    multi, selOptions, className, parentNode) {

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
        appendElem = parentNode;
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
    size, maxlength, value, className, parentNode) {

    var o = {};
    var input = null;
    var str = '';
    
    // Also accept a keyword obj as a param -- options are passed
    // as a keyword prop, appendElem is a second arg after the obj
    // e.g, createInput({ type: 'password', name: 'foo', id: 'foo', 
    //      value: 'asdf', className: 'fooFoo' },  nodeToAppendTo);
    if (typeof arguments[0] == 'object') {
        o = arguments[0];
        appendElem = arguments[1];
    }
    // Normal order-based param invocation
    else {
        o.type = type;
        o.name = name;
        o.id = id;
        if (size) {
            o.size = size;
        }
        if (maxlength) {
            o.maxlength = maxlength;
        }
        if (value) {
            o.value = value;
        }
        if (className) {
            o.className = className;
        }
        appendElem = parentNode;
    }

    // IE falls down on DOM-method-generated
    // radio buttons and checkboxes
    // Old-skool with conditional branching and innerHTML
    if (document.all && (type == 'radio' || type == 'checkbox')) {
        str = '<input type="' + type + '"' +
            ' name="' + o.name + '"' +
            ' id ="' + o.id + '"';
        if (o.size) {
            str += ' size="' + o.size + '"';
        }
        if (o.maxlength) {
            str += ' maxlength="' + o.maxlength + '"';
        }
        if (o.className) {
            str += ' class="' + o.className + '"';
        }
        str += '>';
        var s = document.createElement('span');
        s.innerHTML = str;
        input = s.firstChild;
        s.removeChild(input); 
    }
    // Standards-compliant browsers -- all intputs
    // IE -- everything but radio button and checkbox
    else {
        input = document.createElement('input');
        for (var p in o) {
            input[p] = o[p];
        }
            
    }
    
    if (appendElem) {
        appendElem.appendChild(input);
    }
    return input;
};

cosmo.util.html.appendNbsp = function (parentNode) {
    parentNode.appendChild(document.createTextNode('\u00A0'));
};

cosmo.util.html.nbsp = function () {
    return document.createTextNode('\u00A0');
};

cosmo.util.html.getRadioButtonSetValue = function (set) {
    for (var i = 0; i < set.length; i++) {
        if (set[i].checked) {
            return set[i].value;
        }
    }
    return null;
};

//TODO Should this be a widget, or too granular? Discuss.
cosmo.util.html.createRollOverMouseDownImage = function(normalImageUrl){
    var i = normalImageUrl.lastIndexOf(".");
    var suffix = normalImageUrl.substr(i);
    var prefix = normalImageUrl.substr(0,i);
    var img = document.createElement("img");
    img.src = normalImageUrl;
    dojo.event.connect(img, "onmouseover", function(){
       img.src = prefix + "_rollover" + suffix;
    });
    dojo.event.connect(img, "onmouseout", function(){
       img.src = normalImageUrl;
    });
    dojo.event.connect(img, "onmousedown", function(){
       img.src = prefix + "_mousedown" + suffix;
    });
    dojo.event.connect(img, "onmouseup", function(){
       img.src = prefix + "_rollover" + suffix;
    });
    return img;

};

// Fix bug where safari does not put form inputs into form.elements.
cosmo.util.html.addInputsToForm = function(inputs, form){
	for (i=0; i < inputs.length; i++){
		form[inputs[i].id] = inputs[i];
	}
};

cosmo.util.html.handleTextInputFocus = function (e) {
    var t = e.target;
    if (t.className == 'inputTextDim') {
        t.className = 'inputText';
        t.value = '';
    }
    else {
        t.select();
    }
}

