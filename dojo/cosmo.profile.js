var dependencies = [
    "cosmo.app",
    "cosmo.app.pim", 
    "cosmo.ui.global_css",
    "cosmo.ui.event.listeners",
    "dojo.debug.console",
    "dojo.logging.ConsoleLogger",
    "dojo.logging.Logger",
    "cosmo.ui.widget.Debug",
    "cosmo.datetime.timezone.LazyCachingTimezoneRegistry"
];

dependencies.prefixes = [
   ["cosmo", "../../cosmo/src/main/webapp/js/cosmo"]
];

load("getDependencyList.js");