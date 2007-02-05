#!/usr/bin/env python
#
#   Copyright 2006 Open Source Applications Foundation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#  
# silmut?
#   1) It's an anagram of litmus, which is a WebDAV test suite.
#   2) It's Finnish for buds.

import sys, getopt, httplib, urlparse, doctest, socket, xml

try:
    from elementtree import ElementTree
except ImportError:
    ElementTree = None

# The silmut framework that is usable from the test suites.
__all__ = ['host', 'port', 'path', 'url', 'adminuser', 'user1', 'user2',
           'adminpassword', 'password1', 'password2', 'request']

# Defaults
host = 'localhost'
port = 8080
path = '/cosmo'
url = 'http://%s:%s%s' % (host, port, path)
tls  = False
user1 = 'test1'
password1 = 'test1'
user2 = 'test2'
password2 = 'test2'
adminuser = 'root'
adminpassword = 'cosmo'
parseXML = True


def headerValidator(response, method=None, sentHeaders=None):
    """
    Validate response headers
    """
    # If we have a body, make sure Content-Length is correct
    bodyLen = len(response.read())
    if bodyLen > 0 and response.getheader('Transfer-Encoding') != 'chunked':
        contentLen = long(response.getheader('Content-Length'))
        if bodyLen != contentLen:
            raise Exception('Content-Length (%d) does not match body (%d)' % \
                            (bodyLen, contentLen))
    
    if hasattr(response, 'getheaders'):
        for header, value in response.getheaders():
            if header == 'etag':
                if len(value) == 0:
                    raise Exception('ETag should not be empty') 


def request(method, url, body=None, headers={}, 
            autoheaders=('Content-Length', 'Content-Type', 'User-Agent',
                          'Host'),
            xmlExpectedStatusCodes=(200, 207,),
            headerValidator=headerValidator):
    """
    Helper function to make requests easier to make.
    
    @return: Customized httplib.HTTPResponse object: read() will always return
             full data that was received.
    """
    if not tls:
        c = httplib.HTTPConnection(host, port)
    else:
        c = httplib.HTTPSConnection(host, port)
    h = headers.copy()
    for header in autoheaders:
        if header == 'Content-Length' and body is not None:
            h[header] = '%d' % len(body)
        if header == 'Content-Type' and body is not None:
            h[header] = 'text/xml'
        if header == 'User-Agent':
            h[header] = 'silmut'
        if header == 'Host':
            h[header] = '%s:%s' % (host, port)
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
    
    if parseXML:
        xmlMethods = ('MKTICKET', 'PROPFIND')
        if (method in xmlMethods and r.status in xmlExpectedStatusCodes) or \
           xmlContent(r):
            #print method, r.status, r.getheader('Content-Type')
            parse(r.read())
    
    headerValidator(r, method, headers)
    
    return r


def xmlContent(response):
    """
    Does the response have an XML Content-Type header?
    """
    try:
        xml = response.getheader('Content-Type').find('xml') != -1
    except AttributeError:
        return False
    else:
        return xml


def parse(text):
    """
    Parse text as XML.
    
    >>> parse('<doc/>')
    <Element doc ...>

    >>> parse('')
    Traceback (most recent call last):
        ...
    Exception: 
    <BLANKLINE>
    xml.parsers.expat.ExpatError: no element found: line 1, column 0
    >>> parse('<doc><foo></doc>')
    Traceback (most recent call last):
        ...
    Exception: 
    <doc><foo></doc>
    xml.parsers.expat.ExpatError: mismatched tag: line 1, column 12
    """
    try:
        doc = ElementTree.XML(text)
    except xml.parsers.expat.ExpatError, e:
        # This cuts out a little bit of the Python traceback
        # and shows the bad XML at the end, right above the
        # error description.
        raise Exception('\n%s\n%s: %s' % (text, e.__class__, e))
    
    return doc


def usage():
    """
    Silmut is a CalDAV, TICKET etc. compliance testsuite for Cosmo.

    Usage: python silmut.py [options]
    
    Options:
      -u      url (default is http://localhost:8080/cosmo)
      -0      adminuser:adminpassword (default is root:cosmo)
      -1      user1:password1 (default is test1:test1)
      -2      user2:password2 (default is test2:test2)
      -t      timeout (default is None)
      -r      tests to run (default is caldav,ticket)
      -v      verbose mode (default is quiet mode)
      -x      do not parse XML, which requires elementtree (parse by default)
      -h      display this help text
    
    This test requires that two test accounts already exist on the server.
    """
    print usage.__doc__


def parseUser(userPassword):
    """
    Parse user and password.
    
    >>> parseUser('test1:test1password')
    ('test1', 'test1password')
    """
    colon = userPassword.find(':')
    user = userPassword[:colon]
    password = userPassword[colon + 1:]
    return user, password


def parseURL(url):
    """
    Parse URL to host, port, path and tls.
    
    >>> parseURL('http://localhost:8080/cosmo')
    ('localhost', 8080, '/cosmo', False)
    >>> parseURL('https://localhost')
    ('localhost', 443, '', True)
    >>> parseURL('localhost')
    ('localhost', 80, '', False)
    >>> parseURL('localhost/')
    ('localhost', 80, '', False)
    >>> parseURL('http://localhost:8080')
    ('localhost', 8080, '', False)
    >>> parseURL('http://localhost:8080/')
    ('localhost', 8080, '', False)
    """
    parsed = urlparse.urlparse(url, scheme='http', allow_fragments=0)
    
    if parsed[0] == 'http':
        tls = False
        port = 80
    elif parsed[0] == 'https':
        tls = True
        port = 443
    else:
        raise Exception('Unknown protocol')
    
    host = parsed[1]
    
    colon = host.rfind(':')
    if colon != -1:
        port = int(host[colon + 1:])
        host = host[:colon]
        
    path = parsed[2]
    
    # Work around some urlparse bugs
    if host == '':
        host = path
        slash = host.rfind('/')
        if slash != -1:
            host = host[:slash]
        path = ''
        
    if path == '/':
        path = ''
            
    return host, port, path, tls


def main(argv):
    global host, port, path, tls, url, user1, password1, user2, password2, \
           parseXML, adminuser, adminpassword
    
    try:
        opts, args = getopt.getopt(argv, 'u:0:1:2:r:t:vdh',)
    except getopt.GetoptError:
        usage()
        sys.exit(1)

    up0 = '%s:%s' % (adminuser, adminpassword)
    up1 = '%s:%s' % (user1, password1)
    up2 = '%s:%s' % (user2, password2)
    
    verbose = False
    
    tests = 'caldav,ticket'
    
    for (opt, arg) in opts:
        if   opt == '-u': url = arg
        elif opt == '-0': up0 = arg
        elif opt == '-1': up1 = arg
        elif opt == '-2': up2 = arg
        elif opt == '-t':
            # This is nasty, but httplib.HTTP(S)Connection keeps
            # socket private, so we cannot set per socket timeouts. 
            socket.setdefaulttimeout(float(arg))
        elif opt == '-r': tests = arg
        elif opt == '-v': verbose = True
        elif opt == '-x': parseXML = False
        elif opt == '-d':
            doctest.testmod(optionflags=doctest.ELLIPSIS)
            sys.exit()
        elif opt == '-h':
            usage()
            sys.exit()

    if parseXML and ElementTree is None:
        print 'XML support requires the elementtree module.'
        sys.exit(1)

    host, port, path, tls = parseURL(url)
    adminuser, adminpassword = parseUser(up0)
    user1, password1 = parseUser(up1)
    user2, password2 = parseUser(up2)
    
    try:
        request('OPTIONS', '/')
    except socket.error, e:
        print 'Error:', e
        sys.exit(1)

    for test in tests.split(','):
        doctest.testfile('%s.txt' % test.strip(), verbose=verbose)


if __name__ == "__main__":
    main(sys.argv[1:])    
