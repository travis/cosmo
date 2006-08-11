dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");

dojo.provide("scooby.ui.widget.Debug");

dojo.widget.defineWidget("scooby.ui.widget.Debug", dojo.widget.HtmlWidget, {

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
        
        //ctrl shift d
        if (keyCode == 68 && shift && ctrl){
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