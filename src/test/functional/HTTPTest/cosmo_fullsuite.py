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
    #from cosmo_freebusy import CosmoFreeBusy
    from cosmo_invalid import CosmoInvalid
    from cosmo_limitexpand import CosmoLimitExpand
    from cosmo_mkcalendar import CosmoMkcalendar
    from cosmo_multiget import CosmoMultiget
    from cosmo_ticket import CosmoTicket
    from cosmo_timerangequery import CosmoTimeRangeQuery
    
    cosmobasicquery = CosmoBasicQuery(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmobugs = CosmoBugs(host=host, port=port, path=path, debug=debug, mask=mask)
    #cosmofreebusy = CosmoFreeBusy(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmoinvalid = CosmoInvalid(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmolimitexpand = CosmoLimitExpand(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmomkcalendar = CosmoMkcalendar(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmomultiget = CosmoMultiget(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmoticket = CosmoTicket(host=host, port=port, path=path, debug=debug, mask=mask)
    cosmotimerangequery = CosmoTimeRangeQuery(host=host, port=port, path=path, debug=debug, mask=mask)
    
    suite = [cosmobasicquery, cosmobugs, cosmoinvalid, 
              cosmolimitexpand, cosmomkcalendar, cosmomultiget, cosmoticket, 
              cosmotimerangequery] #cosmofreebusy is currently removed.
    
    # Run Suite
    for x in suite:
        x.startRun()
    
    # Calculate passes and failures
    count = 0
    failures = 0
    passes = 0
    
    for s in suite:
        for i in range(len(s.results)):
            if s.results[i] == False:
                failures = failures + 1
                print "Failure :: Script %s :: Test %s :: %s" % (s.__class__.__name__, s.resultnames[i], s.resultcomments[i])
            elif s.results[i] == True:
                passes = passes +1
                if debug > 0:
                    print "Failure :: Script %s :: Test %s :: %s" % (s.__class__.__name__, s.resultnames[i], s.resultcomments[i])
            count = count + 1
        
    print "Failures :: %s" % failures
    print "Passes :: %s" % passes 
    print "Total tests run :: %s" % count
