dependencies = {};

dependencies.layers = [
    {
        name: "../cosmo/login.js",
        resourceName: "cosmo.login",
        dependencies: [
        ]
    },
    {
        name: "../cosmo/pim.js",
        resourceName: "cosmo.pim",
        layerDependencies:["cosmo.login"],
        dependencies: [
        ]
    },
    {
        name: "../cosmo/userlist.js",
        layerDependencies:["../cosmo/userlist.js"],
        dependencies: [
        ]
    }
];
dependencies.prefixes = [
    ["dijit", "../dijit" ],
    ["dojox", "../dojox" ],
    ["cosmo", "../../src/cosmo"]
];

