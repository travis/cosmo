dependencies = {};

dependencies.layers = [
    {
        name: "../cosmo/login.js",
        resourceName: "cosmo.login",
        dependencies: [
            "cosmo.app",
            "cosmo.account.create",
            "cosmo.convenience",
            "cosmo.ui.widget.LoginDialog",
            "cosmo.ui.widget.ModalDialog",
            "dojo.cookie"
        ]
    },
    {
        name: "../cosmo/pim.js",
        resourceName: "cosmo.pim",
        layerDependencies:["cosmo.login"],
        dependencies: [
            "cosmo.app.pim"
        ]
    }
];
dependencies.prefixes = [
    [ "dijit", "../dijit" ],
    [ "dojox", "../dojox" ],
    ["cosmo", "../../../cosmo/src/main/webapp/js/cosmo", "../../../../cosmo/src/main/webapp/js/cosmo/copyright.txt"]
];
