dojo.provide("cosmotest.util");
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
    }
}
