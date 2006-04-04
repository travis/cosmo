import time, md5, random

from HTTPTest import HTTPTest    

class CosmoTicket(HTTPTest):
    
    def startRun(self):
        
        #Set Headers and paths
        
        #Create Headers for CMP
        
        # ------- Test Create Account ------- #
           
        cmpheaders = self.headeradd({'Content-Type' : "text/xml; charset=UTF-8"})
        cmpheaders = self.headeraddauth("root", "cosmo", headers=cmpheaders)
           
        #CMP path
        cmppath = self.pathbuilder('/cmp/user/cosmo-ticketTestAccount')
        cmppath2 = self.pathbuilder('/cmp/user/cosmo-ticketTestAccount2')
        
        #Create testing account        
        bodycreateaccount = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-ticketTestAccount</username> \
                                 <password>cosmo-ticket</password> \
                                 <firstName>cosmo-ticket</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-ticketTestAccount@osafoundation.org</email> \
                                 </user>'
                                 
        bodycreateaccount2 = '<?xml version="1.0" encoding="utf-8" ?> \
                                 <user xmlns="http://osafoundation.org/cosmo/CMP"> \
                                 <username>cosmo-ticketTestAccount2</username> \
                                 <password>cosmo-ticket</password> \
                                 <firstName>cosmo-ticket2</firstName> \
                                 <lastName>TestAccount</lastName> \
                                 <email>cosmo-ticketTestAccount2@osafoundation.org</email> \
                                 </user>'                                 
                                 
        #Create account and check status
        self.request('PUT', cmppath, body=bodycreateaccount, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED
    
        #Create account and check status
        self.request('PUT', cmppath2, body=bodycreateaccount2, headers=cmpheaders)
        self.checkStatus(201) # 201 ACCOUNT CREATED    
    
        #Add auth to global headers
        authheaders1 = self.headeraddauth("cosmo-ticketTestAccount", "cosmo-ticket")
        authheaders2 = self.headeraddauth("cosmo-ticketTestAccount2", "cosmo-ticket")
        
        f = open('files/rTicket.xml')
        rTicket = f.read()
        
        f = open('files/rwTicket.xml')
        rwTicket = f.read()
        
        f = open('files/badNSTicket.xml')
        badNSTicket = f.read() 
        
        #Create Calendar on CalDAV server   
        home1 = self.pathbuilder('/home/cosmo-ticketTestAccount/')
        home2 = self.pathbuilder('/home/cosmo-ticketTestAccount2/')
    
        # -------- MKTICKET Test

        # --- OK (read-only)

        self.request('MKTICKET', home1, body=rTicket, headers=authheaders1)
        self.checkStatus(200) # MKTICKET OK (read-only ticket)
    
        ticket = self.test_response.getheader('Ticket')

        # --- OK (read-write)

        self.request('MKTICKET', home1, body=rwTicket, headers=authheaders1)
        self.checkStatus(200) # MKTICKET OK (read-write ticket)

        rwTicketId = self.test_response.getheader('Ticket')

        # --- Bad XML

        self.request('MKTICKET', home1, body=badNSTicket, headers=authheaders1)
        self.checkStatus(400) # MKTICKET bad XML

        # --- No XML body

        self.request('MKTICKET', home1, headers=authheaders1)
        self.checkStatus(400) # MKTICKET no body
    
        # --- No access privileges

        self.request('MKTICKET', home2, body=rTicket, headers=authheaders1)
        self.checkStatus(403) # MKTICKET no access

        # --- No access privileges, no body
    
        self.request('MKTICKET', home2, headers=authheaders1)
        self.checkStatus(403) # MKTICKET no access, no body

        # --- No such resource, no body

        self.request('MKTICKET', '%s%s' % (home1, 'doesnotexist'), headers=authheaders1)
        self.checkStatus(404) # MKTICKET no such resource, no body

        # --- No such resource

        self.request('MKTICKET', '%s%s' % (home1, 'doesnotexist'), body=rTicket, headers=authheaders1)
        self.checkStatus(404) # MKTICKET no such resource
    
        # --- No access, no such resource

        self.request('MKTICKET', '%s%s' % (home2, 'doesnotexist'), headers=authheaders1)
        self.checkStatus(403) # MKTICKET no access, no such resource


        # -------------  GET/PUT/DELETE Tests

        # --- GET with OK ticket
    
        self.request('GET', '%s?ticket=%s' % (home1, ticket))
        self.checkStatus(200) # GET OK

        # --- GET with read-write ticket

        self.request('GET', '%s?ticket=%s' % (home1, rwTicketId))
        self.checkStatus(200) # GET OK (read-write)

        # --- GET with nonexisting ticket
    
        nosuchticket = 'nosuchticket5dfe45210787'
        self.request('GET', '%s?ticket=%s' % (home1, nosuchticket))
        self.checkStatus(401) # GET no such ticket

        # ---  PUT with read-only ticket

        uniqueFile = 'test%s.xml' % md5.md5(str(random.random())).hexdigest()
        doc = '<my><doc/></my>'
        self.request('PUT', '%s%s?ticket=%s' % (home1, uniqueFile, ticket), body=doc)
        self.checkStatus(403) # PUT no access with read-only ticket

        # --- PUT with nonexisting ticket
    
        self.request('PUT', '%s%s?ticket=%s' % (home1, uniqueFile, nosuchticket))
        self.checkStatus(401) # PUT no such ticket

        # --- PUT with read-write ticket

        self.request('PUT', '%s%s?ticket=%s' % (home1, uniqueFile, rwTicketId), body=doc)
        self.checkStatus(201) # PUT ok (created) with read-write ticket
        self.request('PUT', '%s%s?ticket=%s' % (home1, uniqueFile, rwTicketId), body=doc)
        self.checkStatus(204) # PUT ok (updated) with read-write ticket

        # --- DELETE with read-only ticket

        self.request('DELETE', '%s%s?ticket=%s' % (home1, uniqueFile, ticket))
        self.checkStatus(403) # DELETE no access with read-only ticket

        # --- DELETE with nonexisting ticket
    
        self.request('DELETE', '%s%s?ticket=%s' % (home1, uniqueFile, nosuchticket))
        self.checkStatus(401) # DELETE no such ticket

        # --- DELETE with read-write ticket

        self.request('DELETE', '%s%s?ticket=%s' % (home1, uniqueFile, rwTicketId))
        self.checkStatus(204) # DELETE ok with read-only ticket

        # --- GET deleted file

        self.request('GET', '%s%s?ticket=%s' % (home1, uniqueFile, rwTicketId))
        self.checkStatus(404) # GET deleted file


        # --------------- DELTICKET Tests

        # --- No access

        t = authheaders2.copy()
        t['Ticket'] = ticket
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket), headers=t)
        self.checkStatus(403) # DELTICKET no access
        
        # --- OK (No Content)

        t = authheaders1.copy()
        t['Ticket'] = ticket
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket), headers=t)
        self.checkStatus(204) # DELTICKET OK (No Content)
   
        t = {'Ticket': rwTicketId}
        self.request('DELTICKET', '%s?ticket=%s' % (home1, rwTicketId))
        self.checkStatus(204) # DELTICKET OK (No Content, read-write ticket auth)

        # --- Ticket does not exist

        t = authheaders1.copy()
        nosuchticket = 'nosuchticket5dfe45210787'
        t['Ticket'] = nosuchticket
        self.request('DELTICKET', '%s?ticket=%s' % (home1, nosuchticket), headers=t)
        self.checkStatus(412) # DELTICKET no such ticket
    
        # --- Ticket does not exist, body

        t = authheaders1.copy()
        t['Ticket'] = 'nosuchticket5dfe45210787'
        self.request('DELTICKET', '%s?ticket=%s' % (home1, nosuchticket), body=rTicket, headers=t)
        self.checkStatus(412) # DELTICKET no such ticket, body
    
        # --- Ticket does not exist, resource does not exist

        t = authheaders1.copy()
        t['Ticket'] = 'nosuchticket5dfe45210787'
        self.request('DELTICKET', '%sdoesnotexist?ticket=%s' % (home1, nosuchticket), headers=t)
        self.checkStatus(404) # DELTICKET no such ticket or resource
    
        # --- Ticket does not exist, resource does not exist, body

        t = authheaders1.copy()
        t['Ticket'] = 'nosuchticket5dfe45210787'
        self.request('DELTICKET', '%sdoesnotexist?ticket=%s' % (home1, nosuchticket), body=rTicket, headers=t)
        self.checkStatus(404) # DELTICKET no such ticket or resource, body
        
        # -------------------    Miscellaneous Tests

        # --- Try to delete an already deleted ticket

        self.request('MKTICKET', home1, body=rTicket, headers=authheaders1)
        self.checkStatus(200) # MKTICKET OK
        ticket = self.test_response.getheader('Ticket')
        t = authheaders1.copy()
        t['Ticket'] = ticket
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket), headers=t)
        self.checkStatus(204) # DELTICKET OK (No Content)
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket), headers=t)
        self.checkStatus(412) # DELTICKET ticket already deleted
    
        # --- Mismatched ticket in URL and header

        self.request('MKTICKET', home1, body=rTicket, headers=authheaders1)
        self.checkStatus(200) # MKTICKET OK
        ticket = self.test_response.getheader('Ticket')
        self.request('MKTICKET', home1, body=rTicket, headers=authheaders1)
        self.checkStatus(200) # MKTICKET OK
        ticket2 = self.test_response.getheader('Ticket')
        t = authheaders1.copy()
        t['Ticket'] = ticket
        self.request('DELTICKET', '%s?ticket=%s%s' % (home1, 'nosuch', ticket), headers=t)
        self.checkStatus(412) # DELTICKET nonexisting ticket in URL, ok in header
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket2), headers=t)
        self.checkStatus(204) # DELTICKET OK, URL ticket differs from header
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket2), headers=t)
        self.checkStatus(412) # DELTICKET ticket already deleted
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket), headers=t)
        self.checkStatus(204) # DELTICKET OK (No Content)
        self.request('DELTICKET', '%s?ticket=%s' % (home1, ticket), headers=t)
        self.checkStatus(412) # DELTICKET ticket already deleted


    def timeoutRun(self):
        
        #Add auth to global headers
        authheaders1 = self.headeraddauth("cosmo-ticketTestAccount", "cosmo-ticket")
        authheaders2 = self.headeraddauth("cosmo-ticketTestAccount2", "cosmo-ticket")
        
        rTicket = '<?xml version="1.0" encoding="UTF-8"?> \
                   <X:ticketinfo xmlns:D="DAV:" xmlns:X="http://www.xythos.com/namespaces/StorageServer"> \
                       <D:privilege><D:read/></D:privilege> \
                       <X:timeout>Second-60</X:timeout> \
                   </X:ticketinfo>'
        badNSTicket = '<?xml version="1.0" encoding="UTF-8"?> \
                       <D:ticketinfo xmlns:D="DAV:"> \
                           <D:privilege><D:read/></D:privilege> \
                           <D:timeout>Second-60</D:timeout> \
                       </D:ticketinfo>'

        rwTicket = '<?xml version="1.0" encoding="UTF-8"?> \
                    <X:ticketinfo xmlns:D="DAV:" xmlns:X="http://www.xythos.com/namespaces/StorageServer"> \
                        <D:privilege><D:read/><D:write/></D:privilege> \
                        <X:timeout>Second-60</X:timeout> \
                    </X:ticketinfo>'     
        
        #Create Calendar on CalDAV server   
        home1 = self.pathbuilder('/home/cosmo-ticketTestAccount/')
        home2 = self.pathbuilder('/home/cosmo-ticketTestAccount2/')

        # --- Timeout test cases must be run last, startRun must be run first
        
        # --- GET a resource with ticket

        shortTicket = '<?xml version="1.0" encoding="UTF-8"?> \
                       <X:ticketinfo xmlns:D="DAV:" xmlns:X="http://www.xythos.com/namespaces/StorageServer"> \
                           <D:privilege><D:read/></D:privilege> \
                           <X:timeout>Second-10</X:timeout> \
                       </X:ticketinfo>'
                       
        self.request('MKTICKET', home1, body=shortTicket, headers=authheaders1)
        self.checkStatus(200) # MKTICKET OK
        ticket = self.test_response.getheader('Ticket')
        t = authheaders1.copy()
        t['Ticket'] = ticket
        self.request('GET', '%s?ticket=%s' % (home1, ticket))
        self.checkStatus(200) # GET with ticket OK
    
        # --- GET with timed out ticket

        time.sleep(12)
        self.request('HEAD', '%s?ticket=%s' % (home1, ticket))
        self.checkStatus(401) # GET ticket timed out
    
        # --- DELTICKET the timed out ticket

        self.request('DELTICKET', home1, headers=t)
        self.checkStatus(412) # DELTICKET ticket already timed out

# Commented out until bug 5172 gets fixed.
#DELTICKET immediately after ticket times out
#
#    >>> secTicket = """<?xml version="1.0" encoding="UTF-8"?>
#    ... <X:ticketinfo xmlns:D="DAV:" 
#    ...               xmlns:X="http://www.xythos.com/namespaces/StorageServer">
#    ... <D:privilege><D:read/></D:privilege>
#    ... <X:timeout>Second-1</X:timeout>
#    ... </X:ticketinfo>"""
#    >>> r = request('MKTICKET', home1, body=secTicket,
#    ...             headers=authheaders)
#    >>> r.status # MKTICKET OK
#    200
#    >>> ticket = r.getheader('Ticket')
#    >>> t = authheaders1.copy()
#    >>> t['Ticket'] = ticket
#    >>> time.sleep(2)
#    >>> r = request('DELTICKET', home1,
#    ...             headers=t)
#    >>> r.status # DELTICKET ticket already timed out
#    412

        
        

        
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
    
    cosmoticket = CosmoTicket(host=host, port=port, path=path)
    cosmoticket.debug = debug
    cosmoticket.startRun()
    cosmoticket.end()
    