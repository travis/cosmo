dependencies = {};

dependencies.layers = [
    {
        name: "../cosmo/layers/login.js",
        dependencies: [
            "cosmo.layers.login"
        ]
    },
    {
        name: "../cosmo/layers/pim.js",
        layerDependencies:["../cosmo/login.js"],
        dependencies: [
            "cosmo.layers.pim"
        ]
    },
    {
        name: "../cosmo/layers/userlist.js",
        dependencies: [
            "cosmo.layers.userlist"
        ]
    }
];
dependencies.prefixes = [
    ["dijit", "../dijit" ],
    ["dojox", "../dojox" ],
    ["cosmo", "../../src/cosmo"],
    ["css", "../../src/cosmo/themes/default"]
];
