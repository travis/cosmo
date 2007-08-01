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

/**
 * @fileoverview TicketWidget - interacts with a cosmo DAV service to create a new ticket
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.TicketWidget");

dojo.require("dojo.widget.*");
dojo.require("dojo.validate.*");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.auth");
dojo.require("dojo.lang");
dojo.require("dojo.io.*");
dojo.require("cosmo.convenience");

dojo.widget.defineWidget("cosmo.ui.widget.TicketWidget", dojo.widget.HtmlWidget, {

    templatePath: dojo.uri.dojoUri( "../../cosmo/ui/widget/templates/TicketWidget/TicketWidget.html"),

    ticketForm: null,
    timeoutErrorSpan: null,
    privilegesErrorSpan: null,

    itemId: "",

    createTicket: function(){
    	if (!this.validateInput()){
    		return;
    	}

		var timeout = this.ticketForm.timeout.value;

		if (timeout == ""){
			timeout = "Infinite";
		} else {
			timeout = "Second-" + timeout;
		}

		var privs = this.ticketForm.privileges;
		var privString = "";

		for (var i = 0; i < privs.length; i++){
			if (privs[i].checked){
				switch(privs[i].value){
					case 'ro':
						privString = '<D:read/>'
						break;
					case 'rw':
						privString = '<D:read/><D:write/>'
						break;
					case 'fb':
						privString = '<D:freebusy/>'
						break;
				}

				break;
			}

		}

		var content = '<?xml version="1.0" encoding="utf-8" ?>';
		content += '<ticket:ticketinfo xmlns:D="DAV:" ';
		content += 'xmlns:ticket="http://www.xythos.com/namespaces/StorageServer">';
   		content += '<D:privilege>' + privString + '</D:privilege>';
   		content += '<ticket:timeout>' + timeout + '</ticket:timeout>';
		content += '</ticket:ticketinfo>';
		var request = cosmo.util.auth.getAuthorizedRequest()
		dojo.lang.mixin(request,
		 {
		    load: this.createSuccess,
            error: this.createFailure,
            transport: "XMLHTTPTransport",
            contentType: 'text/xml',
			postContent: content,
			method:  "POST",


            url: cosmo.env.getBaseUrl() + "/dav" + this.itemId

        	}
        );
        request.headers['X-Http-Method-Override'] =  "MKTICKET";


	    dojo.io.bind(request);

    	},

   	createSuccess: function(type, data, evt){
   	},
   	createFailure: function(type, error){
		alert("Ticket not created. Error: " + error.message);
   	},

   	validateInput: function(){
   		var timeout = this.ticketForm.timeout.value;
    	var timeoutValid = timeout == "" ||
    		dojo.validate.isInteger(timeout);

   		var privs = this.ticketForm.privileges;
   		var privsSelected = false;

   		for (var i = 0; i < privs.length; i++){
   			if (privs[i].checked){
   				privsSelected = true;
   				break;
   			}
   		}

   		if (!privsSelected){
			this.privilegesErrorSpan.innerHTML = _('Ticket.Error.Privilege');
		}
		if (!timeoutValid){
			this.timeoutErrorSpan.innerHTML = _('Ticket.Error.Timeout');
		}

		return timeoutValid && privsSelected;


   	},

   	postCreate: function(){
		this.ticketForm.privileges[0].checked = true;


   	}


  } );
