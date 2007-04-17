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
dojo.provide("cosmo.model.util");

cosmo.model.util._upperFirstChar = function(str){
    return str.charAt(0).toUpperCase() + str.substr(1,str.length -1 );
}

cosmo.model.util.BasePropertyApplicator = function(){}   
cosmo.model.util.BasePropertyApplicator.prototype = {
    addProperty: function(ctr, propertyName, kwArgs){
        var upProp = cosmo.model.util._upperFirstChar(propertyName);
        var setterName = "set" + upProp;
        var getterName = "get" + upProp;
        
        this.initializeObject(ctr, propertyName, kwArgs);
        ctr.prototype[setterName] = this.getSetter(ctr, propertyName, kwArgs);
        ctr.prototype[getterName] = this.getGetter(ctr, propertyName, kwArgs);
    },
    
    initializeObject: function(ctr, propertyName, kwArgs){
        //implementers should override this.
    },
    
    getSetter: function(ctr, propertyName, kwArgs){
        //implementers should override this.
        return null;
    },

    getGetter: function(ctr, propertyName, kwArgs){
        //implementers should override this.
        return null;
    }
}

cosmo.model.util.simplePropertyApplicator = new cosmo.model.util.BasePropertyApplicator();
cosmo.model.util.simplePropertyApplicator._NULL = {};
cosmo.model.util.simplePropertyApplicator._FALSE_OR_ZERO = {};
cosmo.model.util.simplePropertyApplicator.getGetter = function(ctr, propertyName, kwArgs){
    var self = this;
    return function(){        
        var privateName = "_" + propertyName;
        var result = this[privateName];
        if (result){
            return result == self._NULL ? null : self._FALSE_OR_ZERO ? 0 : result;
        }
        if (typeof(kwArgs["default"]) == "function"){
            this[privateName] = kwArgs["default"]();
            return this[privateName];
        }
        return kwArgs["default"];
    }
}

cosmo.model.util.simplePropertyApplicator.getSetter = function(ctr, propertyName, kwArgs){
    var self = this;
    return function(value){
        var privateName = "_" + propertyName;
        if (value == null){
            this[privateName] = self._NULL;
        } else if (value == 0){
            this[privateName] = self._FALSE_OR_ZERO;
        } else {
            this["_" + propertyName] = value;
        }
    }
}

cosmo.model.util.applyMultiplePropertiesWithPropertyInitializer = function(ctr, properties, propertyApplicator){
    for (var x = 0; x < properties.length; x++){
        var propertyDescriptor = properties[x];
        propertyApplicator.addProperty(ctr,propertyDescriptor["name"]);
    }
}