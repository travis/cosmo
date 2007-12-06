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

dojo.require("dojo.event.*");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.html");

dojo.provide('cosmo.account.common');

cosmo.account.formTypes = {
  CREATE: 'create',
  SETTINGS: 'settings' };

cosmo.account.accountBase = new function () {
    this.handleInputFocusChange = function (e) {
        if (e.target) {
          if (e.type == 'focus' && e.target.type != 'password') {
              this.focusedField = e.target;
          }
          else {
              this.focusedField = null;
          }
        }
    };
};

cosmo.account.getFieldList = function (accountInfo, subscription) {
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
                    cosmo.util.validate.minLength(elem, 3) || 
                    cosmo.util.validate.doesNotMatch(
                        elem, new RegExp(":|\u007F"), "Signup.Error.UsernameIllegalChar") ||
                    cosmo.util.validate.inBMP(elem, "Signup.Error.UsernameBMP") ||
                    cosmo.util.validate.noControl(elem, "Signup.Error.UsernameControl")
                   ); };
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
    // Make sure password is between 5 and 16 chars per CMP spec.
    var validatePasswordLength = function(elem){
        return cosmo.util.validate.minLength(elem, 5) ||
            cosmo.util.validate.maxLength(elem, 16);
    };
    // User editing own account -- blank password means no change
    if (accountInfo) {
        f.validators = validatePasswordLength;
    }
    // Creating a new account -- require password field
    else {
        f.validators = function (elem) {
            return (cosmo.util.validate.required(elem) ||
                    validatePasswordLength(elem));
        };
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

    // If this is a new account creation attempt
    // and Terms of Service are required
    if (!accountInfo && cosmo.ui.conf.getBooleanValue("tosRequired")){
        f = { label: _('Signup.Form.TOS'),
            elemName: 'tos',
            elemType: 'checkbox'
        };
        f.validators = function (elem) {
            return cosmo.util.validate.tosChecked(elem); };
        f.value = a[f.elemName];
        list.push(f);
    }
    
    if (subscription){
        f = {label: _('Signup.Form.Subscription.Name'),
             elemName: 'subscriptionName',
             elemType: 'text'
        };
        f.validators = function (elem) {
            return cosmo.util.validate.required(elem); };
        f.value = subscription.getDisplayName();
        list.push(f);

        f = {label: _('Signup.Form.Subscription.Ticket'),
             elemName: 'subscriptionTicket',
             elemType: 'text',
             disabled: true
              
        };
        f.validators = function (elem) {
            return cosmo.util.validate.required(elem); };
        f.value = subscription.getTicketKey();
        list.push(f);

        f = {label: _('Signup.Form.Subscription.Uuid'),
             elemName: 'subscriptionUuid',
             elemType: 'text',
             disabled: true
        };
        f.validators = function (elem) {
            return cosmo.util.validate.required(elem); };
        f.value = subscription.getUid();
        list.push(f);
    }

    return list;
};

/**
 * Programmatically creates the table of form elements
 * used for signup. Loops through fieldList for all the
 * form fields.
 * @return Object (HtmlFormElement), form to append to the
 *     content area of the modal dialog box.
 */
cosmo.account.getFormTable = function (fieldList, callingContext) {
    var isCreate = (callingContext.formType == cosmo.account.formTypes.CREATE);
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
    
    table.appendChild(body);
    form.appendChild(table);

    var inputs = [];

    // Table row for each form field except TOS link
    for (var i = 0; i < fieldList.length; i++) {
        var f = fieldList[i];
        if (f.elemName == 'tos') {
            form.appendChild(cosmo.account.createTosInput(f));
        } else {
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

            var elem = cosmo.account.fieldToElement(f);
            elem.maxlength = type == 'text' ? 32 : 16;
            elem.size = type == 'text' ? 32 : 16;
            elem.style.width = type == 'text' ? '240px' : '140px';
            elem.className = 'inputText';
            dojo.event.connect(elem, 'onfocus', callingContext, 'handleInputFocusChange');
            dojo.event.connect(elem, 'onblur', callingContext, 'handleInputFocusChange');
            inputs.push(elem);
            td.appendChild(elem);

            tr.appendChild(td);
            body.appendChild(tr);
        }
    }

    // BANDAID: Hack to get the checkbox into Safari's
    // form elements collection
    if (navigator.userAgent.indexOf('Safari') > -1) {
        cosmo.util.html.addInputsToForm(inputs, form);
    }
    return form;
};

cosmo.account.fieldToElement = function (field){
    // Form field cell
    td = _createElem('td');
    td.id = field.elemName + 'ElemCell';
    // Form field
    elem = _createElem('input');
    elem.type = field.elemType;
    elem.name = field.elemName;
    elem.id = field.elemName;
    elem.value = field.value || '';
    elem.disabled = field.disabled || false;
    return elem;
};

cosmo.account.createTosInput = function (tosField){
    var tosDiv = _createElem('div');
    tosDiv.id = "tosElemCell"

    var lab = _createElem('span');
    lab.id = "tosLinkLabel";
    lab.innerHTML = tosField.label;

    var input = cosmo.account.fieldToElement(tosField)

    tosDiv.appendChild(input);
    tosDiv.appendChild(lab);
    return tosDiv;
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

