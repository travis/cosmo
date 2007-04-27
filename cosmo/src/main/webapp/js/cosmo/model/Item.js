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

cosmo.model._stampRegistry = {};
   
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
    
    var stampOccurrenceCtr = dojo.declare(ctrName+"Occurrence", newCtr, {
        __noOverride:{},

        initializer: function stampOccurrenceInitializer(noteOccurrence){
           this._master = noteOccurrence._master;
           this.recurrenceId = noteOccurrence.recurrenceId; 
           this.item = noteOccurrence;
        },
        
        //it doesn't make sense to initialze properties of an occurrence. 
        initializeProperties: function noop(){
            return;
        },
        
        __getProperty: cosmo.model._occurrenceGetProperty,  

        __setProperty: cosmo.model._occurrenceSetProperty,

        getMaster: function getMaster(){
            return this._master;
        },
    
        _getMasterProperty: function noteOccurrenceGetMasterProperty(propertyName){
            return this._master._stamps[stampName].__getProperty(propertyName);
        },
    
	_getModifiedProperty: function stampOccurrenceGetModifiedProperty(propertyName){
            var modification = this._master.getModification(this.recurrenceId);
            var modifiedStamp = modification._modifiedStamps[stampName];
	    if (modifiedStamp){
		return modifiedStamp[propertyName];
	    }
        },
    
	_setModifiedProperty: function stampOccurrenceSetModifiedProperty(propertyName, value){
            var modification = this._master.getModification(this.recurrenceId);
            var modifiedStamp = modification._modifiedStamps[stampName];
	    if (!modifiedStamp){
		modifiedStamp = {};
		modification._modifiedStamps[stampName] = modifiedStamp;
	    }
	    modifiedStamp[propertyName] = value;
        },
    })
     
    cosmo.model._stampRegistry[stampName] 
        = {constructor:newCtr, occurrenceConstructor:stampOccurrenceCtr};
    
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
            dojo.debug("Item Initter");
            this.initializeProperties(kwArgs);
      }
  });

cosmo.model.declare("cosmo.model.Note", cosmo.model.Item, 
    [ ["body", {"default": null}] ],
    {
        //TODO could be useful to use the same format as is in the UUID in EIM
         OCCURRENCE_FMT_STRING: "%Y-%m-%d %H:%M:%S",
        
        _stamps: null,
        
        initializer: function(){
            this._stamps = {};
            this._modifications = {};
        },
        
        getStamp: function(/*String*/ stampName, /*Boolean?*/ createIfDoesntExist) {
           var stamp = this._stamps[stampName];
           
           if (stamp){
               return stamp;
           } 
           
           if (createIfDoesntExist){
               var ctr = cosmo.model._stampRegistry[stampName]["constructor"];
               var stamp =  new ctr({item:this});
           this._stamps[stampName] = stamp;
           return stamp;
       }          
           
        }, 
        
        getModification: function getModification(/*cosmo.datetime.Date*/ recurrenceId){
            return this._modifications[this._formatRecurrenceId(recurrenceId)];
        },
        
        addModification: function(/*cosmo.model.Modification*/modification){
            this._modifications[this._formatRecurrenceId(modification.getRecurrenceId())] = modification;
        },
        
        removeModification: function removeModification(/*cosmo.model.Modification*/ recurrenceId){
            delete(this._modifications[this._formatRecurrenceId(recurrenceId)]);  
        },
        
        _formatRecurrenceId: function formatRecurrenceId(/*cosmo.datetime.Date*/date){
            return date.strftime(this.OCCURRENCE_FMT_STRING);
        },
      
        isOccurrence: function isOccurrence(){
          return false;
        },
      
        getMaster: function getMaster(){
          return this;
        },

        removeStamp: function removeStamp(/*String*/ stampName){
            delete this._stamps[stampName];
        },
        
        getEventStamp: function getEventStamp(/*Boolean?*/ createIfDoesntExist){
            return this.getStamp("event", createIfDoesntExist);
        },
        
        getTaskStamp: function getTaskStamp(/*Boolean*/ createIfDoesntExist){
            return this.getStamp("task", createIfDoesntExist);
        },
        
        getNoteOccurrence: function getNoteOccurrence(/*cosmo.datetime.Date*/ recurrenceId){
            return new cosmo.model.NoteOccurrence(this, recurrenceId);
        }
    });
    
dojo.declare("cosmo.model.NoteOccurrence", cosmo.model.Note, {
    __noOverride:{uid:1,version:1},
    
    initializer: function noteOccurrenceInitializer(master, recurrenceId){
//        dojo.debug("noteOccurrenceInitializer");
        this._master = master;
        this.recurrenceId = recurrenceId;
    },
    
   isOccurrence: function isOccurrence(){
        return true;
    },
    
    getMaster: function getMaster(){
        return this._master;
    },
    
    _getMasterProperty: function noteOccurrenceGetMasterProperty(propertyName){
        return this._master.__getProperty(propertyName);
    },
    
    _getModifiedProperty: function noteOccurrenceGetModifiedProperty(propertyName){
        var modification = this._master.getModification(this.recurrenceId);
        return modification.getModifiedProperties()[propertyName];
    },
    
    _setModifiedProperty: function noteOccurrenceSetModifiedProperty(propertyName, value){
        var modification = this._master.getModification(this.recurrenceId);
        modification._modifiedProperties[propertyName] = value;  
    },
    
    __getProperty: cosmo.model._occurrenceGetProperty,  
    
    __setProperty: cosmo.model._occurrenceSetProperty,
    
    
    initializeProperties: function noop(){
        return;
    },
    
    _throwOnlyMaster: function(){
        throw new Error("You can only call this method on the master item");
    },

    getStamp: function occurrenceGetStamp(/*String*/ stampName, /*Boolean*/createIfDoesntExist){
           var ctr = cosmo.model._stampRegistry[stampName]["occurrenceConstructor"];
           //does the parent have the stamp?
           if (this.getMaster().getStamp(stampName)){
               return new ctr(this);
           } else {
              var modification = master.getModification(this.recurrenceId);
              if (modification && modification.getModifiedStamps[stampName]){
                  return new ctr(this);
              }                               
           } 
           
           if (createIfDoesntExist){
               return new ctr(this);
           } else {
               return null;
           }
    }, 
    
    removeStamp: function removeStamp(/*String*/ stampName){
        throw new Error("remove stamp not implented yet!");
    },

    getModification: function(/*cosmo.datetime.Date*/ recurrenceId){
        this._throwOnlyMaster();
    },
    
    addModification: function(/*cosmo.model.Modification*/modification){
        this._throwOnlyMaster();
    },
    
    removeModification: function(/*cosmo.model.Modification*/ recurrenceId){
        this._throwOnlyMaster();
    },

    getNoteOccurrence: function getNoteOccurrence(/*cosmo.datetime.Date*/ recurrenceId){
        this._throwOnlyMaster();
    }
    
});

cosmo.model.declare("cosmo.model.Modification", null,
   [["recurrenceId", {"default": null}],
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