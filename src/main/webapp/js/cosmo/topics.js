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

/**
 * summary:
 *     This is where all core Cosmo topics and topic messages should be defined.
 */
dojo.provide("cosmo.topics");

dojo.require("dojo.event.topic");

cosmo.topics.declareMessage = function(/*string*/ className,
                                       /*string*/ topicName,
                                       /*Function|Array*/ superclass, 
                                       /*Function*/ initializer, 
                                       /*Object*/ props) {
    /**
     * summary: convienience method for declaring new messages.
     * description: You call this the same way you call "dojo.declare" except
     *              this doesn't do any argument juggling - you must specify 
     *              the arguments in the order listed above. If you specify "null" 
     *              as the superclass, "cosmo.topics.Message" is assumed.
     *              
     *              The topicName property is added to the prototype as well
     *              as the constructor object so you can easily get the topicName 
     *              from an instance or from "cosmo.topics.MyNewMessage.topicName" 
     */                
    var con = dojo.declare(className, superclass || cosmo.topics.Message, initializer, props);
    con.topicName = topicName;
    con.prototype.topicName = topicName;                                 
}


dojo.declare("cosmo.topics.Message", null, { topicName: null});

cosmo.topics.declareMessage("cosmo.topics.CollectionUpdatedMessage", 
    // summary: Published after successful updating of a collection to the server
    //          has occured
    "COLLECTION_UPDATED", null, 
    //initializer
    function(/*cosmo.model.CalendarMetadata*/ collection){
        this.collection = collection;
    },
    //props 
    {
        collection: null
    }
);

cosmo.topics.declareMessage("cosmo.topics.SubscriptionUpdatedMessage", 
    // summary: Published after successful updating of a subscription to the server
    //          has occured
    "SUBSCRIPTION_UPDATED", null, 
    //initializer
    function(/*cosmo.model.Subscription*/ subscription){
        this.subscription = subscription;
    },
    //props 
    {
        subscription: null
    }
);

cosmo.topics.publish = function(/*Function*/messageConstructor, /*Array*/initializerArguments){
   // summary: create a message and publish it in one fell swoop. You don't specify a topic, because the topic
   //          used is the "topicName" property of the message object.
   //
   // messageConstructor: a constructor that creates a message
   //
   // initializerArguments: arguments to pass to the constructor/initializer
   var message = {};
   message.__proto__ = messageConstructor.prototype;
   messageConstructor.apply(message, initializerArguments);
   message.constructor = messageConstructor;
   dojo.event.topic.publish(message.topicName, message);
}