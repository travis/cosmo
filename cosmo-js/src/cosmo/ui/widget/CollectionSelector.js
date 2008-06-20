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

dojo.provide("cosmo.ui.widget.CollectionSelector");
dojo.require("dojox.color");
dojo.require("dijit._Templated");
dojo.require("cosmo.ui.ContentBox"); // legacy, deprecate soon
dojo.require("cosmo.topics");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.data.CollectionStore");
dojo.require("cosmo.data.SubscriptionStore");
dojo.require("cosmo.ui.widget.SharingDialog");
dojo.require("cosmo.app.pim");

dojo.requireLocalization("cosmo.ui.widget", "CollectionSelector");
(function(){
function getRGB(h, s, v){
    var rgb = dojox.color.fromHsv(h, s, v).toRgb();
    return 'rgb(' + rgb.join() + ')';
}

var collectionNameMarkup = "<div id='collectionSelectorItemSel_${uid}' dojoAttachPoint='nameCell' class='collectionSelectorCollectionName'>${displayName}</div>";
var collectionDropdownMarkup = "<div dojoAttachPoint='dropdown' dojoAttachEvent='onclick: handleDropdownClick, onmouseover: handleDropdownMouseOver, onmouseout: handleDropdownMouseOut' id='collectionSelectorItemDetails_${uid}' class='collectionSelectorDetails' style='background-color: ${defaultHueString}'><div class='cosmoCollectionDetails'></div></div>";
var selectorAttachEvents = " dojoAttachEvent='onmouseover: handleNameMouseOver, onmouseout: handleNameMouseOut, onclick: handleSelectionClick' ";
dojo.declare("cosmo.ui.widget._BaseSelector", [dijit._Widget, dijit._Templated],
{
    templateString: "<div class='cosmoCollectionSelectorSection' " + selectorAttachEvents + ">" + collectionNameMarkup + collectionDropdownMarkup + "</div>",
    hue: null,
    collection: null,
    store: null,

    nameCell: null,
    selectionSubscription: null,
    dropdown: null,

    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "CollectionSelector");
    },

    postMixInProperties: function(){
        this.hue = this.hue || this.collection.hue;
        this.defaultHueString = getRGB(this.hue, 80, 90);
        if (this.collection instanceof cosmo.model.Subscription)
            this.store = new cosmo.data.SubscriptionStore(cosmo.app.pim.serv);
        else if (this.collection instanceof cosmo.model.Collection)
            this.store = new cosmo.data.CollectionStore(cosmo.app.pim.serv);
        this.uid = this.store.getValue(this.collection, "uid");
        this.displayName = this.store.getValue(this.collection, "displayName");
    },

    postCreate: function(){
        this.selectionSubscription = dojo.subscribe("cosmo:selectionChanged", this, "handleSelectionChanged");
        if (cosmo.app.pim.getSelectedCollection() == this.collection) dojo.publish("cosmo:selectionChanged", [this.collection, this.store]);
    },

    destroy: function(){
        dojo.unsubscribe(this.selectionSubscription);
        this.inherited("destroy", arguments);
    },

    setSelected: function(){
        dojo.publish("cosmo:selectionChanged", [this.collection, this.store]);
        cosmo.view.displayViewFromCollections(this.collection);
    },

    handleSelectionChanged: function(selection){
        if (selection != this.collection){
            this.collection.doDisplay = false;
            dojo.removeClass(this.domNode, "collectionSelectorSel");
        } else {
            this.collection.doDisplay = true;
            dojo.addClass(this.domNode, "collectionSelectorSel");
        }
    },

    handleDropdownMouseOver: function(e){
        this.dropdown.style.backgroundColor = getRGB(this.hue, 50, 100);
    },

    handleDropdownMouseOut: function(e){
        this.dropdown.style.backgroundColor = this.defaultHueString;
    },

    handleNameMouseOver: function(e){
        dojo.addClass(this.domNode, "mouseoverItem");
    },

    handleNameMouseOut: function(e){
        dojo.removeClass(this.domNode, "mouseoverItem");
    },

    handleSelectionClick: function(){
        this.setSelected();
    },

    handleDropdownClick: function(){
        var dialog = new cosmo.ui.widget.SharingDialog(
            {
                store: this.store,
                collection: this.collection,
                xhrArgs: cosmo.util.auth.getAuthorizedRequest(),
                id: "collectionSharingDialog"
            });
        cosmo.app.pim.baseLayout.mainApp.leftSidebar.addChild(dialog);

    }
});

var collectionCheckboxMarkup = "<div class='collectionSelectorCheckbox'><input type='checkbox' id='collectionSelectorItemCheck_${uid}' ${checkedAttribute} dojoAttachEvent='onclick: handleCheckboxClick'/></div>";

dojo.declare("cosmo.ui.widget._CalViewSelector", cosmo.ui.widget._BaseSelector,
{
    templateString: "<div class='cosmoCollectionSelectorSection cosmoCollectionSelectorCalSelect' " + selectorAttachEvents + ">" + collectionCheckboxMarkup + collectionNameMarkup + collectionDropdownMarkup + "</div>",
    postMixInProperties: function(){
        this.inherited("postMixInProperties", arguments);
        this.checkedAttribute = this.collection.isOverlaid? "checked='checked'" : "";
    },
    handleSelectionChanged: function(selection, store){
        if (selection != this.collection){
            dojo.removeClass(this.domNode, "collectionSelectorSel");
            if (!this.collection.isOverlaid)
                this.collection.doDisplay = false;
        } else {
            this.collection.doDisplay = true;
            dojo.addClass(this.domNode, "collectionSelectorSel");
        }

    },
    handleCheckboxClick: function(e){
        var checked = e.target.checked;
        this.collection.doDisplay = checked;
        this.collection.isOverlaid = checked;
        if (cosmo.app.pim.getSelectedCollection() != this.collection) cosmo.view.displayViewFromCollections();
    }
});

dojo.declare("cosmo.ui.widget._ListViewSelector", cosmo.ui.widget._BaseSelector,
{
    templateString: "<div class='cosmoCollectionSelectorSection cosmoCollectionSelectorListSelect'  " + selectorAttachEvents + ">" + collectionNameMarkup + collectionDropdownMarkup + "</div>"
});

dojo.declare("cosmo.ui.widget.CollectionSelector", [dijit._Widget, dijit._Templated, cosmo.ui.ContentBox],
{
    containerNode: null,
    noCollectionsPrompt: null,
    view: null,
    //cosmo.ui.widget._BaseSelector
    selection: null,
    selectionStore: null,
    templatePath: dojo.moduleUrl("cosmo", 'ui/widget/templates/CollectionSelector.html'),

    selectors: {cal: cosmo.ui.widget._CalViewSelector,
                list: cosmo.ui.widget._ListViewSelector
               },

    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "CollectionSelector");
    },

    postCreate: function(){
        var r = dojo.hitch(this, function(){this.render();});
        dojo.subscribe('cosmo:calEventsLoadSuccess', r);
        dojo.subscribe(cosmo.topics.CollectionUpdatedMessage.topicName, r);
        dojo.subscribe(cosmo.topics.SubscriptionUpdatedMessage.topicName, r);
        dojo.subscribe('cosmo:selectionChanged', this, "handleSelectionChanged");
    },

    handleSelectionChanged: function(selection, store){
        this.selection = selection;
        this.selectionStore = store;
    },

    renderSelf: function(){
        this.view = cosmo.app.pim.currentView;
        this.destroyDescendants();
        this.populateChildrenFromCollections(cosmo.app.pim.collections);
    },

    showPrompt: function(){
        this.noCollectionsPrompt.style.display = "block";
    },

    hidePrompt: function(){
        this.noCollectionsPrompt.style.display = "none";
    },

    populateChildrenFromCollections: function(collections){
        if (collections.length > 0) {
            collections.each(dojo.hitch(this, this.addCollection));
            this.hidePrompt();
        } else this.showPrompt();
    },

    addCollection: function(collectionKey, collection){
        var selector = new this.selectors[this.view]({collection: collection});
        this.containerNode.appendChild(selector.domNode);
        this.hidePrompt();
    },
    handleNewCollection: function(){
        var collections = cosmo.app.pim.collections;
        var collectionNameDeferred =
            cosmo.app.getValue(
                _("Main.NewCollection.NamePrompt"),
                _("Main.NewCollection.DefaultName"),
                [function(name){
                     for (var i = 0; i < collections.length; i++){
                         if (name == collections.getAtPos(i).getDisplayName()){
                             return _("Main.NewCollection.NameInUse");
                         }
                     }
                 }],
                 { defaultActionButtonText: _('App.Button.Save'),
                   showCancel: true }
            );
        collectionNameDeferred.addCallback(function(name){
            cosmo.app.modalDialog.setPrompt(_('App.Status.Processing'));
            var createDeferred = cosmo.app.pim.serv.createCollection(name);
            createDeferred.addCallback(function(result){
                //TODO: This is bad. Giant waste of bandwidth.
                // We can fix this by returning a collection from a create request.
                // On the plus side, most of the collections should be cached since
                // we already had them loaded.
                return cosmo.app.pim.reloadCollections();
            });
            createDeferred.addBoth(function(){
                var f = function () {
                    cosmo.topics.publish(cosmo.topics.CollectionUpdatedMessage);
                };
                setTimeout(f, 0);
                cosmo.app.hideDialog();
            });
        });
    },
    handleDeleteCollection: function(){
        var displayName = this.selectionStore.getValue(this.selection, "displayName");
        var confirmDeleteMessage = dojo.string.substitute(this.selectionStore.l10n.confirmDelete, {collectionName: displayName});
        var d = cosmo.app.confirm(confirmDeleteMessage, {cancelDefault: true});
        d.addCallback(dojo.hitch(this, function(confirmed){
            if (confirmed){
                this.selectionStore.deleteItem(this.selection);
                var sd = this.selectionStore.save();
                sd.addErrback(function(e){
                    cosmo.app.showErr(
                        dojo.string.substitute(this.l10n.deleteFailed,
                                               {collectionName: displayName}),
                        e.message, e);
                    return e;
                });
                sd.addCallback(dojo.hitch(this, function(){
                    var deleteId = this.selectionStore.getValue(this.selection, "uid");
                    cosmo.app.pim.collections.removeItem(deleteId);
                    delete cosmo.view.cal.collectionItemRegistries[deleteId];
                    var reloadDeferred = cosmo.app.pim.reloadCollections({
                        removedCollection: this.selection,
                        removedByThisUser: true });
                    reloadDeferred.addCallback(function(){
                        cosmo.topics.publish(cosmo.topics.CollectionUpdatedMessage);
                    });
                    return reloadDeferred;
                }));
                return sd;
            } else return false;
        }));
        return d;

    }
});
})();