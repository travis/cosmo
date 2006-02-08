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

import sys, getopt, httplib, urlparse, base64, time, doctest, socket

# The silmut framework that is usable from the test suites.
__all__ = ['host', 'port', 'path', 'user1', 'user2',
           'password1', 'password2', 'request']

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


def request(method, url, body=None, headers={}, 
            autoheaders=('Content-Length', 'Content-Type', 'User-Agent',
                          'Host')):
    """
    Helper function to make requests easier to make.
    """
    if not tls:
        c = httplib.HTTPConnection(host, port)
    else:
        c = httplib.HTTPSConnection(host, port)
    h = headers.copy()
    for header in autoheaders:
        if header == 'Content-Length' and body is not None:
            h['Content-Length'] = '%d' % len(body)
        if header == 'Content-Type' and body is not None:
            h['Content-Type'] = 'text/xml'
        if header == 'User-Agent':
            h['User-Agent'] = 'silmut'
        if header == 'Host':
            h['Host'] = '%s:%s' % (host, port)
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
    
    return r


def usage():
    """
    Silmut is a CalDAV and TICKET compliance testsuite for Cosmo.

    Usage: python silmut.py [options]
    
    Options:
      -u      url (default is http://localhost:8080/cosmo)
      -1      user1:password1 (default is test1:test1)
      -2      user2:password2 (default is test2:test2)
      -t      timeout (default is None)
      -r      tests to run (default is caldav,ticket)
      -v      verbose mode (default is quiet mode)
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
    global host, port, path, tls, url, user1, password1, user2, password2
    
    try:
        opts, args = getopt.getopt(argv, 'u:1:2:r:t:vdh',)
    except getopt.GetoptError:
        usage()
        sys.exit(1)

    up1 = '%s:%s' % (user1, password1)
    up2 = '%s:%s' % (user2, password2)
    
    verbose = False
    
    tests = 'caldav,ticket'
    
    for (opt, arg) in opts:
        if   opt == '-u': url = arg
        elif opt == '-1': up1 = arg
        elif opt == '-2': up2 = arg
        elif opt == '-t':
            # This is nasty, but httplib.HTTP(S)Connection keeps
            # socket private, so we cannot set per socket timeouts. 
            socket.setdefaulttimeout(float(arg))
        elif opt == '-r': tests = arg
        elif opt == '-v': verbose = True
        elif opt == '-d':
            doctest.testmod()
            sys.exit()
        elif opt == '-h':
            usage()
            sys.exit()

    host, port, path, tls = parseURL(url)
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
