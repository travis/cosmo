if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 0
    counter = 10
    mask = 1
    
    for arg in sys.argv:
        args = arg.split("=")
        if args[0] == "host":
            host = args[1]
        elif args[0] == "port":
            port = int(args[1])
        elif args[0] == "path":
            path = args[1]
        elif args[0] == "recurring":
            counter = int(args[1])
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
    
    cosmobasicquery = CosmoBasicQuery(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmobugs = CosmoBugs(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmofreebusy = CosmoFreeBusy(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmoinvalid = CosmoInvalid(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmolimitexpand = CosmoLimitExpand(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmomkcalendar = CosmoMkcalendar(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmomultiget = CosmoMultiget(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmoticket = CosmoTicket(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmotimerangequery = CosmoTimeRangeQuery(host=host, port=port, path=path, debug=debug, mask=mask)
    
    suite = [cosmobasicquery, cosmobugs, cosmoinvalid, cosmofreebusy,
              cosmolimitexpand, cosmomkcalendar, cosmomultiget, cosmoticket, 
              cosmotimerangequery] 
    
    count = 0
    failures = 0
    passes = 0
    scriptcount = 0
    scriptfailures = 0
    
    # Run Suite
    for x in suite:
        if mask == 0:
            print "Starting test script %s" % x.__class__.__name__
            x.startRun()
        elif mask != 0:
            try:
                scriptcount = scriptcount + 1
                x.startRun()
            except:
                print "Failure :: Script %s :: Failed with python error" % x.__class__.__name__
                scriptfailures = scriptfailures + 1
                
    # Calculate passes and failures
    
    for s in suite:
        for i in range(len(s.results)):
            if s.results[i] == False:
                failures = failures + 1
                print "Failure :: Script %s :: Test %s :: %s" % (s.__class__.__name__, s.resultnames[i], s.resultcomments[i])
            elif s.results[i] == True:
                passes = passes + 1
                if debug > 0:
                    print "Failure :: Script %s :: Test %s :: %s" % (s.__class__.__name__, s.resultnames[i], s.resultcomments[i])
            count = count + 1
    print "Scripts Run %s :: Script Passes :: %s; Script Failures :: %s; Tests Run :: %s; Test Passes :: %s; Test Failures :: %s" % (scriptcount, scriptcount - scriptfailures, scriptfailures, 
                                                                                                                                   count, passes, failures)
