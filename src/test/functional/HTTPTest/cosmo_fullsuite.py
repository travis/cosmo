if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 0
    counter = 10
    mask = 5
    recurrence = 1
    
    for arg in sys.argv:
        args = arg.split("=")
        if args[0] == "host":
            host = args[1]
        elif args[0] == "port":
            port = int(args[1])
        elif args[0] == "path":
            path = args[1]
        elif args[0] == "recurrence":
            recurrence = int(args[1])
        elif args[0] == "debug":
            debug = int(args[1])
        elif args[0] == "mask":
            mask = int(args[1])
        
    # ----- Define list of test scripts to run ------ #
    
    from cosmo_basicquery import CosmoBasicQuery
    from cosmo_bugs import CosmoBugs
    from cosmo_freebusy import CosmoFreeBusy
    from cosmo_invalid import CosmoInvalid
    from cosmo_limitexpand import CosmoLimitExpand
    from cosmo_mkcalendar import CosmoMkcalendar
    from cosmo_multiget import CosmoMultiget
    from cosmo_ticket import CosmoTicket
    from cosmo_timerangequery import CosmoTimeRangeQuery
    from cosmo_chandler061 import CosmoChandlerZeroPointSixPointOne
    
    cosmobasicquery = CosmoBasicQuery(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmobugs = CosmoBugs(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmofreebusy = CosmoFreeBusy(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmoinvalid = CosmoInvalid(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmolimitexpand = CosmoLimitExpand(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmomkcalendar = CosmoMkcalendar(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmomultiget = CosmoMultiget(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmoticket = CosmoTicket(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmotimerangequery = CosmoTimeRangeQuery(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    cosmochandler061 = CosmoChandlerZeroPointSixPointOne(host=host, port=port, path=path, debug=debug, mask=mask, recurrence=recurrence)
    
    suite = [cosmobasicquery, 
              cosmobugs, 
              cosmoinvalid, 
              #cosmofreebusy,
              cosmolimitexpand, 
              cosmomkcalendar, 
              cosmomultiget, 
              cosmoticket, 
              cosmotimerangequery, 
              cosmochandler061] 
    
    count = 0
    failures = 0
    passes = 0
    scriptcount = 0
    scriptfailures = 0
    
    # Run Suite
    for x in suite:
        if mask == 0:
            print "Starting test script %s" % x.__class__.__name__
            x.fullRun()
        elif mask != 0:
            try:
                scriptcount = scriptcount + 1
                x.fullRun()
            except:
                print "Failure :: Script %s :: Failed with python error" % x.__class__.__name__
                scriptfailures = scriptfailures + 1
                
    # Calculate passes and failures
    
    for s in suite:
        for i in range(len(s.results)):
            if s.results[i] is False:
                failures = failures + 1
                print "Failure :: Script %s :: Test %s :: %s" % (s.__class__.__name__, s.resultNames[i], s.resultComments[i])
            elif s.results[i] is True:
                passes = passes + 1
                if debug > 0:
                    print "Success :: Script %s :: Test %s :: %s" % (s.__class__.__name__, s.resultNames[i], s.resultComments[i])
            count = count + 1
    print "Scripts Run :: %s; Script Passes :: %s; Script Failures :: %s; Tests Run :: %s; Test Passes :: %s; Test Failures :: %s" % (scriptcount, scriptcount - scriptfailures, scriptfailures, 
                                                                                                                                   count, passes, failures)
