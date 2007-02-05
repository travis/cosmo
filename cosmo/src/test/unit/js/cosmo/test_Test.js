dojo.require("dojo.collections.ArrayList");

function getAL(){
	return new dojo.collections.ArrayList(["foo","bar","test","bull"]);
}


function test_ArrayList_ctor(){
	var al = getAL();

	//	test the constructor
	jum.assertEquals("test10", 4, al.count);
}
