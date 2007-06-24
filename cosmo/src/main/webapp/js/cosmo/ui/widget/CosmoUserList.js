/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
 * @fileoverview CosmoList - a list of cosmo users that speaks CMP
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.CosmoUserList");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.dom");
dojo.require("dojo.date.serialize");
dojo.require("dojo.uri.Uri");

dojo.require("cosmo.env");
dojo.require("cosmo.cmp");
dojo.require("cosmo.convenience");

dojo.require("dojo.widget.FilteringTable");


dojo.widget.defineWidget("cosmo.ui.widget.CosmoUserList", dojo.widget.FilteringTable,
    {
        resourceDirectory : cosmo.env.getTemplateBase() + "CosmoUserList/",

        sortOrder : null,
        sortType : null,

        pageNumber : 1,
        pageSize : 25,
        query: "",

        cmpFirstLink : null,
        cmpPreviousLink : null,
        cmpNextLink : null,
        cmpLast : null,
        userCountIndicator: null,

        orderIndicator : null,

        ASCENDING : "ascending",
        DESCENDING : "descending",
        DEFAULT_SORT_TYPE : "username",
        DEFAULT_SORT_ORDER : "ascending",
        
        PARAM_PAGE_SIZE: 'ps',
        PARAM_PAGE_NUMBER: 'pn',
        PARAM_SORT_ORDER: 'so',
        PARAM_SORT_TYPE: 'st',
        PARAM_QUERY: 'q',
        
        createOrderIndicator : function(){
            var node = document.createElement("img")
            node.setAttribute("id", "orderIndicator")

            resourceDirectory = this.resourceDirectory

            node.setAscending = function(){
                node.setAttribute("src", resourceDirectory + "ascending.png");
                node.setAttribute("alt", " v");
            }
            node.setDescending = function(){
                node.setAttribute("src", resourceDirectory + "descending.png");
                node.setAttribute("alt", " ^");
            }

            node.setAscending();

            return node;
        },

        setSortOrder : function(order){
            if (order == this.DESCENDING){
                this.sortOrder = order;
                this.orderIndicator.setDescending();
            } else if (order == this.ASCENDING){
                this.sortOrder = order;
                this.orderIndicator.setAscending();
            }
        },

        setSortType : function(type){
            this.sortType = type;

            var header;

            var tableHeaders = this.domNode.getElementsByTagName("th");
            for (i = 0; i < tableHeaders.length; i++){
                if (tableHeaders[i].getAttribute("field") == type){
                    header = tableHeaders[i];
                }
            }

            if (!header){
                alert("Could not find " + type + "column");
            } else {
                header.appendChild(this.orderIndicator);
            }
        },

        deleteSelectedUsers: function(){
            var users = this.getSelectedData();
            var usernames = []

            for (var i=0; i<users.length; i++){

                usernames.push(users[i].username)
            }
            var self = this;

            cosmo.cmp.deleteUsers(usernames,
                {load: function(type, data, evt){self.updateUserList();},
                 error: function(type, error){alert("Could not delete user:" + error)}
                }
                );
        },

        loadCMPPage:function(cmpUrl){
            if (cmpUrl){
                var documentAddress = new dojo.uri.Uri(cmpUrl);
    
                var query = documentAddress.query;
    
                var vars = query.split("&");
    
                for (var i=0; i < vars.length; i++){
                    var pair = vars[i].split("=")
                    switch(pair[0]){
                        case(this.PARAM_PAGE_SIZE):
                            this.pageSize = pair[1];
                            break;
                        case(this.PARAM_PAGE_NUMBER):
                            this.pageNumber = pair[1];
                            break;
                        case(this.PARAM_SORT_ORDER):
                            this.setSortOrder(pair[1]);
                            break;
                        case(this.PARAM_SORT_TYPE):
                            this.setSortType(pair[1]);
                            break;
                        case(this.PARAM_QUERY):
                            this.query = pair[1];
                            break;
                        }
                }
            }
            this.updateUserList()
        },
        
        createCurrentUrlQuery: function (){
            var queryList = []
            
            if (this.pageSize) queryList.push(this.PARAM_PAGE_SIZE + "="  + this.pageSize);
            if (this.pageNumber) queryList.push(this.PARAM_PAGE_NUMBER + "=" + this.pageNumber);
            if (this.sortOrder) queryList.push(this.PARAM_SORT_ORDER + "=" + this.sortOrder);
            if (this.sortType) queryList.push(this.PARAM_SORT_TYPE + "=" + this.sortType);
            if (this.query) queryList.push(this.PARAM_QUERY + "=" + this.query)
            
            return "?" + queryList.join("&");
        },

        createPagingLink:function(label, id, jsText){
            var a = document.createElement("a");
            a.setAttribute("class", "userListPagingLink");
            a.setAttribute("id", id);
            a.setAttribute("href", "javascript:void(0)");
            a.style.visibility = "hidden";

            dojo.event.connect(a, "onclick", this, jsText);

            var t =	document.createTextNode(label);
            a.appendChild(t);
            return a;
        },

        createPageSizeChooser:function(){

            var s = document.createElement("span");

            s.appendChild(document.createTextNode(_("UserList.UsersPerPage")));
            s.setAttribute("id", "pageSizeChooser");

            var i = document.createElement("input");
            i.setAttribute("type", "text");
            i.setAttribute("size", "4");
            i.setAttribute("maxlength", "4");
            i.setAttribute("value", this.pageSize);
            i.setAttribute("align", "middle");

            dojo.event.connect(i,"onchange", dojo.lang.hitch(this,
                    function (){
                        if (i.value <= 0 ||
                            i.value % 1 != 0){
                            alert("Page size cannot be " + i.value + ".");
                            i.value = this.pageSize;
                            return;
                        }
                        this.pageSize = i.value;
                        this.pageNumber = 1;
                        this.updateUserList();
                    }));

            s.appendChild(i);

            var b = document.createElement("input");
            b.setAttribute("type", "button");
            b.setAttribute("size", "4");
            b.setAttribute("maxlength", "4");
            b.setAttribute("value", _("UserList.Control.ApplyPageSize"));
            b.setAttribute("align", "middle");

            b.onclick = function(){i.value = i.value; return false};

            s.appendChild(b);

            return s;
        },

        createSearchBox:function(){

            var s = document.createElement("span");

            s.setAttribute("id", "searchBox");

            var i = document.createElement("input");
            i.setAttribute("type", "text");
            i.setAttribute("size", "15");
            i.setAttribute("maxlength", "100");
            i.setAttribute("value", this.query);
            i.setAttribute("align", "left");

            dojo.event.connect(i,"onchange", dojo.lang.hitch(this,
                    function (){
                        this.query = i.value;
                        this.updateUserList();
                    }));

            s.appendChild(i);

            var b = document.createElement("input");
            b.setAttribute("type", "button");
            b.setAttribute("size", "4");
            b.setAttribute("maxlength", "4");
            b.setAttribute("value", _("UserList.Control.Search"));
            b.setAttribute("align", "middle");

            b.onclick = dojo.lang.hitch(this, function(){
                this.query = i.value;
                this.updateUserList();
                return false
            });

            s.appendChild(b);

            return s;
        },
        
        createPageNumberChooser:function(){

            var s = document.createElement("span");

            s.setAttribute("id", "pageNumberChooser");
            s.style.visibility = "hidden";

            s.appendChild(this.createPagingLink(" << ", "firstPageLink","loadFirstPage"));
            s.appendChild(this.createPagingLink(" < ", "previousPageLink", "loadPreviousPage"));

            s.appendChild(document.createTextNode("Go to page: "));

            var i = document.createElement("input");
            i.setAttribute("type", "text");
            i.setAttribute("size", "2");
            i.setAttribute("maxlength", "10");
            i.setAttribute("value", this.pageNumber);
            i.setAttribute("align", "middle");

            dojo.event.connect(i, "onchange", dojo.lang.hitch(this,
               function (){
                  this.pageNumber = i.value;
                  this.updateUserList();
               })
            );

            s.appendChild(i);

            var b = document.createElement("input");
            b.setAttribute("type", "button");
            b.setAttribute("size", "4");
            b.setAttribute("maxlength", "4");
            b.setAttribute("value", _("UserList.Control.ApplyPageNumber"));
            b.setAttribute("align", "middle");

            b.onclick = function(){i.value = i.value; return false};

            s.appendChild(b);

            s.appendChild(this.createPagingLink(" > ", "nextPageLink", "loadNextPage"));
            s.appendChild(this.createPagingLink(" >> ", "lastPageLink", "loadLastPage"));

            return s;
        },
        
        createStateLink:function(){

            var s = document.createElement("a");

            s.setAttribute("id", "stateLink");
            s.setAttribute("href", "");
            s.innerHTML = _("UserList.StateLink");
            
            this.stateLink = s;
            
            return s;
        },


        createUserCountIndicator: function(){
        	var s = document.createElement("span");
        	s.setAttribute("id", "userCountIndicatorSpan");

			var count = document.createElement("span");

			this.userCountIndicator = count;

			s.appendChild(document.createTextNode(_("UserList.TotalUsers")));
			s.appendChild(count);

			this.updateTotalUserCount();
        	return s;
        },

        updateTotalUserCount: function(){
        	var self = this;
	       	var setCountCallback = function (type, data, evt){
				self.userCountIndicator.innerHTML = data;
    	   	}

        	cosmo.cmp.getUserCount({
        		load: setCountCallback,
        		error: function(type, error){
        			alert('Could not get user count: ' + error.message);
        		}
        	});

        },

        loadFirstPage:function(){
            if (this.cmpFirstLink){
                this.loadCMPPage(this.cmpFirstLink);
            }
        },

        loadPreviousPage:function(){
            if (this.cmpPreviousLink){
                this.loadCMPPage(this.cmpPreviousLink);
            }
        },

        loadNextPage:function(){
            if (this.cmpNextLink){
                this.loadCMPPage(this.cmpNextLink);
            }
        },

        loadLastPage:function(){
            if (this.cmpLastLink){
                this.loadCMPPage(this.cmpLastLink);
            }
        },

        updatePageNumber:function(page){
            this.pageNumber = page;
            this.updateUserList();
        },

        updatePageSize:function(size){
            this.pageSize = size;
            this.updateUserList();
        },
        
        updateQuery: function(query){
            this.query = query;
            this.updateUserList();  
        },

        updateControlsView: function(){
            document.getElementById("pageSizeChooser").
                getElementsByTagName("input")[0].value = this.pageSize;

            document.getElementById("pageNumberChooser").
                getElementsByTagName("input")[0].value = this.pageNumber;
            
            document.getElementById("searchBox").
                getElementsByTagName("input")[0].value = this.query;

           	this.updateTotalUserCount();

        },

        updateUserListCallback:function(data, evt){
            var cmpXml = evt.responseXML;

            this.updateControlsView();

            var jsonObject = [];

            var users = data;

            for (i = 0; i < users.length; i++){

                var user = users[i];

                var row = {};

                row.email = user.email;
                row.name = user.firstName + " " + user.lastName;
                row.username = user.username;

                row.created = dojo.date.fromRfc3339(user.dateCreated);

                row.modified = dojo.date.fromRfc3339(user.dateModified);

                if (user.unactivated) {
                	row.activated = _("UserList.No");
                } else {
                	row.activated = _("UserList.Yes");
                }

                if (user.administrator) {
                    row.admin = _("UserList.Yes");
                } else {
                    row.admin = _("UserList.No");
                }

                if (user.locked) {
                    row.locked = _("UserList.Yes");
                } else {
                    row.locked = _("UserList.No");
                }

                row.userObject = user;

                jsonObject.push(row);
            }

            var pagingLinks ;

            // Non-NS version of getElementsByTagName doesn't return link elements in Safari.
            if (dojo.render.html.safari){
               pagingLinks = cmpXml.getElementsByTagNameNS('*', "link");
            } else {
               pagingLinks = cmpXml.getElementsByTagName("link");
            }
            this.cmpFirstLink = null;
            this.cmpPreviousLink = null;
            this.cmpNextLink = null;
            this.cmpLastLink = null;

            for (var i=0; i< pagingLinks.length; i++){

                link = pagingLinks[i]
                // Apparently, Safari has issues with the $amp; entity. Instead
                // of converting it to &, (like it converts %lt; to <), it converts it to
                // &#38; It appears we can fix this by turning it into a DOM node first and getting
                // the value of that node.
                // Since this only appears to be an issue for &amp;, I'm putting the fix here.
                // If we find similar bugs in the future we may want to generalize this.
                // -travis@osafoundation.org
                var url = //link.getAttribute("href");
                dojo.html.createNodesFromText(link.getAttribute("href"))[0].nodeValue;

                //url = url.replace(/&#38;/g, "&");

                switch(link.getAttribute("rel")){
                    case 'first':
                        this.cmpFirstLink = url;
                        break;
                    case 'previous':
                        this.cmpPreviousLink = url;
                        break;
                    case 'next':
                        this.cmpNextLink = url;
                        break;
                    case 'last':
                        this.cmpLastLink = url;
                        break;
                }
            }

            var multiPage = (this.cmpPreviousLink || this.cmpNextLink)

            document.getElementById("firstPageLink").style.visibility =
                (this.cmpFirstLink && multiPage) ? 'visible' : 'hidden';

            document.getElementById("previousPageLink").style.visibility =
                (this.cmpPreviousLink) ? 'visible' : 'hidden';

            document.getElementById("nextPageLink").style.visibility =
                (this.cmpNextLink) ? 'visible' : 'hidden';

            document.getElementById("lastPageLink").style.visibility =
                (this.cmpLastLink && multiPage) ? 'visible' : 'hidden';

            document.getElementById("pageNumberChooser").style.visibility =
                (multiPage) ? 'visible' : 'hidden';
                
            this.stateLink.href = this.createCurrentUrlQuery();

            this.store.setData(jsonObject);

        },

        updateUserList: function(){

            var self = this;

            cosmo.cmp.getUsers({
                load: function(type, data, evt){self.updateUserListCallback(data, evt)},
                 error: function(type, error){alert("Could not update user list:" + error.message)}
                 },
                 this.pageNumber,
                 this.pageSize,
                 this.sortOrder,
                 this.sortType,
                 this.query);
        },

        // These two functions will disable client side sorting.
        createSorter : function(x){return null},

        onSort:function(/* DomEvent */ e){
            this.pageNumber = 1;

            var sortType = e.currentTarget.getAttribute("field");

            if (this.sortType == sortType){
                if (this.sortOrder == this.ASCENDING){
                    this.setSortOrder(this.DESCENDING);
                } else {
                    this.setSortOrder(this.ASCENDING);
                }
            } else if (sortType) {
                this.setSortType(sortType);
                e.currentTarget.appendChild(this.orderIndicator);
            }

            this.updateUserList();
        }

    },
    "html",
    function(){
        var self = this;

        //dojo.widget.html.SortableTable.call(this);
        this.widgetType="CosmoUserList";

        this.orderIndicator = this.createOrderIndicator();

        this.userListPostCreate = function(){

            this.setSortType(this.DEFAULT_SORT_TYPE);
            this.setSortOrder(this.DEFAULT_SORT_ORDER);

            var table = this.domNode;

            var controls = document.createElement("table");
            controls.setAttribute("id", "userListControls");
            table.parentNode.insertBefore(controls, table);
            controls.style.width = '100%';
            var tableBody = document.createElement("tbody");
            controls.appendChild(tableBody);
            tableBody.style.width = '100%';
            var controlsRow = document.createElement("tr");
            tableBody.appendChild(controlsRow);

            var td;

            td = document.createElement("td");
            td.setAttribute("id", "searchBoxContainer");
            td.appendChild(this.createSearchBox());
            controlsRow.appendChild(td);

            td = document.createElement("td");
            td.setAttribute("id", "stateLinkContainer");
            td.appendChild(this.createStateLink());
            controlsRow.appendChild(td);

            td = document.createElement("td");
            td.setAttribute("id", "userCountContainer");
            td.appendChild(this.createUserCountIndicator());
            controlsRow.appendChild(td);

            td = document.createElement("td");
            td.setAttribute("id", "pageNumberChooserContainer");
            td.appendChild(this.createPageNumberChooser());
            controlsRow.appendChild(td);

            td = document.createElement("td");
            td.setAttribute("id", "pageSizeChooserContainer");
            td.appendChild(this.createPageSizeChooser());
            controlsRow.appendChild(td);

            dojo.event.topic.registerPublisher("/userListSelectionChanged", this, "renderSelections");
            
            this.loadCMPPage(location.search);

        }
        dojo.event.connect("after", this, "postCreate", this, "userListPostCreate");

		this.aroundCreateRow = function (invocation){
			var user = invocation.args[0].src;
			var row = invocation.proceed();
			row.id = user.username + "Row";
			return row;

		};
        dojo.event.connect("around", this, "createRow", this, "aroundCreateRow");

    }
);
