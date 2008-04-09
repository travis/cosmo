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

// summmary: this module contains most of the Cosmo model meta-programming magic
// description: the functions and classes here enable the declaration of Cosmo's
//              model classes, but none are declared themselves here.

cosmo.model.util._upperFirstChar = function(str){
    //summary: given a string, return a string that is similar but
    //         has its first character upper-cased
    return str.charAt(0).toUpperCase() + str.substr(1,str.length -1 );
}

cosmo.model.util.getGetterAndSetterName = function(propertyName){
    // summary: returns the getter and setter name for a given property name
    //          using standard JavaBean conventions
    // description: returns a two element array, the getter name is in the
    //              first slot, and the setter is in the second
    var upProp = cosmo.model.util._upperFirstChar(propertyName);
    var setterName = "set" + upProp;
    var getterName = "get" + upProp;
    return [getterName, setterName];
}

cosmo.model.util.BasePropertyApplicator = function(){}   
cosmo.model.util.BasePropertyApplicator.prototype = {
    // summmary: the abstract base for PropertyApplicator. 
    // description: The property applicator takes a constructor and adds getter
    //              and setter methods to it's class.

    enhanceClass: function(ctr, propertyArray, kwArgs){
        // summary: This is function that adds the properties to the class and
        //          performs any necessary initialization via initializeClass
        //    ctr:  
        //        this is the constructor of the class to be enhanced
        //
        //    propertyArray:
        //        an array of property arguments to pass to add property. The
        //        first element should be the property name, the second element
        //        is kwArgs for that property.See the implementing "addProperty"
        //        for kwArgs details
        //
        //    kwArgs:
        //        keyword arguments to pass to initializeClass() - see that method
        //        for more details
        this.initializeClass(ctr, kwArgs);
        for (var x = 0; x < propertyArray.length; x++){
            this.addProperty.apply(this, [ctr, propertyArray[x][0], propertyArray[x][1]]);
        }
    },
    
    addProperty: function(ctr, propertyName, kwArgs){
        // summary: adds a property to the construtor's class
        //    ctr:
        //        the constructor to enhance
        //    propertyName:
        //        the property name of the property to be added
        //    kwArgs:
        //        kwArgs for specialized behavior of a property (see implementors 
        //        for details)
        
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
        // summary: implementers should override this to perform any operations
        //          on the class that need to happen before properties are added
    },
    
    getSetter: function(ctr, propertyName, kwArgs){
        // sumamry: implementers should override this to return a getter method for
        //          the class being enhanced. Use kwArgs to allow for different 
        //          options for a particular property
        return null;
    },

    getGetter: function(ctr, propertyName, kwArgs){
        // sumamry: implementers should override this to return a setter method for
        //          the class being enhanced. Use kwArgs to allow for different 
        //          options for a particular property
        return null;
    }
}

dojo.declare("cosmo.model.util.SimplePropertyApplicator", cosmo.model.util.BasePropertyApplicator, {
    // summary: an implementation of BasePropertyApplicator
    // description:
    //     When calling enhanceClass(ctr, propertyArray, kwArgs), kwArgs is
    //     passed to initializeClass(ctr, kwArgs) - let's call that the "class-wide"
    //     keyword arguments.
    //
    //     Similarly, when calling enhanceClass(), the second element of each 
    //     element in the property array is a kwArgs which is passed to addProperty -
    //     let's call that "per-property" keyword arguments.
    //
    //     Here follows a decription of the options which can be set with those
    //     arguments.
    //     
    //     class-wide kwArgs:
    //         immutable: if this value is set to true, instances of the enhanced 
    //                    class cannot have their properties changed after
    //                    instantiation. In other words, the only way to set the 
    //                    properties is through the constructor
    //         enhanceInitializer:
    //                    if this value is set, the initializer of the class
    //                    will be modified so that, in addition to doing whatever
    //                    it normally does, it will also use the first argument
    //                    to populate the properties of the class. This argument
    //                    should be a hash (JavaScript object) whose keys are 
    //                    property names and whose values are what the properties
    //                    are to be set to.
    // 
    //                    if it is set to "before" the properties are set before
    //                    whatever other initializer stuff happens. Otherwise,
    //                    it happens after.
    //
    //     per-property kwArgs:
    //         default: 
    //                    if set, the property defaults to this value unless
    //                    explicitly set to something else later
    //         

    addProperty: function(ctr, propertyName, kwArgs){
        kwArgs = kwArgs || {};
        this.inherited("addProperty", arguments);
        // maintain a list of propertyNames
        ctr.prototype.__propertyNames.push(propertyName);
        // maintain a defaults map
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
            //since we'll be adding to them and don't want to add to the 
            //parent's arrays/objects!
            ctr.prototype.__propertyNames = ctr.prototype.__propertyNames.slice();
            var newDefaults = {};
            dojo.mixin(newDefaults,ctr.prototype.__defaults);
            ctr.prototype.__defaults = newDefaults;
            return;
        }
        if (!kwArgs){
            kwArgs = {};
        }
        // let's subclasses know that this class is already enhanced so that
        // initializeClass doesn't get called again on it
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
            var oldInitter = ctr.prototype._constructor;
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
            ctr.prototype._constructor = newInitializer;
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
                if (cosmo.util.lang.has(kwProps, propertyName)){
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
    if (dojo.isIE && type == "string"){
	    var lineBreakRegex = /\r\n/g;
	    var replacement = "\n";
        a = a.replace(lineBreakRegex, replacement);
        b = b.replace(lineBreakRegex, replacement);
    }  
    
    return a == b;
}   

  cosmo.model._occurrenceGetProperty = function (propertyName){
      // summary: this is the get property method for occurences of recurring
      //          events
      // description: this does all the "right stuff" in that it first checks to 
      //              see if the occurence has this property set, and if not
      //              checks the master

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
    // summmary: This is the setter for recurrence occurrences.
    // description: sets the property for a given occurrence
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

    if (dojo.isArray(thing)){
        var clone = [];
        for (var x = 0; x < thing.length; x++){
            clone[x] = cosmo.model.clone(thing[x]);
        }
        return thing;
    }
    
   if (dojo.isObject(thing)){
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