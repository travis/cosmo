dojo.provide("cosmo.datetime");
dojo.provide("cosmo.datetime.*");

dojo.kwCompoundRequire({
	common: ["cosmo.datetime.timezone", "cosmo.datetime.Date", false, false]
});
