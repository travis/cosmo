  var bu_alert = dojo.hostenv.println;
  var print = dojo.hostenv.println;

  var test_names = jum.alltests_.group_names_;
  var test_menu = document.getElementById("test-menu");
  for (var i in test_names) {
     var row = document.createElement("tr");
     var col = document.createElement("td");
     var box = document.createElement("input");
     box.setAttribute("type","checkbox");
     box.setAttribute('value',test_names[i]);
     col.appendChild(box);
     row.appendChild(col);
     col = document.createElement("td");
     col.appendChild(document.createTextNode(test_names[i]));
     row.appendChild(col);
     test_menu.appendChild(row);
   }
   function checkBoxesFromLocation(){
       var testNames = location.search.slice(1).split("&");
   var tests = {};
   
   for (var i = 0; i < testNames.length; i++){
       var testQuery = testNames[i].split("=");
   if (testQuery[0] == "t"){
           tests[testQuery[1]] = true;
       }
   }
   var inputs = test_menu.getElementsByTagName("input");
       for (var i = 0; i < inputs.length; i++){
           if (tests[inputs[i].value]){
               inputs[i].checked = true;
           }
       }
   }
   
   checkBoxesFromLocation();
   
   function getCurrentQueryString(){
       var inputs = test_menu.getElementsByTagName("input");
   var qs = "?";
   var qsElements = 0;
   for (var i = 0; i < inputs.length; i++){
       if (inputs[i].checked) {
              var and = (qsElements > 0)? "&" : "";
   qs = qs + and + "t=" + inputs[i].value;
               qsElements++;
           }
       }
       return qs;
   }
   
   
   function run_tests() {
       var tests = []
       var rows = test_menu.getElementsByTagName ("tr");
   for (var i = 0; i < rows.length; i++) {
       var test_row = rows[i];
       if (test_row.nodeType != 1/*Node.ELEMENT_NODE in most browsers*/) continue;
           if (test_row.childNodes[0].childNodes[0].checked) {
             jum.runGroup(test_row.childNodes[1].childNodes[0].data);
           }
       }
   }
   
   function checkAll(){
       var inputs = test_menu.getElementsByTagName("input");
       for (var i = 0; i < inputs.length; i++){
           inputs[i].checked = true;
       }
   }
   
   function uncheckAll(){
       var inputs = test_menu.getElementsByTagName("input");
       for (var i = 0; i < inputs.length; i++){
           inputs[i].checked = false;
       }
   }
   
   function clearOutput(){
       var out = document.getElementById(djConfig.debugContainerId);
       out.innerHTML = "";
   }
   
   /*
   Convenience!
   */
   document.onkeyup = function (e){
       e = !e ? window.event : e;
   
   if (String.fromCharCode(e.keyCode) == 'R'){
       location = location.href.split("?")[0] + getCurrentQueryString();
   }

   if (String.fromCharCode(e.keyCode) == 'T'){
        run_tests();
    }

   if (String.fromCharCode(e.keyCode) == 'U'){
        uncheckAll();
    }

   if (String.fromCharCode(e.keyCode) == 'A'){
        checkAll();
    }

   if (String.fromCharCode(e.keyCode) == 'C'){
   clearOutput();
   }
 }