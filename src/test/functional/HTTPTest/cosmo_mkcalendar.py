from HTTPTest import HTTPTest    

class CosmoMkcalendar(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headerAdd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headerAddAuth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathBuilder('/cmp/user/cosmo-mkcalendarTestAccount')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-mkcalendarTestAccount</username> \
                                 <password>cosmo-mkcalendar</password> \
                                 <firstName>cosmo-mkcalendar</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-mkcalendarTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
    
        #Add auth to global headers
        self.headers = self.headerAddAuth("cosmo-mkcalendarTestAccount", "cosmo-mkcalendar")
        
        # ------- Test 1 - Invalid - Bad XML ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidBadXML/')
        f = open('files/mkcalendar/invalidBadXML.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
    
        # ------- Test 2 - Invalid - Caldav Property missing ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidCaldavProperty/')
        f = open('files/mkcalendar/invalidCaldavProperty.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 3 - Invalid - Dav Property ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidDavProperty/')
        f = open('files/mkcalendar/invalidDavProperty.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 4 - Invalid - Supported Calendar Component ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidSupportedCalendarComponent/')
        f = open('files/mkcalendar/invalidSupportedCalendarComponent.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 5 - Invalid - Supported Calendar Data ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidSupportedCalendarData/')
        f = open('files/mkcalendar/invalidSupportedCalendarData.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 6 - Invalid - Timezone ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidTimezone/')
        f = open('files/mkcalendar/invalidTimezone.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(400)
        
        # ------- Test 7 - Valid - No Body ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/invalidNoBody/')
        self.request('MKCALENDAR', calpath, body=None, headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 8 - Valid - Description No Lang ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/validDescriptionNoLang/')
        f = open('files/mkcalendar/validDescriptionNoLang.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 9 - Valid - Full Body ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/validFullBody/')
        f = open('files/mkcalendar/validFullBody.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 10 - Valid - No Desciption ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/validNoDescription/')
        f = open('files/mkcalendar/validNoDescription.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 11 - Invalid - No Display Name ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/validNoDisplayname/')
        f = open('files/mkcalendar/validNoDisplayname.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 12 - Invalid - No Supported Calendar Component ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/validNoSupportedCalendarComponent/')
        f = open('files/mkcalendar/validNoSupportedCalendarComponent.xml')
        self.request('MKCALENDAR', calpath, body=f.read(), headers=self.headers)
        self.checkStatus(201)
        
        # ------- Test 13 - Invalid - No Timezone ------- #        
        
        #Create Calendar on CalDAV server   
        calpath = self.pathBuilder('/home/cosmo-mkcalendarTestAccount/validNoTimezone/')
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
    