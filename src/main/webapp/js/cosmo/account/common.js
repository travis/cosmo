/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

dojo.require("dojo.html.common");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.convenience");

dojo.provide('cosmo.account.common');

cosmo.account.getFieldList = function (accountInfo) {
    var list = [];
    var f = {};
    var a = accountInfo || {}; // If account info passed, set input values
    
    // Don't include username when editing account settings
    if (!accountInfo) {
        f = { label:_('Signup.Form.Username'), 
            elemName: 'username',
            elemType: 'text'
        };
        f.validators = function (elem) { 
            return (cosmo.util.validate.required(elem) || 
            cosmo.util.validate.minLength(elem, 3)); };
        f.value = a[f.elemName];
        list.push(f);
    }
    
    f = { label: _('Signup.Form.FirstName'),
        elemName: 'firstName',
        elemType: 'text'
    };
    f.validators = function (elem) { 
        return cosmo.util.validate.required(elem); };
    f.value = a[f.elemName];
    list.push(f);
    
    f = { label: _('Signup.Form.LastName'), 
        elemName: 'lastName',
        elemType: 'text'
    };
    f.validators = function (elem) { 
        return cosmo.util.validate.required(elem); };
    f.value = a[f.elemName];
    list.push(f);
    
    f = { label: _('Signup.Form.EMailAddress'), 
        elemName: 'email',
        elemType: 'text'
    };
    f.validators = function (elem) { 
        return (cosmo.util.validate.required(elem) || 
        cosmo.util.validate.eMail(elem)); };
    f.value = a[f.elemName];
    list.push(f);
    
    f = { label: _('Signup.Form.Password'), 
        elemName: 'password', 
        elemType: 'password'
    };
    // User editing own account -- blank password means no change
    if (accountInfo) {
        f.validators = function (elem) { 
            return cosmo.util.validate.minLength(elem, 5); };

    }
    // Creating a new account -- require password field 
    else {
        f.validators = function (elem) { 
            return (cosmo.util.validate.required(elem) || 
            cosmo.util.validate.minLength(elem, 5)); };
    }
    f.value = a[f.elemName];
    list.push(f);
    
    f = { label: _('Signup.Form.ConfirmPassword'), 
        elemName: 'confirm', 
        elemType: 'password' };
    // User editing own account -- blank password means no change
    if (accountInfo) {
        f.validators = function (elem1, elem2) { 
            return cosmo.util.validate.confirmPass(elem1, elem2); };
    }
    // Creating a new account -- require password field 
    else {
        f.validators = function (elem1, elem2) { 
            return (cosmo.util.validate.required(elem1) || 
            cosmo.util.validate.confirmPass(elem1, elem2)); };
    }
    f.value = a[f.elemName];
    list.push(f);
    
    return list;
};

/**
 * Programmatically creates the table of form elements
 * used for signup. Loops through fieldList for all the
 * form fields.
 * @return Object (HtmlFormElement), form to append to the 
 *     content area of the modal dialog box.
 */
cosmo.account.getFormTable = function (fieldList, isCreate) {
    var table = null;
    var body = null;
    var tr = null;
    var td = null;
    var elem = null;
    var form = _createElem('form');
    
    form.id = 'accountSignupForm';
    form.onsubmit = function () { return false; };
    
    table = _createElem('table');
    body = _createElem('tbody');
   
    if (isCreate) {
        table.style.width = '60%';
    }
    else {
        table.style.width = '100%';
    }
    table.style.margin = 'auto';
    
    // Table row for each form field
    for (var i = 0; i < fieldList.length; i++) {
        var f = fieldList[i];
        var type = f.elemType;
        
        // Create row
        tr = _createElem('tr');
        
        // Label cell
        td = _createElem('td');
        td.id = f.elemName + 'LabelCell';
        td.className = 'labelTextHoriz labelTextCell';
        // Label
        td.appendChild(_createText(f.label + ':'));
        td.style.width = '1%';
        tr.appendChild(td);
        
        // Form field cell
        td = _createElem('td');
        td.id = f.elemName + 'ElemCell';
        // Form field
        elem = _createElem('input');
        elem.type = f.elemType;
        elem.name = f.elemName;
        elem.id = f.elemName;
        elem.maxlength = type == 'text' ? 32 : 16;
        elem.size = type == 'text' ? 32 : 16;
        elem.style.width = type == 'text' ? '240px' : '140px';
        elem.className = 'inputText';
        elem.value = f.value || '';
        td.appendChild(elem);
        
        tr.appendChild(td);
        body.appendChild(tr)
    }
    table.appendChild(body);
    form.appendChild(table);
    return form;
};

/**
 * Validates the input from all the form fields -- calls the
 * associated validator prop (an or-chain of functions) for
 * each item, and appends errors per-element. Returns a single
 * error msg if any inputs yields an error.
 * @return String, global error message for form (empty
 *     if no element yielded an error).
 */
cosmo.account.validateForm = function (form, fieldList) {
    var err = '';
    var errRet = '';
    for (var i = 0; i < fieldList.length; i++) {
        var field = fieldList[i];
        cell = document.getElementById(field.elemName + 
            'ElemCell');  
        err = field.validators(form[field.elemName], form['password'], false);
        // Remove any previous err msg div
        child = cell.firstChild;
        if (child.nodeName.toLowerCase() == 'div' && child.className != 'floatLeft') {
            cell.removeChild(child);
        }
        child = cell.firstChild;
        // At least one err msg string returned from chain of methods
        if (err) {
            // Set master err msg for return
            errRet = _('Signup.Error.Main'); 
            // Insert err msg div before text input
            div = _createElem('div');
            div.className = 'inputError';
            div.appendChild(_createText(err));
            cell.insertBefore(div, child);
        }
    }
    return errRet;
};

