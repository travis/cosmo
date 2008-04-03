dojo.registerModulePath("cosmo", "../../../cosmo");
dojo.registerModulePath("cosmotest", "../../../../test/unit/js/cosmo");
dojo.require("cosmo.env");
cosmo.env.setBaseUrl(dojo.moduleUrl("cosmo", "../.."));

dojo.require("cosmo.testutils");
cosmo.testutils.init(
    ["cosmotest.model.test_delta",
     "cosmotest.model.test_model",
     "cosmotest.model.test_modelTriage",
     "cosmotest.model.test_common",
     "cosmotest.model.test_eventStamp",
     "cosmotest.model.test_performance",
     "cosmotest.model.util.test_util",
     "cosmotest.datetime.test_date",
     "cosmotest.datetime.test_timezone",
     "cosmotest.datetime.test_serialize",
     "cosmotest.service.translators.test_eim",
     "cosmotest.service.transport.test_Atom",
     "cosmotest.service.transport.test_Rest",
     "cosmotest.util.test_html",
     "cosmotest.util.test_i18n",
     "cosmotest.cmp.test_cmp",
     "cosmotest.account.test_preferences",
     "cosmotest.util.encoding.test_base64",
     "cosmotest.model.test_delta",
     "cosmotest.model.test_eventStamp",
     "cosmotest.model.test_model",
     "cosmotest.model.util.test_util",
     "cosmotest.datetime.test_date",
     "cosmotest.datetime.test_timezone",
     "cosmotest.datetime.test_serialize"
    ]);
 