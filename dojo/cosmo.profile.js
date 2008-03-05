dependencies = {};

dependencies.layers = [
    {
        name: "../cosmo/login.js",
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
        layerDependencies:["../cosmo/login.js"],
        dependencies: [
            "cosmo.app.pim",
            "cosmo.datetime.timezone.LazyCachingTimezoneRegistry",
            "cosmo.ui.event.listeners"
        ]
    },
    {
        name: "../cosmo/userlist.js",
        layerDependencies:["../cosmo/userlist.js"],
        dependencies: [
            "cosmo.ui.widget.UserList"
        ]
    }
];
dependencies.prefixes = [
    [ "dijit", "../dijit" ],
    [ "dojox", "../dojox" ],
    ["cosmo", "../../../cosmo/src/main/webapp/js/cosmo", "../../../../cosmo/src/main/webapp/js/cosmo/copyright.txt"]
];
