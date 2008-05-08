dojo.provide("cosmo.util.tests.auth");
dojo.require("cosmo.util.auth");

(function(){
	tests.register("cosmo.util.tests.auth", [
		function testSetCredentials(t){
            cosmo.util.auth.setUsername("foo");
            cosmo.util.auth.setPassword("bar");
			t.t(cosmo.util.auth.currentlyAuthenticated());
            cosmo.util.auth.clearAuth();
			t.f(cosmo.util.auth.currentlyAuthenticated());
		}
	]);
})();
