/*
 * Copyright 2008 Open Source Applications Foundation
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

dojo.provide("cosmo.ui.Error");

//summary: an error
// param property: the name of the property with the error.
// param errorKey: the key to use when looking up the error in the i18n file
// param errorMessage: the localized error message
// param params: any parameters to use for variable replacement in i18n strings
//
// note that "errorKey" and "errorMessage" are mutually exclusive - but having at least
// one of them is required. All other properties are optional.
dojo.declare("cosmo.ui.Error", null, {
    constructor: function(property, errorKey, errorMessage, params) {
        this.property = property;
        this.errorKey = errorKey;
        this.errorMessage = errorMessage;
        this.params = params;
    },

    //summary: this exists so that we can use the error objects as keys
    //         in a hash
    toString: function(){
        var s = this.property + ";" + this.errorKey + ";"+this.errorMessage+";";
        dojo.map(params, function(param){s += param + ";"; });
        return s;
    }
});

dojo.declare("cosmo.ui.ErrorList", null, {
    _errorsList: null,
    _errorsMap: null,

    constructor: function(){
        this._errorsList  = [];
        this._errorsMap = {};
    },

    //summary: adds the given error object to the list
    //param: error: a cosmo.ui.Error
    addError: function(error){
        if (!error) {
            return;
        }
        if (this._errorsMap[error]){
            return;
        }

        this._errorsMap[error] = true;
        this._errorsList.push(error);
    },

    addErrors: function(/*Array*/ errors){
        var self = this;
        dojo.map(errors, function(error){ self.addError(error);});
    },

    isEmpty: function(){
        return this._errorsList.length == 0;
    },

    toString: function() {
        var s = "";
        var self = this;
         dojo.map(this._errorsList, function(error) {
            var property = error.property
                ? self._getPropertyDisplayName(error.property)
                : null;
            var errorMessage = error.errorMessage
                || _.apply(null, cosmo.util.lang.unnest(error.errorKey, error.params));
            var message = property
                 ?  ("'" + property + "': "
                     + " " + errorMessage)
                 : errorMessage;

            s += message + "<br>";
        });
        return s;
    },

    _getPropertyDisplayName: function(propertyName){
        return _("Main.DetailForm." + propertyName);
    }


})
