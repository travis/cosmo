// Environment setup
// -----------------------
// This is a hack to add the same shortcuts
// for the various UI elements to varRegistry 
// that the JSON tests have from initialize.json
cosmo.jsTestInit = function () {
  var str = windmill.jsTest.getFile('/windmill-jstest/setup/initialize.json');
  var obj = eval('(' + str + ')');
  var vars = obj.params.variables;
  for (var i = 0; i < vars.length; i++) {
    var v = vars[i].split('|');
    var key = v[0];
    var val = v[1];
    key = (key.indexOf('{$') != 0) ? '{$'+ key +'}' : key;
    windmill.varRegistry.addItem(key, val);
  }
};
cosmo.jsTestInit();
