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

/**
 * Data/Model level messages: These are messages that get published when data level events
 * happen, like an item was updated or deleted.  
 *
 */
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

/**
 * Application Level messages: Messages about changing the application state are to be found here. For
 * instance, the selected collection changed.
 */

//Nothing yet

/**
 * Lower Level UI messages: Messsages very specific to the UI - like screen resizes and whatnot
 */
 
cosmo.topics.declareMessage("cosmo.topics.ModalDialogDisplayed",
  // summary: published when the modal dialog box is displayed
  // description: UI elements who have parts which "shine through" floating divs
  //              which are placed above it in certain browsers (FF on Mac OS X for 
  //              instance) should subscribe to this message's topic so it can hide
  //              those parts.
  "MODAL_DIALOG_DISPLAYED",null,null,null
);

cosmo.topics.declareMessage("cosmo.topics.ModalDialogDismissed",
  // summary: published when the modal dialog box is no longer visible
  // description: any UI elements who were hiding certain elements (see
  //              cosmo.topics.ModalDialogDisplayed) can now show them
  "MODAL_DIALOG_DISMISSED",null,null,null
);

// Other handy dandy functions

cosmo.topics.publish = function(/*Function*/messageConstructor, /*Array*/initializerArguments){
   // summary: create a message and publish it in one fell swoop. You don't specify a topic, because the topic
   //          used is the "topicName" property of the message object.
   //
   // messageConstructor: a constructor that creates a message
   //
   // initializerArguments: arguments to pass to the constructor/initializer
   if (!initializerArguments){
       initializerArguments = [];
   }
   var message = new messageConstructor(initializerArguments[0],
                                        initializerArguments[1],
                                        initializerArguments[2], 
                                        initializerArguments[3]);
   dojo.event.topic.publish(message.topicName, message);
}