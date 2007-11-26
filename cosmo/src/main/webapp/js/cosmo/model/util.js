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

cosmo.model.util.getGetterAndSetterName = function(propertyName){
        var upProp = cosmo.model.util._upperFirstChar(propertyName);
        var setterName = "set" + upProp;
        var getterName = "get" + upProp;
        return [getterName, setterName];
}

cosmo.model.util.BasePropertyApplicator = function(){}   
cosmo.model.util.BasePropertyApplicator.prototype = {
    
    enhanceClass: function(ctr, propertyArray, kwArgs){
        this.initializeClass(ctr, kwArgs);
        for (var x = 0; x < propertyArray.length; x++){
            this.addProperty.apply(this, [ctr, propertyArray[x][0], propertyArray[x][1]]);
        }
    },
    
    addProperty: function(ctr, propertyName, kwArgs){
        kwArgs = kwArgs || {};
        var getterAndSetter = cosmo.model.util.getGetterAndSetterName(propertyName);
        var setterName = getterAndSetter[1];
        var getterName = getterAndSetter[0];
        
        if (!ctr.prototype[setterName]){
            ctr.prototype[setterName] = this.getSetter(ctr, propertyName, kwArgs);
        }
        
        if (!ctr.prototype[getterName]){
            ctr.prototype[getterName] = this.getGetter(ctr, propertyName, kwArgs);
        }
    },
    
    initializeClass: function(ctr, kwArgs){
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

dojo.declare("cosmo.model.util.SimplePropertyApplicator", cosmo.model.util.BasePropertyApplicator, {
    addProperty: function(ctr, propertyName, kwArgs){
        kwArgs = kwArgs || {};
        this._inherited("addProperty", arguments);
        ctr.prototype.__propertyNames.push(propertyName);
        ctr.prototype.__defaults[propertyName] = kwArgs["default"];
    },
    
    getGetter: function(ctr, propertyName, kwArgs){
        return function(){        
            return this.__getProperty(propertyName);
        }
    },

    getSetter: function(ctr, propertyName, kwArgs){
        return function(value){
            this.__setProperty(propertyName, value);
        }
    },

    initializeClass: function(ctr, kwArgs){
               if (ctr.prototype["__enhanced"]){
            //it's already been enhanced, which usually means that this is a subclass.
            //so let's just copy the arrays/objects to the new prototype 
            //since we'll be adding to them and don't want to add to the parent's arrays/objects!
            ctr.prototype.__propertyNames = ctr.prototype.__propertyNames.slice();
            var newDefaults = {};
            dojo.lang.mixin(newDefaults,ctr.prototype.__defaults);
            ctr.prototype.__defaults = newDefaults;
            return;
        }
        if (!kwArgs){
            kwArgs = {};
        }
        ctr.prototype.__enhanced = true;
        ctr.prototype.__getProperty = this._genericGetter;
        ctr.prototype.__setProperty = this._genericSetter;
        ctr.prototype.__getDefault = this._getDefault;
        ctr.prototype.__propertyNames = [];
        ctr.prototype.__defaults = {};
        ctr.prototype.initializeProperties = this._initializeProperties;
        ctr.prototype.__immutable = kwArgs["immutable"];
        ctr.prototype.toString = this._genericToString;
        ctr.prototype.equals = this._genericEquals;
        ctr.prototype.clone = this._genericClone;
        ctr.prototype.isChanged = this._genericIsChanged;
        
        if (kwArgs["enhanceInitializer"]){
            var oldInitter = ctr.prototype.initializer;
            var when = kwArgs["enhanceInitializer"];
            //TODO use dojo AOP
            function newInitializer(){
                if (when == "before"){
                    this.initializeProperties(arguments[0]);
                }
                
                oldInitter.apply(this,arguments);
                
                if (when != "before"){
                    this.initializeProperties(arguments[0]);
                }
            }
            ctr.prototype.initializer = newInitializer;
        }    
    },
    
    //These functions are "protected" - in other words they should only be used by this class, 
    //or other classes in this package.
    _initializeProperties: function(kwProps, kwArgs){
        kwArgs = kwArgs || {};

        //if there are properties to set, set them.
        if (kwProps){
            for (var x = 0; x < this.__propertyNames.length; x++){
                var propertyName = this.__propertyNames[x];
                if (dojo.lang.has(kwProps, propertyName)){
                    this.__setProperty(propertyName, kwProps[propertyName]);
                } else if (!kwArgs.noDefaults){
                    this.__setProperty(propertyName, this.__getDefault(propertyName));
                }
            }
        } else if (!kwArgs.noDefaults) {
            //if there are not properties to set, and there are defaults, go through
            //and set the defaults
            for (x = 0; x < this.__propertyNames.length; x++){
                var propertyName = this.__propertyNames[x];
                this.__setProperty(propertyName, this.__getDefault(propertyName));
            }
        }

        if (this.__immutable){
            this.__setProperty = function(){
                throw new Error (this.declaredClass + " is an immutable type! No changes are allowed.")
            }
        }
    }, 
    
    _genericGetter: function (propertyName){
        return this["_"+propertyName];
    },
    
    _genericSetter: function (propertyName, value){
        this["_"+propertyName] = value;
    },
    
    _getDefault: function (propertyName){
        var propDefault = this.__defaults[propertyName];
                    
        if (typeof(propDefault) == "function"){
            return propDefault();
        } else {
            return propDefault;
        }
        
        return propDefault;
    },
    
    _genericToString: function (){
        var s = "{";
        for (var x = 0; x < this.__propertyNames.length; x++){
            var propName  = this.__propertyNames[x];
            s += "\n    " + propName + ": " +this.__getProperty(propName);
        }
        s += "\n}";
        return s;
    },
    
    _genericEquals: function (that){
        if (that == null){
            return false;
        }

        for (var x = 0; x < this.__propertyNames.length; x++){
            var propName  = this.__propertyNames[x];
            var thisProp = this.__getProperty(propName);
            var thatProp = that.__getProperty(propName);
            if (!cosmo.model.util.equals(thisProp, thatProp)){
                return false;
            }
        }
        return true;
    },
    
    _genericClone: function (){
        var clone = new this.constructor();
        for (var x = 0; x < this.__propertyNames.length; x++){
            var propName  = "_" + this.__propertyNames[x];
            clone[propName] = cosmo.model.clone(this[propName]);
        }
        return clone;
    },
          
    _genericIsChanged: function(propertyName, changedProperty, looseStringComparisons){
        var getterName = cosmo.model.util.getGetterAndSetterName(propertyName)[0];
        var origProperty = this[getterName]();
        return !cosmo.model.util.equals(origProperty, changedProperty, looseStringComparisons);
    }
    
});
//instantiate the singleton 
cosmo.model.util.simplePropertyApplicator = new cosmo.model.util.SimplePropertyApplicator();

cosmo.model.util.equals = function (a, b, looseStringComparisons){
    //summary: generic equals method which takes primitives and objects as operands
    //description: compares primimives using "==", and makes sure they are of the same type, or
    //             an error is thrown.
    //params a,b: things to be compared
    //param looseStringComparisons: if true the empty string, null and undefined 
    //                              are treated as equal

    looseStringComparisons = !!looseStringComparisons;
    
    var type = typeof (a);
    
    if (looseStringComparisons){
        if (a === "" || a == undefined || a == null){
            return b === "" || b == null || b == undefined;
        }
        if (b === "" || b == undefined || b == null){
            return a === "" || a == null || a == undefined;
        }
    }
    
    if (type == "undefined" && typeof b != "undefined"){
        return false;
    }
    
    if (typeof b == "undefined"){
        return false;
    }
    
    if (a == null){
        return b == null;
    }
    
    if (b == null){
        return false;
    }
 
    if (type != typeof(b)){
        throw new Error("Both operands must be of the same type!\n You passed '" 
           + a + "' and '" + b +"', a " + typeof(a) + " and a "+ typeof(b));
    }
    
    if (type == "object"){
       return a.equals(b);
    }
	
	// Work around IE sticking in Windows line breaks in for regular ones.
	if (dojo.render.html.ie && type == "string"){
	    var lineBreakRegex = /\r\n/g;
	    var replacement = "\n";
        a = a.replace(lineBreakRegex, replacement);
        b = b.replace(lineBreakRegex, replacement);
	}  
    
    return a == b;
}   

  cosmo.model._occurrenceGetProperty = function (propertyName){
        //get the master version
        var master = this.getMaster();
        var masterProperty = this._getMasterProperty(propertyName);

        //see if it's overridable 
        //if it's not, just go right to the master
        if (this.__noOverride[propertyName]){
            return masterProperty;
        }
        
        //if it is check the modificaiton
        var modification = master.getModification(this.recurrenceId);

        //if no modification, return the master
        if (!modification){
            return masterProperty;
        }
            
        //there IS a modification, so let's check to see if it has 
        //an overridden value for this particular property
        var modificationProperty = this._getModifiedProperty(propertyName);
        if (typeof(modificationProperty) != "undefined"){
            return modificationProperty;            
        }
        return masterProperty;
}

cosmo.model._occurrenceSetProperty = function (propertyName, value){
    if (this.__noOverride[propertyName]){
        throw new Error("You can not override property '" + propertyName +"'");
    }

    var master = this.getMaster();

    var masterProperty = this._getMasterProperty(propertyName);

    //is there a modification?
    var modification = master.getModification(this.recurrenceId);
    if (modification){
        //has this particular property been set already on the modification?
        if (!typeof(this._getModifiedProperty(propertyName)) == "undefined"){
            this._setModifiedProperty(propertyName, value);
            return;                    
        } else {
            this._setModifiedProperty(propertyName, value);
            return;
        }
    } 

    //if the new value is the same as the master property, 
    // no need to do anything
    if (cosmo.model.util.equals(value, masterProperty)){
        return;
    } else {
        this._setModifiedProperty(propertyName, value, true);
    }
}

cosmo.model.clone = function (thing){
    var type = typeof thing;

    if  (type == "undefined" ||
         type == "number" ||
         type == "string" ||
         type == "boolean" ||
         thing == null){
        return thing;
    }

    if (dojo.lang.isArray(thing)){
        var clone = [];
        for (var x = 0; x < thing.length; x++){
            clone[x] = cosmo.model.clone(thing[x]);
        }
        return thing;
    }
    
   if (dojo.lang.isObject(thing)){
        if (thing.clone){
            return thing.clone();
        }
        
        var clone = {};
        for (var propName in thing){
            clone[propName] = cosmo.model.clone(thing[propName]);
        }
        
        return clone;
   }
   throw new Error("unclonable!?")
}