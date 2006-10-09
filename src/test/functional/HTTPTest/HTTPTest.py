import base64, md5, copy
import getopt, httplib, urlparse, socket, xml

from elementtree import ElementTree

from TestObject import TestObject

class HTTPTest(TestObject):
    
    def __init__(self, host, port, path, debug=0, headers=None, tls=False, mask=0, recurrence=1, appendDict={}, appendList=[], appendVar='', printAppend='', threadNum=None):
        
        TestObject.__init__(self, debug=debug, mask=mask, recurrence=recurrence, appendVar=appendVar, printAppend=printAppend, threadNum=threadNum, appendDict=appendDict, appendList=appendList)
        
        if headers is None:
            self.headers = {'Host' : "localhost:8080",
                             'Accept' : "*/*"}
        else:
            self.headers = headers
        
        self.connection = {'host':host, 'port':port, 'path':path, 'tls':tls, 'keep-alive':False}
        self.request('OPTIONS', path, body=None, headers=self.headers)
        
    def headerAdd(self, headers):
        """
        Method to return dict copy of self.headers with header added
        """
        headers_return = copy.copy(self.headers)
        headers_return.update(headers)
        return headers_return
    
    def headerAddAuth(self, username, password, headers=None):
        """
        Method to return dict with 'Authorization' header added, if no headers are defined a copy of self.headers is used and returned
        """
        if headers is None:
            headers = copy.copy(self.headers)
        auth = 'Basic %s' % base64.encodestring('%s:%s' % (username, password)).strip()

        headers["Authorization"] = auth
        return headers
        
    def pathBuilder(self, path):
        
        return '%s%s' % (self.connection["path"], path)
    
    def checkStatus(self, status):
        
        out = self.test_response.status
        if out == status:
            self.report(True, test='Status Code Check on %s' % status, comment=None)
            return True
        else:
            self.report(False, test='Status Code Check on %s' % status, comment='expected %s ; received %s' % (status, out))
            return False
            
    def verifyListInResponse(self, positive=None, negative=None, comment=None):
        
        self.listComparison(self.test_response.read().replace('\n ', ''), test='verifyListInResponse', positive=positive, negative=negative, comment='%s,%s' % (positive, negative))  
            
    def xmlParse(self):
        """
        Get xml in body
        """
        
        self.xml_doc = ElementTree.XML(self.test_response.read())
    
    def startKeepAlive(self):
    
        self.connection['keep-alive'] = True
        
    def endKeepAlive(self):
        
        self.connection['keep-alive'] = False
                                        
    def request(self, method, url, body=None, headers={}, 
                autoheaders=('Content-Length', 'Content-Type', 'User-Agent',
                              'Host'),
                xmlExpectedStatusCodes=(200, 207,)):
        """
        Helper function to make requests easier to make.
        
        @return: Customized httplib.HTTPResponse object: read() will always return
                 full data that was received.
        """
        
        if self.connection['keep-alive'] is False:
        
            if not self.connection["tls"]:
                c = httplib.HTTPConnection(self.connection["host"], self.connection["port"])
                h = headers.copy()
                c.request(method, url, body, h)                
            else:
                c = httplib.HTTPSConnection(self.connection["host"], self.connection["port"])
                h = headers.copy()
                c.request(method, url, body, h)
        
        elif self.connection['keep-alive'] is True:
            headers['Connection'] = "keep-alive"
            h = headers.copy()
            c.request(method, url, body, h)
        
        r = c.getresponse()
            
        #for header in autoheaders:
        #    if header == 'Content-Length' and body is not None:
        #        h[header] = '%d' % len(body)
        #    if header == 'Content-Type' and body is not None:
        #        h[header] = 'text/xml'
        #    if header == 'User-Agent':
        #        h[header] = 'silmut'
        #    if header == 'Host':
        #        h[header] = '%s:%s' % (self.connection["host"], self.connection["port"])
        
        
        
        
        # Automatically follow 302 GET (same host only)
        if method == 'GET' and r.status == 302:
            q = url.find('?')
            query = ''
            if q != -1:
                query = url[q:]
            redirectHost, redirectPort, url, redirectTLS = parseURL(r.getheader('Location'))
            if redirectHost != host or redirectPort != port:
                raise Exception('Redirect allowed to same server only')
            if url.find('?') == -1:
                url = '%s%s' % (url, query)
            return request(method, url, body, headers)
    
        # This is needed because once read(), more read()s would just return empty.
        r.body = r.read()
        r.read = lambda: r.body
        
        self.test_response = r
        self.test_connection = c
        
        if self.debug > 3:
            print 'Request::\n%s' % body
            print 'RequestHeaders::\n%s' % headers
            print 'Response::\n%s' % r.read()
        elif self.debug > 2:
            print r.read()
        
        return r
    


            
            
            
    
    