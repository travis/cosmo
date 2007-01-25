var dependencies = [
        "cosmo.app",
	"cosmo.ui.cal_main",
	"cosmo.ui.global_css",
	"cosmo.ui.event.listeners",
	"dojo.debug.console",
	"dojo.logging.ConsoleLogger",
	"dojo.logging.Logger",
	"cosmo.ui.widget.Debug",
    "cosmo.datetime.timezone.LazyCachingTimezoneRegistry"
];

dependencies.prefixes = [
   ["cosmo", "../../cosmo"]
];

load("getDependencyList.js");