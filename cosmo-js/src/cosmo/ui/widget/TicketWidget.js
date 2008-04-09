if(!dojo._hasResource["cosmo.ui.widget.TicketWidget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.ui.widget.TicketWidget"] = true;
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

dojo.require("dojo.number");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Form");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");

dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.convenience");

dojo.requireLocalization("cosmo.ui.widget", "TicketWidget", null, "ROOT");

dojo.declare("cosmo.ui.widget.TicketWidget", [dijit._Widget, dijit._Templated], {
    templateString:"<div>\n<form dojoType=\"dijit.form.Form\" dojoAttachEvent=\"execute: execute\" dojoAttachPoint=\"form\">\n  <span>\n    ${l10n.ticket}\n  </span>\n  \n  <span>\n    <input dojoType=\"dijit.form.ValidationTextBox\" regExp=\"${timeoutRE}\" name=\"timeout\" invalidMessage=\"${l10n.ticketInvalid}\"/>\n    ${l10n.ticketSeconds}\n  </span>\n  <span>\n    ${l10n.privileges}\n  </span>\n  <span>\n    <input type=\"radio\" dojoType=\"dijit.form.RadioButton\" \n           name=\"privileges\" value=\"ro\" id=\"${id}ReadRadio\" checked=\"true\"/> ${l10n.read}\n    <input type=\"radio\" dojoType=\"dijit.form.RadioButton\" \n           name=\"privileges\" value=\"rw\" id=\"${id}ReadWriteRadio\"/> ${l10n.readWrite}\n    <input type=\"radio\" dojoType=\"dijit.form.RadioButton\" \n           name=\"privileges\" value=\"fb\" id=\"${id}FreeBusyRadio\"/> ${l10n.freeBusy}\n  </span>\n  <button styleClass=\"buttonInput\"  type=\"submit\" name=\"submit\" id=\"${id}GrantTicketButton\" dojoType=\"dijit.form.Button\">\n    ${l10n.grantTicket}\n  </button>\n</span>\n</form>\n</div>\n",

    widgetsInTemplate: true,
    itemId: "",
    timeoutRE: dojo.number._integerRegexp(),

    privDict: {'ro': '<D:read/>',
            'rw': '<D:read/><D:write/>',
            'fb': '<D:freebusy/>'},

    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "TicketWidget");
    },

    execute: function(form){
        if (this.form.isValid()){
		    var timeout = form.timeout;
            
		    timeout = timeout? "Second-" + timeout: "Infinite";
            
		    var content = ['<?xml version="1.0" encoding="utf-8" ?>',
		        '<ticket:ticketinfo xmlns:D="DAV:" ',
		        'xmlns:ticket="http://www.xythos.com/namespaces/StorageServer">',
   		        '<D:privilege>', this.privDict[form.privileges], '</D:privilege>',
   		        '<ticket:timeout>', timeout, '</ticket:timeout>',
		        '</ticket:ticketinfo>'].join("");
		    var request = cosmo.util.auth.getAuthorizedRequest()
		    dojo.mixin(request,
		               {
                           contentType: 'text/xml',
			               postData: content,
                           url: cosmo.env.getBaseUrl() + "/dav" + this.itemId
        	           }
                      );
            request.headers['X-Http-Method-Override'] =  "MKTICKET";
            var d = dojo.rawXhrPost(request);
		    d.addCallback(dojo.hitch(this, this.createSuccess));
            d.addErrback(dojo.hitch(this, this.createFailure));
            return d;
        }
    },

   	createSuccess: function(data){
   	},

   	createFailure: function(error){
		alert("Ticket not created. Error: " + error.message);
   	}
});

}
