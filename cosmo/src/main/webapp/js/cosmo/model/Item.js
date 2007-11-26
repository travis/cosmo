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
dojo.require("cosmo.model.Delta");
dojo.require("dojo.uuid.TimeBasedGenerator");

cosmo.model.NEW_DATESTAMP = function(){return (new Date()).getTime()};
cosmo.model.NEW_OBJECT = function(){return {}};
cosmo.model.NEW_ARRAY = function(){return []};

//Triage Statuses
cosmo.model.TRIAGE_NOW = 100;
cosmo.model.TRIAGE_LATER = 200;
cosmo.model.TRIAGE_DONE = 300;

//for use in ModBy records
cosmo.model.ACTION_EDITED = 100;
cosmo.model.ACTION_QUEUED = 200;
cosmo.model.ACTION_SENT = 300;
cosmo.model.ACTION_UPDATED = 400;
cosmo.model.ACTION_CREATED = 500;

cosmo.model._stampRegistry = {};

cosmo.model.uuidGenerator = dojo.uuid.TimeBasedGenerator;

cosmo.model.getStampMetaData = function(stampName){
    return this._stampRegistry[stampName].constructor.prototype.stampMetaData;
}
cosmo.model.declare = function(/*String*/ ctrName, /*Function*/ parentCtr, propertiesArray, otherDeclarations, kwArgs){
    var newCtr = dojo.declare(ctrName, parentCtr, otherDeclarations);
    cosmo.model.util.simplePropertyApplicator.enhanceClass(newCtr, propertiesArray, kwArgs || {});
    return newCtr;
}

cosmo.model.declareStamp = function(/*String*/ ctrName, stampName, namespace, attributesArray, otherDeclarations, occurrenceDeclarations, seriesOnly){
    var newCtr = dojo.declare(ctrName, cosmo.model.BaseStamp, otherDeclarations);
    var meta = new cosmo.model.StampMetaData(stampName, namespace, attributesArray, seriesOnly);
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

        initializer: function (noteOccurrence){
           this._master = noteOccurrence._master;
           this.recurrenceId = noteOccurrence.recurrenceId; 
           this.item = noteOccurrence;
        },
        
        //it doesn't make sense to initialze properties of an occurrence.
        initializeProperties: function (){
            return;
        },
        
        __getProperty: cosmo.model._occurrenceGetProperty,  

        __setProperty: cosmo.model._occurrenceSetProperty,

        getMaster: function (){
            return this._master;
        },
        
        isOccurrenceStamp: function(){
            return true;
        },
    
        _getMasterProperty: function (propertyName){
            if (this._masterPropertyGetters && this._masterPropertyGetters[propertyName]){
                return this._masterPropertyGetters[propertyName].apply(this);
            }
            var masterStamp = this._master._stamps[stampName];
            if (!masterStamp){
                return undefined;
            }
            return masterStamp.__getProperty(propertyName);
        },
    
        _getModifiedProperty: function (propertyName){
            var modification = this._master.getModification(this.recurrenceId);
            var modifiedStamp = modification._modifiedStamps[stampName];
            if (modifiedStamp){
                return modifiedStamp[propertyName];
            }
        },
    
        _setModifiedProperty: function (propertyName, value, create){
            var modification = this._master.getModification(this.recurrenceId, create);
            var modifiedStamp = modification._modifiedStamps[stampName];
            if (!modifiedStamp){
                modifiedStamp = {};
                modification._modifiedStamps[stampName] = modifiedStamp;
            }
            modifiedStamp[propertyName] = value;
        }
});

    dojo.lang.mixin(stampOccurrenceCtr.prototype, occurrenceDeclarations || {});
    
    
    cosmo.model._stampRegistry[stampName] 
        = {constructor:newCtr, occurrenceConstructor:stampOccurrenceCtr};
    
    return newCtr;
}

cosmo.model.declare("cosmo.model.Item", null, 
    //declare the dynamically generated properties
   [["uid", {"default": dojo.lang.hitch(cosmo.model.uuidGenerator, cosmo.model.uuidGenerator.generate)}],
    ["displayName", {"default": null} ],
    ["version", {"default": null} ],
    ["creationDate", {"default": cosmo.model.NEW_DATESTAMP}],
    ["modifiedDate", {"default": cosmo.model.NEW_DATESTAMP}],
    ["triageStatus", {"default": 100}],
    ["autoTriage", {"default": true}],
    ["rank", {"default": 0}]
   ],
   //declare other properties
  {
      initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
      }
  });
cosmo.model.Item.triageStatusCodeNumbers = {
    DONE: 300,
    NOW: 100,
    LATER: 200 };

cosmo.model.declare("cosmo.model.Note", cosmo.model.Item, 
    [ ["body", {"default": null}],
      ["icalUid", {"default": null}],
      ["modifiedBy", {"default": function(){return new cosmo.model.ModifiedBy()}}]],
    {
        //TODO could be useful to use the same format as is in the UUID in EIM
         OCCURRENCE_FMT_STRING: "%Y-%m-%d %H:%M:%S",
        
        _stamps: null,
        _stampsToDelete: null,
        
        initializer: function(){
            this._stamps = {};
            this._modifications = {};
            this._stampsToDelete = {};
        },
        
        getStamp: function(/*String*/ stampName, /*Boolean?*/ createIfDoesntExist, /*Object*/ initialProps) {
           initialProps =  initialProps || {};
           var stamp = this._stamps[stampName];
           
           if (stamp){
               return stamp;
           } 
           
           if (createIfDoesntExist){
               var ctr = cosmo.model._stampRegistry[stampName]["constructor"];
               var stamp =  new ctr(dojo.lang.mixin({item:this}, initialProps));
               this._stamps[stampName] = stamp;
               return stamp;
           }          
           
        }, 
        
        getModification: function (/*cosmo.datetime.Date*/ recurrenceId, create){
            var mod =  this._modifications[this._formatRecurrenceId(recurrenceId)];
            if (!mod && create){
                mod = new cosmo.model.Modification({
                        recurrenceId: recurrenceId
                });
                this.addModification(mod);
            }
            return mod;
        },
        
        addModification: function(/*cosmo.model.Modification*/modification){
            this._modifications[this._formatRecurrenceId(modification.getRecurrenceId())] = modification;
        },
        
        removeModification: function (/*cosmo.model.Modification*/ recurrenceId){
            delete(this._modifications[this._formatRecurrenceId(recurrenceId)]);  
        },
        
        _formatRecurrenceId: function (/*cosmo.datetime.Date*/date){
            return date.strftime(this.OCCURRENCE_FMT_STRING);
        },
      
        isOccurrence: function (){
            return false;
        },
        
        isFirstOccurrence: function (){
            throw new Error("This is the master event, not an occurrence!");
        },
      
        getMaster: function (){
            return this;
        },
        
        isMaster: function (){
            return true;
        },
        
        hasRecurrence: function (){
            var stamp = this.getMaster().getEventStamp();

            if (!stamp){
                return false;
            }  
            
            return !!stamp.getRrule();
        },

        removeStamp: function (/*String*/ stampName){
            delete this._stamps[stampName];
            this.addStampToDelete(stampName);
        },
       
        getEventStamp: function (/*Boolean?*/ createIfDoesntExist, /*Object?*/initialProps){
            return this.getStamp("event", createIfDoesntExist, initialProps);
        },
        
        getTaskStamp: function (/*Boolean*/ createIfDoesntExist, /*Object?*/initialProps){
            return this.getStamp("task", createIfDoesntExist, initialProps);
        },

        getMailStamp: function (/*Boolean*/ createIfDoesntExist, /*Object?*/initialProps){
            return this.getStamp("mail", createIfDoesntExist, initialProps);
        },
        
        getNoteOccurrence: function (/*cosmo.datetime.Date*/ recurrenceId){
            return new cosmo.model.NoteOccurrence(this, recurrenceId);
        },
        
        _getImplicitTriageStatus: function(){
            //summary: returns what the triage status would be based on auto-triage rules
            var eventStamp = this.getEventStamp();

            if (!eventStamp){
                return cosmo.model.TRIAGE_NOW;
            } 
            
            var startDate = eventStamp.getStartDate();
            var endDate = eventStamp.getEndDate();
            var startTime = eventStamp.getStartDate().getTime();
            var endTime = (endDate != null) ? endDate.getTime() : startDate.getTime();

            if (eventStamp.getAllDay() || eventStamp.getAnyTime()){
               endTime = startTime + (24 * 60 * 60 * 1000);
            } 
           
            var now = (new Date()).getTime();
           
            if (now <= endTime){
                if (now >= startTime){
                    // This is a little bit of a hack for bug 10520:
                    // If the event is happening now and getLastPastOccurrence has been set, follow the
                    // semantics of getLastPastOccurrence.
                    // Once we implement the getLastPastOccurence sematics for the general case, 
                    // we can simplify this.
                    if (startDate.equals(eventStamp.getLastPastOccurrence())) {
                        return cosmo.model.TRIAGE_DONE;
                    }
                    else {
                        return cosmo.model.TRIAGE_NOW;
                    }
                } else {
                    return cosmo.model.TRIAGE_LATER;
                }
            } else {
                return cosmo.model.TRIAGE_DONE;
            }
            
        },
        
        autoTriage: function(){
           if (!this.getAutoTriage() || !this.getEventStamp() ||(this.isMaster() && this.hasRecurrence())){
               return false;
           }    
           
           var currentTriageStatus = this.getTriageStatus();
           var newTriageStatus = this._getImplicitTriageStatus();
           
           
           if (newTriageStatus != currentTriageStatus){
               this.setTriageStatus(newTriageStatus);
               return true;
           }
           
           return false;
           
        },
        
        tickle: function(){
           if (!this.getAutoTriage() || (this.isMaster() && this.hasRecurrence())){
               return false;
           }
           
           var currentTriageStatus = this.getTriageStatus();
           
           //you can only traige from LATER to NOW
           if (currentTriageStatus && currentTriageStatus != cosmo.model.TRIAGE_LATER){
               return false;
           }
           
           var newTriageStatus = this._getImplicitTriageStatus();
           if (newTriageStatus != cosmo.model.TRIAGE_LATER || (!currentTriageStatus)){
               this.setTriageStatus(cosmo.model.TRIAGE_NOW);
               this.setAutoTriage(false);
               return true;
           }
           
           return false;
           
        },
        
        clone: function (){
          //summary: creates a deep copy of all the properties of this Item. 
          //description: Copies all the properties of the Note, making copies
          //             of all mutable objects
          var clone = this._inherited("clone");
          if (this._stamps){
              clone._stamps = cosmo.model.clone(this._stamps);
              for (var stampName in clone._stamps){
                  var stamp = clone._stamps[stampName];
                  stamp.item = clone;
              }
          }
          if (this._modifications){
              clone._modifications = cosmo.model.clone(this._modifications);
          }
          return clone;
        },
        
        getItemUid: function () {
            if (this.isMaster()){
                return this.getUid()
            } 
            else {
                return this.getUid() + 
                    this.recurrenceId.strftime(this.OCCURRENCE_FMT_STRING).replace(' ', '_');
            }
        }
              
    });
    
dojo.declare("cosmo.model.NoteOccurrence", cosmo.model.Note, {
    __noOverride:{uid:1,version:1},
    
    initializer: function (master, recurrenceId){
        this._master = master;
        this.recurrenceId = recurrenceId;
    },
    
    isOccurrence: function (){
        return true;
    },
    
    isMaster: function(){
        return false;  
    },
    
    getMaster: function (){
        return this._master;
    },
    
    autoTriage: function(){
        var modification = this._getThisModification();
        if (modification){
            return this._inherited("autoTriage");
        } else {
            return false;
        }
    },
    
    tickle: function(){
        var modification = this._getThisModification();
        if (modification){
            return this._inherited("tickle");
        } else {
            return false;
        }
    },
    
    _getMasterProperty: function (propertyName){
        return this._master.__getProperty(propertyName);
    },
    
    _getModifiedProperty: function (propertyName){
        var modification = this._master.getModification(this.recurrenceId);
        return modification.getModifiedProperties()[propertyName];
    },
    
    _setModifiedProperty: function (propertyName, value, create){
        var modification = this._master.getModification(this.recurrenceId, create);
        modification._modifiedProperties[propertyName] = value;  
    },
    
    _getThisModification: function(){
        return this._master.getModification(this.recurrenceId);  
    },
    
    __getProperty: cosmo.model._occurrenceGetProperty,  
    
    __setProperty: cosmo.model._occurrenceSetProperty,
        
    initializeProperties: function (){
        return;
    },
    
    _throwOnlyMaster: function(){
        throw new Error("You can only call this method on the master item");
    },

    getStamp: function (/*String*/ stampName, /*Boolean*/createIfDoesntExist){
           if (cosmo.model.getStampMetaData(stampName).seriesOnly){
               return this.getMaster().getStamp(stampName, createIfDoesntExist);
           }
           var modification = this.getMaster().getModification(this.recurrenceId);
           var ctr = cosmo.model._stampRegistry[stampName]["occurrenceConstructor"];
           var deleted = modification && modification.getDeletedStamps()[stampName];
           
           //Has this stamp been deleted for the modification for this occurrence?
           //if not...
           if (!deleted){
               //does the master already have this stamp?
               if (this.getMaster().getStamp(stampName)){
                   return new ctr(this);
               } else {
                  //the master doesn't have this stamp, but maybe this occurrence 
                  //already does?
                  if (modification && modification.getModifiedStamps()[stampName]){
                      return new ctr(this);
                  
                  //ok, the master doesn't have the stamp, the occurrence doesn't either
                  //but the create flag was passed 
                  } else if (createIfDoesntExist){
                      //if there is not yet a modification make one
                      if (!modification){
                          modification = new cosmo.model.Modification({
                              recurrenceId: this.recurrenceId
                          });
                          this.getMaster().addModification(modification);
                      }
                      //add the new modified "stamp" to the modification 
                      modification.getModifiedStamps()[stampName] = {};
                      return new ctr(this);
                  } else {
                      return null;
                  }
               } 
           } else {
               if (createIfDoesntExist){
                   return new ctr(this);
               } else {
                   return null;
               }
           }
    }, 
    
    setUrl: function(protocol, url){
        this._getThisModification().setUrl(protocol, url);
    }, 
      
    getUrl: function(protocol){
        return this._getThisModification().getUrl(protocol);
    },
      
    setUrls: function(protocolToUrlMap){
        this._getThisModification().setUrls(protocolToUrlMap);
    },
      
    getUrls: function(){
        return this._getThisModification().getUrls();
    },
    
    removeStamp: function (/*String*/ stampName){
        this.addStampToDelete(stampName);
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
    
    hasModification: function(){
        return !!this._master.getModification(this.recurrenceId);
    },

    getNoteOccurrence: function (/*cosmo.datetime.Date*/ recurrenceId){
        this._throwOnlyMaster();
    },
    
    isFirstOccurrence: function(){
        return (this.getMaster().getEventStamp().getStartDate().toUTC() == this.recurrenceId.toUTC());  
    }, 
    
    clone: function(){
        throw new Error("you cannot clone an occurrence!");
    },
    
    _getNonInheritableProperty: function(propertyName, defaultFunction){
        //summary: gets properties that cannot be inherited from the master item.
        //         The value of the property must therefore either come from the modification 
        //         item or as the evaluation of the passed default function
        var modification = this._getThisModification();
        if (!modification){
            return defaultFunction();
        }
        
        var modifiedProperty = this._getModifiedProperty(propertyName);
        if (typeof(modifiedProperty) != "undefined"){
            return modifiedProperty;            
        }
        
        return defaultFunction();
        
    },
    
    getTriageStatus: function(){
        //triage status is not inherited from the master. In fact, the triage status of master events should 
        //never really be used. 
        return this._getNonInheritableProperty("triageStatus", dojo.lang.hitch(this, this._getImplicitTriageStatus))
    },
    
    getAutoTriage: function(){
        return this._getNonInheritableProperty("autoTriage", dojo.lang.hitch(this, function(){return true}))
    },

    setTriageStatus: function(triageStatus){
        if (!this.hasRecurrence()){
            this.__setProperty("triageStatus", triageStatus);
        }
        
        this._setModifiedProperty("triageStatus", triageStatus, true);
        
    },
    
    getStampsToDelete: function(){
        var mod = this._getThisModification();
        if (mod){
            return mod.getStampsToDelete();
        }
    },
    
    addStampToDelete: function(stampName){
        var mod = this._getThisModification();
        if (!mod){
            mod = new cosmo.model.Modification({
                recurrenceId: this.recurrenceId
            });
            master.addModification(modification);
        }
        mod.addStampToDelete(stampName);
    },
    
    removeStampToDelete: function(stampName){
        var mod = this._getThisModification();
        if (mod){
            mod.removeStampToDelete(stampName);
        }
    },
    
    isStampToBeDeleted: function(stampName){
        var mod = this._getThisModification();
        if (mod){
            return mod.isStampToBeDeleted(stampName);
        }
        return false;
    },
    
    clearStampsToDelete: function(){
        var mod = this._getThisModification();
        if (mod){
            mod.clearStampsToDelete();
        }
    }
});

cosmo.model.declare("cosmo.model.Modification", null,
   [["recurrenceId", {"default": null}],
    ["modifiedProperties", {"default": cosmo.model.NEW_OBJECT}],
    ["modifiedStamps", {"default": cosmo.model.NEW_OBJECT}],
    ["deletedStamps", {"default": cosmo.model.NEW_OBJECT}]
    ],
    {
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        }
    });

cosmo.model.declare("cosmo.model.Collection", cosmo.model.Item, 
    [["writeable", {"default": false}]
    ],
    {
       isWriteable: function(){
          return this.getWriteable();
       }
    });

cosmo.model.declare("cosmo.model.Subscription", cosmo.model.Item,
    [["ticketKey", {"default": null}],
     ["writeable", {"default": false}],
     ["collection", {"default": null}],
     ["collectionDeleted", {"default": false}],
     ["ticketDeleted", {"default": false}]
     ],
    {
         isWriteable: function(){
             return this.getCollection().getWriteable();
         },
         getWriteable: function(){
             return this.isWriteable();
         }
    }
);

dojo.declare("cosmo.model.StampMetaData", null,{
    __immutable:true,
    stampName: null,
    namespace: null,
    attributes: null,

    //if this stamp only can apply to the entire series, this is true
    seriesOnly: false,
    
    initializer: function(stampName, namespace,stampAttributesArray, seriesOnly){
        this.attributes = [];
        this.stampName = stampName || null;
        this.namespace = namespace;
        this.seriesOnly = seriesOnly || false;
        if (!stampAttributesArray){
            return;
        } else {
            for (var x = 0; x < stampAttributesArray.length; x++){
                var ctrArgs = stampAttributesArray[x];
                this.attributes.push(new cosmo.model.StampAttribute(ctrArgs[0], ctrArgs[1], ctrArgs[2]));
            }
        }
    },
    
    getAttribute: function (name){
        for (var x = 0; x < this.attributes.length; x++){
            var attr = this.attributes[x];
            if (attr.name == name){
                return attr;
            }
        }
        
        return null;  
    },
    
    clone: function(){
        //should be treated like an immutable object.
        return this;
    } 
       
});

dojo.declare("cosmo.model.StampAttribute", null, {
    name: null,
    type: null,  /*Function*/
    
    initializer: function(name, type, kwArgs){
        this.name = name;
        this.type = type;            
    },
    
    clone: function(){
        //should be treated like an immutable object.
        return this;
    } 
});

dojo.declare("cosmo.model.BaseStamp", null, {
    stampMetaData: null,
    item: null,
    initializer: function (kwArgs){
        if (kwArgs){
            this.item = kwArgs.item;
        }
    },
    
    isOccurrenceStamp: function (){
        return false;
    }
    
});

cosmo.model.declareStamp("cosmo.model.TaskStamp", "task", "http://osafoundation.org/eim/task/0",
    [ ],
    {
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        }
    });

cosmo.model.declareStamp("cosmo.model.MailStamp", "mail", "http://osafoundation.org/eim/mail/0",
    [[ "messageId", String, {}],
     [ "headers", String, {}],
     [ "fromAddress", String, {}],
     [ "toAddress", String, {}],
     [ "ccAddress", String, {}],
     [ "bccAddress", String, {}],
     [ "originators", String, {}],
     [ "dateSent", String, {}],
     [ "inReplyTo", String, {}],
     [ "references", String, {}]
     ],
    {
        initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
        }
    },{},true);

cosmo.model.declare("cosmo.model.ModifiedBy", null, 
    //declare the dynamically generated properties
   [["userId", {"default": null} ],
    ["action", {"default": cosmo.model.ACTION_CREATED} ],
    ["timeStamp", {"default": cosmo.model.NEW_DATESTAMP}]
   ],
   //declare other properties
  {
      initializer: function(kwArgs){
            this.initializeProperties(kwArgs);
      }
  });  

//stuff that note and stamp has in common.
cosmo.model._noteStampCommon = {
        applyChange: function(propertyName, changeValue, type){
          var getterAndSetter = cosmo.model.util.getGetterAndSetterName(propertyName);
          var setterName = getterAndSetter[1];
          if (type =="occurrence"){
              this[setterName](changeValue);
          } else if (type == "master"){
              var masterObject = this;
              if (this instanceof cosmo.model.BaseStamp){
                  if (this.isOccurrenceStamp()){
                      masterObject = this.getMaster().getStamp(this.stampMetaData.stampName, true);
                  }
              } else {
                 if (this.isOccurrence()){
                     masterObject = this.getMaster();
                 }                  
              }
              masterObject[setterName](changeValue);
          }
        }
}

cosmo.model._urlsMixin =  {
      _urls: {},
      setUrl: function(protocol, url){
          this._urls[protocol] = url; 
      }, 
      
      getUrl: function(protocol){
          return this._urls[protocol];
      },
      
      setUrls: function(protocolToUrlMap){
          this._urls = protocolToUrlMap;
      },
      
      getUrls: function(){
          return this._urls;
      }
}

cosmo.model._deletedStampsMixin = {
    _stampsToDelete: {},
    getStampsToDelete: function(){
        var array = [];
        for (var stampName in this._stampsToDelete){
            if (this._stampsToDelete[stampName]){
                array.push(stampName);
            }
        }
        
        return array;    
    },
    
    addStampToDelete: function(stampName){
       this._stampsToDelete[stampName] = true;  
    },
    
    removeStampToDelete: function(stampName){
        delete this._stampsToDelete[stampName];
    },
    
    isStampToBeDeleted: function(stampName){
        return !!this._stampsToDelete(stampName);  
    },
    
    clearStampsToDelete: function(){
        this._stampsToDelete = {};
    }
    
}

dojo.lang.mixin(cosmo.model.Note.prototype, cosmo.model._noteStampCommon);
dojo.lang.mixin(cosmo.model.BaseStamp.prototype, cosmo.model._noteStampCommon);
dojo.lang.mixin(cosmo.model.Item.prototype, cosmo.model._urlsMixin);
dojo.lang.mixin(cosmo.model.Note.prototype, cosmo.model._deletedStampsMixin);
dojo.lang.mixin(cosmo.model.Modification.prototype, cosmo.model._urlsMixin);
dojo.lang.mixin(cosmo.model.Modification.prototype, cosmo.model._deletedStampsMixin);