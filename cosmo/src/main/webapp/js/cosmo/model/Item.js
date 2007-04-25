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
cosmo.model.NEW_DATESTAMP = function(){return (new Date()).getTime()};
cosmo.model.NEW_OBJECT = function(){return {}};
cosmo.model.NEW_ARRAY = function(){return []};
cosmo.model.TRIAGE_NOW = 100;
cosmo.model.TRIAGE_LATER = 200;
cosmo.model.TRIAGE_DONE = 300;
      
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
    
    var stampInstanceCtr = dojo.declare(ctrName+"Instance", newCtr, {
        initializer: function stampInstanceInitializer(master, instanceDate){
           this._master = master;
           this.instanceDate = instanceDate; 
        },
        
        //it doesn't make sense to initialze properties of an instance. 
        initializeProperties: function noop(){
            return;
        },
    })
 
 
    return newCtr;
}

cosmo.model.declare("cosmo.model.Item", null, 
    //declare the dynamically generated properties
   [["uid", {"default": null}],
    ["displayName", {"default": null} ],
    ["version", {"default": null} ],
    ["creationDate", {"default": cosmo.model.NEW_DATESTAMP}],
    ["modifiedDate", {"default": cosmo.model.NEW_DATESTAMP}],
    ["triageStatus", {"default": 100}],
    ["autoTriage", {"default": false}],
    ["rank", {"default": 0}]
   ], 
   //declare other properties
  {
      initializer: function(kwArgs){
//            dojo.debug("Item Initter");
            this.initializeProperties(kwArgs);
      }
  });

cosmo.model.declare("cosmo.model.Note", cosmo.model.Item, 
    [ ["body", {"default": null}] ],
    {
        //TODO could be useful to use the same format as is in the UUID in EIM
         INSTANCE_FMT_STRING: "%Y-%m-%d %H:%M:%S",
        
        _stamps: null,
        
        initializer: function(){
//            dojo.debug("Note Initter");
            
            this._stamps = {};
            this._modifications = {};
        },
        
        getStamp: function(/*String*/ stampName){
            return this._stamps[stampName];
        }, 
        
        getModification: function getModification(/*cosmo.datetime.Date*/ instanceDate){
            return this._modifications[this._formatInstanceDate(instanceDate)];
        },
        
        addModification: function(/*cosmo.model.Modification*/modification){
            this._modifications[this._formatInstanceDate(modification.getInstanceDate())] = modification;
        },
        
        removeModification: function removeModification(/*cosmo.model.Modification*/ instanceDate){
            delete(this._modifications[this._formatInstanceDate(instanceDate)]);  
        },
        
        _formatInstanceDate: function formatInstanceDate(/*cosmo.datetime.Date*/date){
            return date.strftime(this.INSTANCE_FMT_STRING);
        },
      
        isInstance: function isInstance(){
          return false;
        },
      
        getMaster: function getMaster(){
          return this;
        },
        
        addStamp: function addStamp(/*cosmo.model.BaseStamp*/stamp){
            stamp.item = this;
            this._stamps[stamp.stampMetaData.stampName] = stamp;
        },
        
        removeStamp: function removeStamp(/*String*/ stampName){
            delete this._stamps[stampName];
        },
        
        getEventStamp: function getEventStamp(){
            return this.getStamp("event");
        },
        
        getTaskStamp: function getTaskStamp(){
            return this.getStamp("task");
        },
        
        getNoteInstance: function getNoteInstance(/*cosmo.datetime.Date*/ instanceDate){
//            dojo.debug("getNoteInstance");
            return new cosmo.model.NoteInstance(this, instanceDate);
        }
    });
    
dojo.declare("cosmo.model.NoteInstance", cosmo.model.Note,{
    __noOverride:{uid:1,version:1},
    
    initializer: function noteInstanceInitializer(master, instanceDate){
//        dojo.debug("noteInstanceInitializer");
        this._master = master;
        this.instanceDate = instanceDate;
    },
    
   isInstance: function isInstance(){
        return true;
    },
    
    getMaster: function getMaster(){
        return this._master;
    },
    
    _getMasterProperty: function noteInstanceGetMasterProperty(propertyName){
//        dojo.debug("MASTER: " +propertyName);
        return this._master.__getProperty(propertyName);
    },
    
    _getModifiedProperty: function noteInstanceGetModifiedProperty(propertyName){
//        dojo.debug("INSTANCE: " +propertyName);
        var modification = this._master.getModification(this.instanceDate);
//        dojo.debug("MOD: " + modification);
        return modification.getModifiedProperties()[propertyName];
    },
    
    _setModifiedProperty: function noteInstanceSetModifiedProperty(propertyName, value){
        var modification = master.getModification(this.instanceDate);
        modification[propertyName] = value;  
    },
    
    __getProperty: cosmo.model._instanceGetProperty,  
    
    __setProperty: cosmo.model._instanceSetProperty,
    
    
    initializeProperties: function noop(){
        return;
    },
    
    _throwOnlyMaster: function(){
     throw new Error("You can only call this method on the master item");
    },

///    
    getStamp: function(/*String*/ stampName){
        //TODO
    }, 

    addStamp: function addStamp(/*cosmo.model.BaseStamp*/stamp){
        //TODO
    },
    
    removeStamp: function removeStamp(/*String*/ stampName){
        //TODO
    },

    getModification: function(/*cosmo.datetime.Date*/ instanceDate){
        this._throwOnlyMaster();
    },
    
    addModification: function(/*cosmo.model.Modification*/modification){
        this._throwOnlyMaster();
    },
    
    removeModification: function(/*cosmo.model.Modification*/ instanceDate){
        this._throwOnlyMaster();
    },

    getNoteInstance: function getNoteInstance(/*cosmo.datetime.Date*/ instanceDate){
        this._throwOnlyMaster();
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
    },
    
    getAttribute: function getAttribute(name){
        for (var x = 0; x < this.attributes.length; x++){
            var attr = this.attributes[x];
            if (attr.name = name){
                return attr;
            }
        }
        
        return null;  
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
    stampMetaData: null,
    item: null
});

cosmo.model.declareStamp("cosmo.model.EventStamp", "event",
    [ ["startDate", cosmo.datetime.Date, {}],
      ["duration", Number, {}],
      ["anytime", Boolean, {}],
      ["location", String, {}],
      ["rrule", cosmo.model.RecurrenceRule, {}],
      ["exdates", [Array, cosmo.datetime.Date], {}],
      ["status", String, {}],
    ],
    {
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        }
    });
    
cosmo.model.declareStamp("cosmo.model.TaskStamp", "task",
    [ ],
    {
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        }
    });