from DAVTest import DAVTest

class CosmoChandlerZeroPointSixPointOne(DAVTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        try:
            self.appendUser = self.appendDict['username']
        except KeyError:
            self.appendUser = ''
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-chandler061TestAccount%s' % self.appendUser)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-chandler061TestAccount%s</username> \
                                 <password>chandler061</password> \
                                 <firstName>cosmo-chandler061</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-chandler061TestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendUser, self.appendUser)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
        print self.test_response.read()

        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-chandler061TestAccount%s" % self.appendUser, "chandler061")
        
        homepath = self.pathBuilder('/home/cosmo-chandler061TestAccount%s/' % self.appendUser)
        
        # ------- Begin Emulation of TestSharing.py
        
        # --- Emulation of TestSharing.py
        self.testStart('Emulation of TestSharing.py')
        self.testStart('OPTIONS Request for /cosmo/home/test')
        self.request('OPTIONS', homepath, None, self.headers)
        self.checkStatus(200)
        
        self.testStart('PROPFIND for /cosmo/home/test/')
        #Build request body
        f = open('files/reports/chandler/chandler_0.6.1_testAccount.xml')
        propfindbody = f.read()
        propheaders = self.headerAdd({'Content-Type' : 'text/xml; charset="utf-8"'})
        self.request('PROPFIND', homepath, propfindbody, propheaders)
        self.checkStatus(207)
        
        self.testStart('Another identical PROPFIND Request for /cosmo/home/test')
        self.request('PROPFIND', homepath, propfindbody, propheaders)
        self.checkStatus(207)

        self.testStart(' OPTIONS Request for /cosmo/home/test/test_s_calendar')
        calpath = self.pathBuilder('/home/cosmo-chandler061TestAccount%s/test_s_calendar/' % self.appendUser)
        self.request('OPTIONS', calpath, None, self.headers)
        self.checkStatus(200)
        
        self.testStart(' PROPFIND Request for /cosmo/home/test/test_s_calendar')
        #Build request body
        f = open('files/reports/chandler/chandler_0.6.1_testAccount.xml')
        propfindbody = f.read()
        self.request('PROPFIND', calpath, propfindbody, propheaders)
        self.checkStatus(501)
        
        self.testStart('HEAD request which also fails')
        self.request('HEAD', calpath, None, self.headers)
        self.checkStatus(404)
        
        self.testStart('MKCALENDAR request')
        self.request('MKCALENDAR', calpath, None, self.headers)
        self.checkStatus(201)
        
        self.testStart('PROPPATCH request')
        #Build request body
        f = open('files/reports/chandler/chandler_0.6.1_proppatch.xml')
        proppatchbody = f.read()
        self.request('PROPPATCH', calpath, proppatchbody, propheaders)
        self.checkStatus(207)

        self.testStart('MKTICKET request read only')
        f = open('files/reports/chandler/chandler_0.6.1_mkticket.xml')
        mkticketbody = f.read()
        self.request('MKTICKET', calpath, mkticketbody, propheaders)
        self.checkStatus(200)
        
        self.testStart('MKTICKET request read/write')
        f = open('files/reports/chandler/chandler_0.6.1_mkticket_rw.xml')
        mkticketrwbody = f.read()
        self.request('MKTICKET', calpath, mkticketrwbody, propheaders)
        self.checkStatus(200)
        #print self.test_response.read()
        
        self.testStart('OPTIONS Request for /cosmo/home/test/test_s_calendar/.chandler/')
        dotchandlerpath = self.pathBuilder('/home/cosmo-chandler061TestAccount%s/test_s_calendar/.chandler' % self.appendUser)
        self.request('OPTIONS', dotchandlerpath, None, self.headers)
        self.checkStatus(200)
        
        self.testStart('PROPFIND Request for /cosmo/home/test/test_s_calendar/.chandler/')
        self.request('PROPFIND', dotchandlerpath, propfindbody, propheaders)
        self.checkStatus(501)
        
        self.testStart(' HEAD Request for /cosmo/home/test/test_s_calendar/.chandler/')
        self.request('HEAD', dotchandlerpath, None, self.headers)
        self.checkStatus(404) 

        self.testStart('MKCOL for .chandler')
        self.request('MKCOL', dotchandlerpath, None, self.headers)
        self.checkStatus(201)
        
        self.testStart('PROPFIND for .chandler')
        self.request('PROPFIND', dotchandlerpath, propfindbody, propheaders)
        self.checkStatus(207)
        
        self.testStart('PUT Request for /cosmo/home/test/test_s_calendar/.chandler/412ad108-c6bd-11da-c95e-001124e4b0d2.xml')
        f = open('files/reports/chandler/412ad108-c6bd-11da-c95e-001124e4b0d2.xml')
        putheaders = self.headerAdd({'Content-Type' : 'text/plain'})        
        putxmlbody = f.read()
        putxmlpath = self.pathBuilder('/home/cosmo-chandler061TestAccount%s/test_s_calendar/.chandler/412ad108-c6bd-11da-c95e-001124e4b0d2.xml' % self.appendUser)
        self.request('PUT', putxmlpath, putxmlbody, putheaders)
        self.checkStatus(201)
        
        self.testStart('PUT Request for /cosmo/home/test/test_s_calendar/.chandler/share.xml')
        f = open('files/reports/chandler/chandler_0.6.1_share.xml')
        putxmlbody = f.read()
        putxmlpath = self.pathBuilder('/home/cosmo-chandler061TestAccount%s/test_s_calendar/.chandler/share.xml' % self.appendUser)
        self.request('PUT', putxmlpath, putxmlbody, putheaders)
        self.checkStatus(201)
        
        self.testStart('PROPFIND Request for /cosmo/home/test/test_s_calendar/')
        self.request('PROPFIND', calpath, propfindbody, self.headers)
        self.checkStatus(207)
        
        self.testStart('PUT Request for /cosmo/home/test/test_s_calendar/412ad108-c6bd-11da-c95e-001124e4b0d2.ics')
        f = open('files/reports/chandler/412ad108-c6bd-11da-c95e-001124e4b0d2.ics')
        puticsbody = f.read()
        putheaders = self.headerAdd({'Content-Type': 'text/calendar'})
        puticspath = self.pathBuilder('/home/cosmo-chandler061TestAccount%s/test_s_calendar/412ad108-c6bd-11da-c95e-001124e4b0d2.ics' % self.appendUser)
        self.request('PUT', puticspath, puticsbody, putheaders)
        self.checkStatus(201)
        
        self.testStart('PROPPATCH Request for /cosmo/home/test/test_s_calendar/412ad108-c6bd-11da-c95e-001124e4b0d2.ics')
        f = open('files/reports/chandler/chandler_0.6.1_proppatch_ics.xml')
        proppatchicsbody = f.read()
        self.request('PROPPATCH', puticspath, proppatchicsbody, propheaders)
        self.checkStatus(207)


        
if __name__ == "__main__":
    
    import sys
    
    host = 'localhost'
    port = '8080'
    path = '/cosmo'
    debug = 0
    counter = 10
    
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
        
    print "host %s port %s recurring %s path %s" % (host, port, counter, path)
    
    cosmoChandlerZeroPointSixPointOne = CosmoChandlerZeroPointSixPointOne(host=host, port=port, path=path)
    cosmoChandlerZeroPointSixPointOne.debug = debug
    cosmoChandlerZeroPointSixPointOne.startRun()
    cosmoChandlerZeroPointSixPointOne.end()