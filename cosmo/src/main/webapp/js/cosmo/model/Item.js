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
dojo.provide("cosmo.model.Item");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.model.util");

cosmo.model.declare = function(/*String*/ ctrName, /*Function*/ parentCtr, propertiesArray, otherDeclarations){
    var newCtr = dojo.declare(ctrName, parentCtr, otherDeclarations);
    cosmo.model.util.simplePropertyApplicator.enhanceClass(newCtr, propertiesArray, {enhanceInitializer: false});
    return newCtr;
}

cosmo.model.declareStamp = function(/*String*/ ctrName, stampName, attributesArray, otherDeclarations){
    var newCtr = dojo.declare(ctrName, cosmo.model.BaseStamp, otherDeclarations);
    var meta = new cosmo.model.StampMetaData(stampName, attributesArray);
    newCtr.prototype.stampMetaData = meta;
    var propertiesArray = [];
    for (var x = 0; x < attributesArray.length; x++){
        var attArgs = attributesArray[x];
        propertiesArray.push([attArgs[0], attArgs[2]]);
    }
    cosmo.model.util.simplePropertyApplicator.enhanceClass(newCtr, propertiesArray, 
        {enhanceInitializer: false});
    return newCtr;
}

cosmo.model.NEW_DATE = function(){return (new Date()).getTime()};
cosmo.model.NEW_OBJECT = function(){return {}};
cosmo.model.NEW_ARRAY = function(){return []};

cosmo.model.declare("cosmo.model.Item", null, 
    //declare the dynamically generated properties
   [["uid", {"default": null}],
    ["displayName", {"default": null} ],
    ["version", {"default": null} ],
    ["creationDate", {"default":  cosmo.model.NEW_DATE}],
    ["modifiedDate", {"default":  cosmo.model.NEW_DATE}]
   ], 
   //declare other properties
  {
      initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
      },
  });

cosmo.model.declare("cosmo.model.Note", cosmo.model.Item, 
    [],
    {
        //TODO could be useful to use the same format as is in the UUID in EIM
         INSTANCE_FMT_STRING: "%Y-%m-%d %H:%M:%S",
        
        _stamps: null,
        
        initializer: function(){
            this._stamps = {};
            this._modifications = {};
        },
        
        getStamp: function(/*String*/ stampName){
            return _stamps[stampName];
        }, 
        
        getModification: function(/*cosmo.datetime.Date*/ instanceDate){
            return this._modifications[this._formatInstanceDate(instanceDate)];
        },
        
        addModification: function(/*cosmo.model.Modification*/modification){
            this._modifications[this._formatInstanceDate(modification.getInstanceDate())] = modification;
        },
        
        removeModification: function(/*cosmo.model.Modification*/ instanceDate){
            delete(this._modifications[this._formatInstanceDate(instanceDate)]);  
        },
        
        _formatInstanceDate: function(/*cosmo.datetime.Date*/date){
            return date.strftime(cosmo.model.INSTANCE_FMT_STRING);dojo.lang.getType()
        }
    });

cosmo.model.declare("cosmo.model.Modification", null,
   [["instanceDate", {"default": null}],
    ["modifiedProperties", {"default": cosmo.model.NEW_OBJECT}],
    ["modifiedStamps", {"default": cosmo.model.NEW_OBJECT}]],
    {
      initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
      },
    });

dojo.declare("cosmo.model.Collection", cosmo.model.Item, {
});

dojo.declare("cosmo.model.StampMetaData", null,{
    stampName: null, 
    attributes: null,
    
    initializer: function(stampName, stampAttributesArray){
        this.attributes = [];
        this.stampName = stampName || null;
        if (!stampAttributesArray){
            return;
        } else {
            for (var x = 0; x < stampAttributesArray.length; x++){
                var ctrArgs = stampAttributesArray[x];
                this.attributes.push(new cosmo.model.StampAttribute(ctrArgs[0], ctrArgs[1], ctrArgs[2]));
            }
        }
    }   
});

dojo.declare("cosmo.model.StampAttribute", null, {
    name: null,
    type: null,  /*Function*/
    
    initializer: function(name, type, kwArgs){
        this.name = name;
        this.type = type;            
    }
});

dojo.declare("cosmo.model.BaseStamp", null, {
    stampMetaData: null
});