dependencies = {};
//should be kept in sync with cosmo.profile.js
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
        dependencies: [
            "cosmo.ui.widget.UserList"
        ]
    },
    { // should be moved into Chui
        name: "../widgets_build/widgets.js",
        dependencies: [
            "chui.ChuiPane",
            "chui.Entri",
            "chui.auth"
        ]
    }

];
dependencies.prefixes = [
    ["dijit", "../dijit" ],
    ["dojox", "../dojox" ],
    ["cosmo", "../../src/cosmo"],
    ["chui", "../../../../widgets/chui"], // assumes widgets is a sibling of cosmo checkout
    ["widgets_build", "../widgets_build"]
];
