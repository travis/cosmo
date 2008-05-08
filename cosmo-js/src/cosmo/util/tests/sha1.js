dojo.provide("cosmo.util.tests.sha1");
dojo.require("cosmo.util.sha1");

(function(){
	var message="\u00E5The rain in Spain falls mainly on the plain.";
	var base64='FODN+eoU0iYFAC1ce4u46Ymg0BY=';
	var hex='14e0cdf9ea14d22605002d5c7b8bb8e989a0d016';
	var s='\x14\xe0\xcd\xf9\xea\x14\xd2&\x05\x00-\\{\x8b\xb8\xe9\x89\xa0\xd0\x16';
	var cus = cosmo.util.sha1;
	tests.register("cosmo.util.tests.sha1", [
		function testBase64Compute(t){
			t.assertEqual(base64, cus.b64(message));
		},
		function testHexCompute(t){
			t.assertEqual(hex, cus.hex(message));
		},
		function testStringCompute(t){
			t.assertEqual(s, cus.str(message));
		}
	]);
})();
