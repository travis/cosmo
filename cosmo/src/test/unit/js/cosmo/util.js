dojo.provide("cosmotest.util");

dojo.require("cosmo.cmp");
dojo.require("cosmo.util.auth");

cosmotest.util = {
    toXMLDocument: function (/*String*/ text){
        if (window.ActiveXObject)
          {
          var doc=new ActiveXObject("Microsoft.XMLDOM");
          doc.async="false";
          doc.loadXML(text);
          }
        // code for Mozilla, Firefox, Opera, etc.
        else
          {
          var parser=new DOMParser();
          var doc=parser.parseFromString(text,"text/xml");
          }
          return doc;
    },
    
    createTestAccount: function(){
       cosmo.util.auth.clearAuth();
       var user = {
           password: "testing"
       };
       var success = false;
       
       var i = 0;
       while (!success && i < 10){
           var un = "user0";
           user.username = un;
           user.firstName = un;
           user.lastName = un;
           user.email = un + "@cosmotesting.osafoundation.org";
           
           cosmo.cmp.signup(user, {
               load: function(){success = true}, 
               error: function(){
                  cosmotest.service.conduits.test_conduits.cleanup(user);
                  i++;
           }}, true);
       }
       cosmo.util.auth.setCred(user.username, user.password);
       
       return user;
       
    },

    cleanup: function(user){
        cosmo.util.auth.setCred("root", "cosmo");
        cosmo.cmp.deleteUser(user.username, {handle: function(){}}, true);
        cosmo.util.auth.clearAuth();
    },
    
    asyncTest: function(callback){
        var user = cosmotest.util.createTestAccount();
        var td = new dojo.Deferred();
        td.addCallback(callback);
        td.addErrback(function(e){dojo.debug(e)});
        td.addBoth(dojo.lang.hitch(this, function(){this.cleanup(user)}));
        td.callback(user);
    }

}
