/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.ui.DetailFormConverter");
dojo.require("cosmo.model.Delta");

dojo.declare("cosmo.ui.DetailFormConverter", null, {
   _item: null,
   initializer: function(item){
       this._item = item;
   },
   
   createDelta: function(){
        var delta = new cosmo.model.Delta(this._item);
        var errorMessage = "";
        
        //get the form values for the "main" section
        dojo.debug("about to populate stamps");
        errorMessage += this._populateDeltaFromMain(delta);
        for (var x = 0; x < cosmo.ui.detail.itemStamps.length; x++){
            var stampName = cosmo.ui.detail.itemStamps[x].stampType;
            dojo.debug("populating stamp: " + stampName);
            var hasFields = cosmo.ui.detail.itemStamps[x].hasBody;
            this._populateDeltaFromStamp(delta, stampName.toLowerCase(),hasFields);
        }
        delta.deltafy();
        return delta;  
    },
    
    getStampForm: function(stampName){
        //summary: returns the form object for the given stamp name
        dojo.debug("getStampForm: " + stampName);
        stampName = stampName.toLowerCase();
        return cosmo.app.pim.baseLayout.mainApp.rightSidebar
                   .detailViewForm[stampName +"Section"].formSection.formNode;
    },
    
    getMainForm: function(){
        return cosmo.app.pim.baseLayout.mainApp.rightSidebar.detailViewForm.mainSection.formNode;
    },
    
    _populateDeltaFromMain: function(delta){
        var map = this._notePropertiesMap;
        var form = this.getMainForm();
        var errors = "";
        for (var fieldName in map){
            var valueAndError = this._validateAndGetValue(map, fieldName,  form);
            var value = valueAndError[0];
            var error = valueAndError[1];
            if (!error){
                delta.addProperty(map[fieldName]["propertyName"], value);
            } else {
                errors += error[1];
            }
        }
        return errors;
    },
    
    _populateDeltaFromStamp: function(delta, stampName, hasFields){
        var map = this._stampPropertiesMaps[stampName];
        var errors = "";
        var enabled = this._isStampEnabled(stampName);
        if (!enabled){
            delta.addDeletedStamp(stampName);
        } else {
            delta.addAddedStamp(stampName);
            if (hasFields){
                var form = this.getStampForm(stampName);
                for (var fieldName in map){
                    var valueAndError = this._validateAndGetValue(map, fieldName,  form);
                    var value = valueAndError[0];
                    var error = valueAndError[1];
                    if (!error){
                        delta.addStampProperty(stampName, map[fieldName]["propertyName"], value);
                    } else {
                        errors += error[1];
                    }
                }
            }
        }
        return errors;
        
    },
    
    _isStampEnabled: function(stampName){
        var checkBox = $("section"+ this._upperFirstChar(stampName) +"EnableToggle");
        return checkBox.checked;
    },
    
    _validateAndGetValue: function(map, fieldName, form){
        var record = map[fieldName];
        var formValue = this._getFormValue(form, fieldName, record.fieldType);
        var convertedValueAndError = this._convertValue(map, formValue, fieldName);
        return convertedValueAndError;
    },
    
    _convertValue: function(map, formValue, fieldName){
        return [formValue,null];    
    },
    
    _getFormValue:  function(form, fieldName, fieldType){
        fieldType = fieldType || "text";
        switch(fieldType){
            case "text":
                return form[fieldName].value;
                break;
            default: 
                return "";
                break;
        }
    },
    
    _upperFirstChar: function(str){
        return str.charAt(0).toUpperCase() + str.substr(1,str.length -1 );
    },
    
    _notePropertiesMap: {
        noteDescription: {propertyName: "body"},
        noteTitle: {propertyName: "displayName"}
    },
    
    _stampPropertiesMaps: {
        event: {
            
        },
        
        mail: {
        },
        
        task: {
        }
    }
});