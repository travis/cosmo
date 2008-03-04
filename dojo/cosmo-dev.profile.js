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
    }
];
dependencies.prefixes = [
    [ "dijit", "../dijit" ],
    [ "dojox", "../dojox" ],
    ["cosmo", "../../../cosmo/src/main/webapp/js/cosmo", "../../../../cosmo/src/main/webapp/js/cosmo/copyright.txt"]
];

