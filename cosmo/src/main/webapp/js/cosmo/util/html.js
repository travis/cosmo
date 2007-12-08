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
    if (options) {
        cosmo.util.html.setSelectOptions(sel, options);
    }

    // Append the select if passed somewhere to put it
    if (appendElem) {
        appendElem.appendChild(sel);
    }
    return sel;
};

cosmo.util.html.getChildrenByTagName = function (node, tagName){
    return dojo.lang.filter(node.getElementsByTagName(tagName),
        function(filterTarget){
            return (filterTarget.nodeName == tagName) && (filterTarget.parentNode == node);
        });
};

cosmo.util.html.setSelectOptions = function (selectElement, options){
    while (selectElement.firstChild){
       selectElement.removeChild(selectElement.firstChild);
    }

    for (var i = 0; i < options.length; i++) {
        var opt = document.createElement('option');
        opt.value = options[i].value || "";
        opt.appendChild(document.createTextNode(options[i].text));
        selectElement.appendChild(opt);
        if (options[i].selected){
            selectElement.selectedIndex = i;
        }
    }

};

cosmo.util.html.setSelect = function (sel, val) {
    var index = 0;
        for (var i = 0; i < sel.options.length; i++) {
            if (sel.options[i].value == val) {
                index = i;
                break;
            }
        }

    while (sel.selectedIndex != index){
        sel.selectedIndex = index;
        try {
            sel.options.item(index).selected = true;
        } catch (e) {

        }
    }
};

cosmo.util.html.getSelectValue = function (selectElement){
    var index = selectElement.selectedIndex;
    return selectElement.options.item(index).value;
}

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

    // Neither IE nor Safari can handle DOM-generated radio
    // or checkbox inputs -- they stupidly assume name and id
    // attributes should be identical. The solution: kick it
    // old-skool with conditional branching and innerHTML
    if ((document.all || navigator.userAgent.indexOf('Safari') > -1) &&
        (o.type == 'radio' || o.type == 'checkbox')) {
        str = '<input type="' + o.type + '"' +
            ' name="' + o.name + '"' +
            ' id ="' + o.id + '"';
        if (o.value) {
            str += ' value="' + o.value + '"';
        }
        if (o.size) {
            str += ' size="' + o.size + '"';
        }
        if (o.maxlength) {
            str += ' maxlength="' + o.maxlength + '"';
        }
        if (o.checked) {
            str += ' checked="checked"';
        }
        if (o.className) {
            str += ' class="' + o.className + '"';
        }
        str += '/>';

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
        if (typeof t.select == 'function') {
            t.select();
        }
    }
};

/*
 * Wrapper to hide browser differences in potentially namespaced elements.
 *
 * Does not really support namespaces in a meaningful way due to the lack of
 * Element.lookupNamespaceURI method on Safari.
 */
cosmo.util.html.getElementsByTagName = function(parent, namespace, tagName, kwArgs){
    if (!tagName) {
        tagName = namespace;
        namespace = null;
    }

    if (dojo.render.html.ie){
        tagName = namespace? namespace + ":" + tagName : tagName;
        return parent.getElementsByTagName(tagName);
    }
    else {
        namespace = "*";
        return parent.getElementsByTagNameNS(namespace, tagName);
    }

};

/**
 * Sets the text in a text input, may add grayed-out CSS class,
 * or disabled, based on passed params
 */
cosmo.util.html.setTextInput = function (textbox, textValue, isDefaultText,
    disabled) {
    textbox.className = isDefaultText ? 'inputTextDim' : 'inputText';
    textbox.value = textValue;
    if (typeof disabled == 'boolean') {
        textbox.disabled = disabled;
    }
};

cosmo.util.html.clearFormElem = function (elem, elemType) {
    switch (elemType) {
        // Note: not doing anything with
        // hidden form elements here
        case 'text':
        case 'password':
        case 'textarea':
            elem.value = '';
            break;

        case 'select':
        case 'select-one':
        case 'select-multiple':
            elem.selectedIndex = 0;
            break;

        case 'radio':
        case 'checkbox':
            if (elem.length) {
                for (var i = 0; i < elem.length; i++) {
                    elem[i].checked = false;
                }
            }
            else {
                elem.checked = false;
            }
            break;
    }
};

cosmo.util.html.enableDisableFormElem = function (elem, elemType,
    enabled) {
    var disabled = !enabled;
    if (elemType == 'radio' || elemType == 'checkbox') {
        if (elem.length) {
            for (var i = 0; i < elem.length; i++) {
                elem[i].disabled = disabled;
            }
        }
        else {
            elem.disabled = disabled;
        }
    }
    else {
        elem.disabled = disabled;
    }
}

cosmo.util.html.enableFormElem = function (elem, elemType) {
    cosmo.util.html.enableDisableFormElem(elem, elemType, true);
};

cosmo.util.html.disableFormElem = function (elem, elemType) {
    cosmo.util.html.enableDisableFormElem(elem, elemType, false);
};

cosmo.util.html.clearAndDisableFormElem = function (elem, elemType) {
    cosmo.util.html.clearFormElem(elem, elemType);
    cosmo.util.html.disableFormElem(elem, elemType);
};

cosmo.util.html.getFormElemNames = function (form) {
    var elems = form.elements;
    var names = {};
    for (var i = 0; i < elems.length; i++) {
        var elem = elems[i];
        var n = elem.name;
        if (typeof names[n] == 'undefined') {
            names[n] = elem.type;
        }
    }
    return names;
};

cosmo.util.html.getElementTextContent = function (element){
    var content = element.innerText || element.textContent;
    if (!content){
        content = "";
        for (var i = 0; i < element.childNodes.length; i++){
            content += element.childNodes[i].nodeValue;
        }
    }
    return content;
}

cosmo.util.html.getFormValue =  function(form, fieldName){
        //summary: given a form and a field name returns the value of that field.
        //         note: doesn't work with multi-selects (yet).
        //         Checkboxes return the string "1" for checked and the string "0" for unchecked.
        var element = form[fieldName];
        var type = null;
        if (element.type){
            type = element.type
        } else if (element.length){
            type = element[0].type;
        }
        switch(type){
            case "text":
                return element.value;
                break;
            case "textarea":
                return element.value;
                break;
            case "radio":
                return cosmo.util.html.getRadioButtonSetValue(element);
                break;
            case "select-one":
                return cosmo.util.html.getSelectValue(element);
                break;
            case "checkbox":
                return element.checked ? "1" : "0";
                break;
            default:
                alert(type);
                return "";
                break;
        }
};

cosmo.util.html.setOpacity =  function(node, value) {
    if (typeof value != 'number') {
        throw new Error('Opacity value must be a number.'); }
    node.style.opacity = value;
    if (document.all) {
        node.style.filter = 'alpha(opacity=' +
            (value * 100) + ')';
    }
};

