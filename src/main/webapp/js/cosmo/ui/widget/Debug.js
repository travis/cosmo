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

dojo.provide("cosmo.ui.widget.Debug");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");

dojo.widget.defineWidget("cosmo.ui.widget.Debug", dojo.widget.HtmlWidget, {

    templatePath : dojo.uri.dojoUri("../../cosmo/ui/widget/templates/Debug.html"),
    templateCssPath : dojo.uri.dojoUri("../../cosmo/ui/widget/templates/Debug.css"),
    time : "",
    debugDiv : null,
    contentContainer : null,

    fillInTemplate: function(){
        this.content.innerHTML = this.preWidgetContent;
        this.preWidgetContent = null;
    },

    clearContent : function(){
        this.content.innerHTML = "";
    },

    onkeypressHandler : function(event){
        var keyCode = event.keyCode ? event.keyCode : event.which;
        var ctrl = event.ctrlKey;
        var shift = event.shiftKey;
 	    var alt = event.altKey;
        
        //ctrl shift d
        if (keyCode == 68 && shift && (alt || ctrl)){
            this.toggleHideShow();
        }
    },
         
    toggleHideShow : function(){
        var vis = this.debugDiv.style.visibility;
        this.debugDiv.style.visibility = vis == "hidden" ? "visible" : "hidden";
    }
    
  },

  "html",
  
  function() {
     if (!djConfig.isDebug){
         return;
     }
     dojo.event.connect(window, "onkeypress", this, "onkeypressHandler");
     this.preWidgetContent = dojo.byId("dojoDebug").innerHTML;
  }

);
