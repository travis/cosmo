/*if(this["load"]){
	load(["jsunit_wrap.js"]);
	bu_alert = print;
}*/
jum.verbose=false;
function test_JsUnitWrap_simple() {
  jum.assertEquals('test1', 1, 1);
  jum.assertTrue('test2', true);
  jum.assertTrue('test3', "true");
  jum.assertFalse('test4', false);
}

function test_JsUnitWrap_deep() {
  if (!jum_uneval) {jum.untested("JsUnitWrap_deep"); return;}
  var a = [1,2];
  var b = [1,2];
  jum.assertEquals('test1', a, b);
}

function test_JsUnitWrap_continueAsync() {
  jum.pause('test_continueAsync', "1", 'testing continueAsync');
  jum.pause("anonAsync", null, "testing anon async function");
  
  jum.resume('test_continueAsync', "1", test_continueAsync);
  jum.resume("anonAsync", null, function(){jum.assertTrue('tada', true);});
}

function test_continueAsync(){
	dojo.debug("continueAsync called!");
	jum.assertTrue("woohooo!", true);
}

/*
function test_JsUnitWrap_waitAll() {
  jum.waitAll('JsUnitWrap_waitAll', arguments.callee, 2000);
}
*/

// jum.init();
