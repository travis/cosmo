from HTTPTest import HTTPTest    

class CosmoMkcalendar(HTTPTest):
    
    def startRun(self):
        
        self.testStart('Setup Accounts')
        
        try:
            self.appendUser = self.appendDict['username']
        except KeyError:
            self.appendUser = ''
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-mkcalendarTestAccount%s' % self.appendUser)
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-mkcalendarTestAccount%s</username> \
                                 <password>cosmo-mkcalendar</password> \
                                 <firstName>cosmo-mkcalendar</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-mkcalendarTestAccount%s@osafoundation.org</email> \
                                 </user>' % (self.appendUser, self.appendUser)
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
    
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-mkcalendarTestAccount%s" % self.appendUser, "cosmo-mkcalendar")
        
        # ------- Test 1 - Invalid - Bad XML ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidBadXML/' % self.appendUser)
        f = open('files/mkcalendar/invalidBadXML.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
    
        # ------- Test 2 - Invalid - Caldav Property missing ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidCaldavProperty/' % self.appendUser)
        f = open('files/mkcalendar/invalidCaldavProperty.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 3 - Invalid - Dav Property ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidDavProperty/' % self.appendUser)
        f = open('files/mkcalendar/invalidDavProperty.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 4 - Invalid - Supported Calendar Component ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidSupportedCalendarComponent/' % self.appendUser)
        f = open('files/mkcalendar/invalidSupportedCalendarComponent.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 5 - Invalid - Supported Calendar Data ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidSupportedCalendarData/' % self.appendUser)
        f = open('files/mkcalendar/invalidSupportedCalendarData.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 6 - Invalid - Timezone ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidTimezone/' % self.appendUser)
        f = open('files/mkcalendar/invalidTimezone.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 7 - Valid - No Body ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/invalidNoBody/' % self.appendUser)
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 8 - Valid - Description No Lang ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/validDescriptionNoLang/' % self.appendUser)
        f = open('files/mkcalendar/validDescriptionNoLang.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 9 - Valid - Full Body ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/validFullBody/' % self.appendUser)
        f = open('files/mkcalendar/validFullBody.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 10 - Valid - No Desciption ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/validNoDescription/' % self.appendUser)
        f = open('files/mkcalendar/validNoDescription.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 11 - Invalid - No Display Name ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/validNoDisplayname/' % self.appendUser)
        f = open('files/mkcalendar/validNoDisplayname.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 12 - Invalid - No Supported Calendar Component ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/validNoSupportedCalendarComponent/' % self.appendUser)
        f = open('files/mkcalendar/validNoSupportedCalendarComponent.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 13 - Invalid - No Timezone ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount%s/validNoTimezone/' % self.appendUser)
        f = open('files/mkcalendar/validNoTimezone.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
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
    
    cosmomkcalendar = CosmoMkcalendar(host=host, port=port, path=path)
    cosmomkcalendar.debug = debug
    cosmomkcalendar.startRun()
    cosmomkcalendar.end()
    