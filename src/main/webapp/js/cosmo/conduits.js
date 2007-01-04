/*
 * Copyright 2006 Open Source Applications Foundation
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
dojo.provide("cosmo.conduits");

cosmo.conduits.Conduit = 
{	
    getCollection: function(collectionUid, transportInfo){},
    getEvents: function(collectionUid, startTime, endTime, transportInfo){},
    getEvent: function(eventUid, transportInfo){},
    saveEvent: function(eventUid, transportInfo){},
    removeEvent: function(eventUid, transportInfo){},
    getRecurrenceRules: function(eventUids, transportInfo){},
    saveRecurrenceRule: function(eventUid, recurrenceRule, transportInfo){},
    expandEvents: function(eventUids, startTime, endTime, transportInfo){},
    saveNewEventBreakRecurrence: function(event, originalEventUid, 
        originalEventEndDate, transportInfo){}, 
    saveDisplayName: function(collectionUid, newDisplayName, transportInfo){}
    
};


cosmo.conduits.OwnedCollectionConduit = 
{
    getCollection: function(collectionUid, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.getCalendar(handlerFunc, collectionUid);
        } else {
            return Cal.serv.getCalendar(collectionUid);
        }
    },
    getEvents: function(collectionUid, startTime, endTime, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.getEvents(handlerFunc, collectionUid, startTime, endTime);
        } else {
            return Cal.serv.getEvents(collectionUid, startTime, endTime);
        }
    },
    getEvent: function(collectionUid, eventUid, transportInfo, handlerFunc){
           if (handlerFunc){
            return Cal.serv.getEvent(handlerFunc, collectionUid, eventUid);
        } else {
            return Cal.serv.getEvent(collectionUid, eventUid);
        }
    },
    saveEvent: function(collectionUid, eventUid, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.saveEvent(handlerFunc, collectionUid, eventUid);
        } else {
            return Cal.serv.saveEvent(collectionUid, eventUid);
        }
    },
    removeEvent: function(collectionUid, eventUid, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.removeEvent(handlerFunc, collectionUid, eventUid);
        } else {
            return Cal.serv.removeEvent(collectionUid, eventUid);
        }
    },
    getRecurrenceRules: function(collectionUid, eventUids, transportInfo, handlerFunc){
        if (handlerFunc){	
            return Cal.serv.getRecurrenceRules(handlerFunc, collectionUid, eventUids);
        } else {
            return Cal.serv.getRecurrenceRules(collectionUid, eventUids);    	
        }
    },
    saveRecurrenceRule: function(collectionUid, eventUid, recurrenceRule, 
        transportInfo, handlerFunc){
        if (handlerFunc){		
            return Cal.serv.saveRecurrenceRule(handlerFunc, collectionUid, eventUid, recurrenceRule);
        } else {
            return Cal.serv.saveRecurrenceRule(collectionUid, eventUid, recurrenceRule);    	
        }
    },
    expandEvents: function(collectionUid, eventUids, startTime, endTime, 
        transportInfo, handlerFunc){

        if (handlerFunc){		
            return Cal.serv.expandEvents(handlerFunc, collectionUid, eventUids, 
                startTime, endTime);
           } else {
            return Cal.serv.expandEvents(collectionUid, eventUids, 
                startTime, endTime);
           }
    },
    saveNewEventBreakRecurrence: function(collectionUid, event,
        originalEventUid, originalEventEndDate, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.saveNewEventBreakRecurrence(handlerFunc, collectionUid, event, 
                originalEventUid, originalEventEndDate);
        } else {
            return Cal.serv.saveNewEventBreakRecurrence(collectionUid, event, 
                originalEventUid, originalEventEndDate);
        }
    },
    
    saveDisplayName: function(collectionUid, newDisplayName, transportInfo, handlerFunc){
        if (handlerFunc){
            Cal.serv.saveDisplayName(handlerFunc, collectionUid, newDisplayName);
        } else {
            Cal.serv.saveDisplayName(collectionUid, newDisplayName);
        }
    }
};

cosmo.conduits.TicketedConduit =
{
    getCollection: function(collectionUid, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.getCalendar(handlerFunc, collectionUid, this.getTicket(transportInfo));
        } else {
            return Cal.serv.getCalendar(collectionUid, this.getTicket(transportInfo));
        }
    },
    getEvents: function(collectionUid, startTime, endTime, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.getEvents(handlerFunc, collectionUid, startTime, endTime, this.getTicket(transportInfo));
        } else {
            return Cal.serv.getEvents(collectionUid, startTime, endTime, this.getTicket(transportInfo));
        }
    },
    getEvent: function(collectionUid, eventUid, transportInfo, handlerFunc){
           if (handlerFunc){
            return Cal.serv.getEvent(handlerFunc, collectionUid, eventUid, this.getTicket(transportInfo));
        } else {
            return Cal.serv.getEvent(collectionUid, eventUid, this.getTicket(transportInfo));
        }
    },
    saveEvent: function(collectionUid, eventUid, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.saveEvent(handlerFunc, collectionUid, eventUid, this.getTicket(transportInfo));
        } else {
            return Cal.serv.saveEvent(collectionUid, eventUid, this.getTicket(transportInfo));
        }
    },
    removeEvent: function(collectionUid, eventUid, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.removeEvent(handlerFunc, collectionUid, eventUid, this.getTicket(transportInfo));
        } else {
            return Cal.serv.removeEvent(collectionUid, eventUid, this.getTicket(transportInfo));
        }
    },
    getRecurrenceRules: function(collectionUid, eventUids, transportInfo, handlerFunc){
        if (handlerFunc){	
            return Cal.serv.getRecurrenceRules(handlerFunc, collectionUid, eventUids, this.getTicket(transportInfo));
        } else {
            return Cal.serv.getRecurrenceRules(collectionUid, eventUids, this.getTicket(transportInfo));    	
        }
    },
    saveRecurrenceRule: function(collectionUid, eventUid, recurrenceRule, 
        transportInfo, handlerFunc){
        if (handlerFunc){		
            return Cal.serv.saveRecurrenceRule(handlerFunc, collectionUid, eventUid, recurrenceRule, this.getTicket(transportInfo));
        } else {
            return Cal.serv.saveRecurrenceRule(collectionUid, eventUid, recurrenceRule, this.getTicket(transportInfo));    	
        }
    },
    expandEvents: function(collectionUid, eventUids, startTime, endTime, 
        transportInfo, handlerFunc){

        if (handlerFunc){		
            return Cal.serv.expandEvents(handlerFunc, collectionUid, eventUids, 
                startTime, endTime, this.getTicket(transportInfo));
           } else {
            return Cal.serv.expandEvents(collectionUid, eventUids, 
                startTime, endTime, this.getTicket(transportInfo));
           }
    },
    saveNewEventBreakRecurrence: function(collectionUid, event,
        originalEventUid, originalEventEndDate, transportInfo, handlerFunc){
        if (handlerFunc){
            return Cal.serv.saveNewEventBreakRecurrence(handlerFunc, collectionUid, event, 
                originalEventUid, originalEventEndDate, this.getTicket(transportInfo));
        } else {
            return Cal.serv.saveNewEventBreakRecurrence(collectionUid, event, 
                originalEventUid, originalEventEndDate, this.getTicket(transportInfo));
        }
    }

};

cosmo.conduits.SubscriptionConduit = 
{
    getTicket: function(transportInfo){
        return transportInfo.ticket.ticketKey;
    },
    saveDisplayName: function(collectionUid, newDisplayName, transportInfo, handlerFunc){
        if (handlerFunc){
            Cal.serv.saveSubscription(handlerFunc, collectionUid, transportInfo.ticket.ticketKey, newDisplayName);
        } else {
            Cal.serv.saveSubscription(collectionUid, transportInfo.ticket.ticketKey, newDisplayName);
        }
    }
};
dojo.lang.mixin(cosmo.conduits.SubscriptionConduit, cosmo.conduits.TicketedConduit)

cosmo.conduits.AnonymousTicketedConduit =
{
    getTicket: function(transportInfo){
        return transportInfo.ticketKey;
    }

};
dojo.lang.mixin(cosmo.conduits.AnonymousTicketedConduit, cosmo.conduits.TicketedConduit)