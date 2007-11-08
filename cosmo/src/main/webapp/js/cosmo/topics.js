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

cosmo.topics.declareMessage = function (/*string*/ className,
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
    var o = {};
    // Accept keywords as an alternative to the param list
    if (arguments.length == 1) {
        o = arguments[0];
    }
    // Otherwise use normal parameter list in order
    else {
        o.className = className;
        o.topicName = topicName;
        o.superclass = superclass;
        o.initializer = initializer;
        o.props = props;
    }
    var con = dojo.declare(o.className,
        o.superclass || cosmo.topics.Message,
        o.initializer || null,
        o.props || null);
    con.topicName = o.topicName;
    con.prototype.topicName = o.topicName;
}


dojo.declare("cosmo.topics.Message", null, { topicName: null });

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
    function (/*cosmo.model.Collection*/ collection){
        this.collection = collection;
    },
    //props
    {
        collection: null
    }
);

cosmo.topics.declareMessage("cosmo.topics.CollectionDeletedMessage",
    // summary: Published after successful deletion of a collection
    //          has occured
    "COLLECTION_DELETED", null,
    //initializer
    function (/*cosmo.model.Collection*/ collection){
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
    function (/*cosmo.model.Subscription*/ subscription){
        this.subscription = subscription;
    },
    //props
    {
        subscription: null
    }
);

cosmo.topics.declareMessage("cosmo.topics.PreferencesUpdatedMessage",
    // summary: Published after preferences are changed
    //          has occured
    "PREFERENCES_UPDATED", null,
    //initializer
    function (/*Object*/ preferences){
        this.preferences = preferences;
    },
    //props
    {
        preferences: null
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
cosmo.topics.declareMessage({ className: "cosmo.topics.AppLevelMessage",
    topicName: '/app',
    props: { type: null }
});

cosmo.topics.declareMessage({ className: "cosmo.topics.ModalDialogToggle",
    // summary: published when the modal dialog box is toggled on or off
    superclass: cosmo.topics.AppLevelMessage,
    initializer: function (opts) {
        this.topicName = this.constructor.superclass.topicName;
        this.isDisplayed = opts.isDisplayed || false;
    },
    props: { isDisplayed: false,
        type: 'modalDialogToggle'  }
} );

// Other handy dandy functions

cosmo.topics.publish = function (/*Function*/messageConstructor,
    /*Array|Object*/initializerArg){
    // summary: create a message and publish it in one fell swoop.
    //     You don't specify a topic, because the topic
    //     used is the "topicName" property of the message object.
    //
    // messageConstructor: a constructor that creates a message
    //
    // initializerArguments: arguments to pass to the constructor/initializer
    if (!initializerArg){
        initializerArg = [];
    }
    var message = null;
    // Pass an Array -- note: instanceof fails in iframe for Array
    if (initializerArg instanceof Array) {
        message = new messageConstructor(initializerArg[0],
            initializerArg[1],
            initializerArg[2],
            initializerArg[3]);
    }
    // Pass an Object
    else {
        message = new messageConstructor(initializerArg);
    }
    dojo.event.topic.publish(message.topicName, message);
}
