import base64, md5, copy
import getopt, httplib, urlparse, socket, xml

from elementtree import ElementTree

class HTTPTest:
    
    def __init__(self, host, port, path, debug=0, headers=None, tls=False, mask=0):
        
        if headers == None:
            self.headers = {'Host' : "localhost:8080",
                             'User-Agent': "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1",
                             'Accept' : "*/*"}
        else:
            self.headers = headers
        
        self.connection = {"host" : host, "port" : port, "path" : path, "tls" : tls}
        self.debug = debug
        self.results = []
        self.resultnames = []
        self.resultcomments = []
        self.request('OPTIONS', path, body=None, headers=self.headers)
        self.mask = mask
        
    def headeradd(self, headers):
        """
        Method to return dict copy of self.headers with header added
        """
        headers_return = copy.copy(self.headers)
        headers_return.update(headers)
        return headers_return
    
    def headeraddauth(self, username, password, headers=None):
        """
        Method to return dict with 'Authorization' header added, if no headers are defined a copy of self.headers is used and returned
        """
        if headers == None:
            headers = copy.copy(self.headers)
        auth = 'Basic %s' % base64.encodestring('%s:%s' % (username, password)).strip()

        headers["Authorization"] = auth
        return headers
        
    def pathbuilder(self, path):
        
        return '%s%s' % (self.connection["path"], path)
    
    def checkStatus(self, status):
        
        out = self.test_response.status
        if out == status:
            self.report(True, test='Status Code Check on %s' % status, comment=None)
            return True
        else:
            self.report(False, test='Status Code Check on %s' % status, comment='expected %s ; received %s' % (status, out))
            return False
    
    def printout(self, string):
        
        if self.mask == 0:
            print string
        
    def end(self):
        
        count = 0
        failures = 0
        passes = 0
        for result in self.results:
            if result == False:
                failures = failures + 1
            elif result == True:
                passes = passes +1
            
            count = count + 1
        
        self.printout("Failures :: %s" % failures)
        self.printout("Passes :: %s" % passes)
        self.printout("Total tests run :: %s" % count)
            
    def xmlparse(self):
        """
        Get xml in body
        """
        
        self.xml_doc = ElementTree.XML(self.test_response.read())
   
    def report(self, result, test=None, comment=None):
        
        self.results.append(result)
        self.resultnames.append(test)
        self.resultcomments.append(comment)
        if result == True:
            if self.debug > 0:
                self.printout("Passed :: Test %s :: %s" % (test, comment))
        if result == False:
            self.printout("Failure :: Test %s :: %s" % (test, comment))
                                        
                                        
    def request(self, method, url, body=None, headers={}, 
                autoheaders=('Content-Length', 'Content-Type', 'User-Agent',
                              'Host'),
                xmlExpectedStatusCodes=(200, 207,)):
        """
        Helper function to make requests easier to make.
        
        @return: Customized httplib.HTTPResponse object: read() will always return
                 full data that was received.
        """
        
        if not self.connection["tls"]:
            c = httplib.HTTPConnection(self.connection["host"], self.connection["port"])
        else:
            c = httplib.HTTPSConnection(self.connection["host"], self.connection["port"])
        h = headers.copy()
        #for header in autoheaders:
        #    if header == 'Content-Length' and body is not None:
        #        h[header] = '%d' % len(body)
        #    if header == 'Content-Type' and body is not None:
        #        h[header] = 'text/xml'
        #    if header == 'User-Agent':
        #        h[header] = 'silmut'
        #    if header == 'Host':
        #        h[header] = '%s:%s' % (self.connection["host"], self.connection["port"])
        c.request(method, url, body, h)
        r = c.getresponse()
        
        
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
        return r
    
    