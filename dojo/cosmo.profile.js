// pull in the dependency list and define it in the var "dependencies". This
// over-rides the default built into getDependencyList.js. The bootstrap and
// hostenv files are included by default and don't need to be included here,
// but you can change the hostenv file that's included by setting the value of
// the variable "hostenvType" (defaults to "browser").
var dependencies = [
    "dojo.lang.*",
    "dojo.debug.console",
    "dojo.widget.*"
];

dependencies.layers = [
	{
		name: "src/cosmo-login.js",
		resourceName: "dojo.cosmo-login",
		layerDependencies: [
			"dojo.js"
		],
		dependencies: [
			"dojo.cosmo-login"
		]
	},
    {
		name: "src/cosmo-pim.js",
		resourceName: "dojo.cosmo-pim",
		layerDependencies: [
			"dojo.js"
		],
		dependencies: [
			"dojo.cosmo-pim"
		]
	}
];
dependencies.prefixes = [
   ["cosmo", "../../cosmo/src/main/webapp/js/cosmo"]
];

load("getDependencyList.js");
