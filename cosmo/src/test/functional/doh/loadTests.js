dojo.registerModulePath("cosmo", "../../../cosmo");
dojo.registerModulePath("cosmotest", "../../../../test/functional/doh/cosmo");
dojo.require("cosmo.env");
cosmo.env.setBaseUrl(dojo.moduleUrl("cosmo", "../.."));

dojo.require("cosmo.datetime.timezone.LazyCachingTimezoneRegistry");
var registry = new cosmo.datetime.timezone.LazyCachingTimezoneRegistry(dojo.moduleUrl("cosmo","../lib/olson-tzdata/"));
cosmo.datetime.timezone.setTimezoneRegistry(registry);

dojo.require("cosmo.testutils");
cosmo.testutils.init(
    [
        "cosmotest.test_unicode",
        "cosmotest.service.conduits.test_conduits",
        "cosmotest.test_cmp",
        "cosmotest.data.test_UserStore"
    ]);
 