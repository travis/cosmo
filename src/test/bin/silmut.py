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

import sys, getopt, httplib, base64, time

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
        redirectHost, redirectPort, url, redirectTLS = parseURL(r.getheader('Location'))
        if redirectHost != host or redirectPort != port:
            raise Exception('Redirect allowed to same server only')
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
    import urlparse
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


def caldav():
    '''
    TODO
    
    urn:ietf:params:xml:ns:caldav
    '''

def delticket():
    '''
    Temporary, to debug problems with tickets.
    
    Initialization
        
    >>> auth = 'Basic %s' % base64.encodestring('%s:%s' % (user1, password1)).strip()
    >>> authHeaders = {'Authorization': auth}
    >>> minTicket = """<?xml version="1.0" encoding="UTF-8"?>
    ... <X:ticketinfo xmlns:D="DAV:" 
    ...               xmlns:X="http://www.xythos.com/namespaces/StorageServer">
    ... <D:privilege><D:read/></D:privilege>
    ... <X:timeout>Second-60</X:timeout>
    ... </X:ticketinfo>"""
    >>> home1 = '%s/home/%s' % (path, user1)

    Create ticket, works

    >>> r = request('MKTICKET', home1, body=minTicket, headers=authHeaders)
    >>> r.status # MKTICKET OK
    200
    >>> ticket = r.getheader('Ticket')

    GET with ticket, does not seem to work, status 401 (unauthorized)

    >>> t = {'Ticket': ticket}
    >>> r = request('GET', home1, headers=t)
    >>> r.status # GET with ticket OK 
    200
                     
    >>> r = request('GET', '%s?ticket=%s' % (home1, ticket))
    >>> r.status # GET with ticket OK 
    200

    DELTICKET does not seem to work, status 501 (not implemented)
    
    >>> t = {'Ticket': ticket, 'Authorization': auth}
    >>> r = request('DELTICKET', home1, headers=t)
    >>> r.status # DELTICKET OK (No Content)
    204
    '''

#def tickets():
    # Tests still TODO (from cosmo 0.2 spec):
    # -make sure MKTICKET request only shows tickets for current account
    # -make sure ticket timeouts followed
    # -make sure PROPFIND on ticketdiscovery property not supported
    # -make sure that visits element always returns infinity
    # -make sure that http://www.xythos.com/namespaces/StorageServer is used for 
    #  the ticketdiscovery, ticketinfo, id and timeout elements
    # -valid values for timeout element are infinity and Seconds-xxxx
    # -if different ticket in headers and URL, URL is used
    #
    # Also more tests from ticket spec.
    '''
    Initialization
        
    >>> auth = 'Basic %s' % base64.encodestring('%s:%s' % (user1, password1)).strip()
    >>> authHeaders = {'Authorization': auth}
    >>> auth2 = 'Basic %s' % base64.encodestring('%s:%s' % (user2, password2)).strip()
    >>> authHeaders2 = {'Authorization': auth2}
    >>> minTicket = """<?xml version="1.0" encoding="UTF-8"?>
    ... <X:ticketinfo xmlns:D="DAV:" 
    ...               xmlns:X="http://www.xythos.com/namespaces/StorageServer">
    ... <D:privilege><D:read/></D:privilege>
    ... <X:timeout>Second-60</X:timeout>
    ... </X:ticketinfo>"""
    >>> badNSTicket = """<?xml version="1.0" encoding="UTF-8"?>
    ... <D:ticketinfo xmlns:D="DAV:">
    ... <D:privilege><D:read/></D:privilege>
    ... <D:timeout>Second-60</D:timeout>
    ... </D:ticketinfo>"""
    >>> home1 = '%s/home/%s/' % (path, user1)
    >>> home2 = '%s/home/%s/' % (path, user2)
    
    MKTICKET
    
    Status codes

    OK
    
    >>> r = request('MKTICKET', home1, body=minTicket,
    ...             headers=authHeaders)
    >>> r.status # MKTICKET OK
    200
    >>> ticket = r.getheader('Ticket')

    Bad XML
    
    >>> r = request('MKTICKET', home1, body=badNSTicket,
    ...             headers=authHeaders)
    >>> r.status # MKTICKET bad XML
    400

    No XML body
    
    >>> r = request('MKTICKET', home1, headers=authHeaders)
    >>> r.status # MKTICKET no body
    400
    
    No access privileges
    
    >>> r = request('MKTICKET', home2, body=minTicket,
    ...             headers=authHeaders)
    >>> r.status # MKTICKET no access
    403

    No access privileges, no body
        
    >>> r = request('MKTICKET', home2, headers=authHeaders)
    >>> r.status # MKTICKET no access, no body
    403

    No such resource, no body
    
    >>> r = request('MKTICKET', '%s%s' % (home1, 'doesnotexist'),
    ...              headers=authHeaders)
    >>> r.status # MKTICKET no such resource, no body
    404

    No such resource
    
    >>> r = request('MKTICKET', '%s%s' % (home1, 'doesnotexist'), 
    ...             body=minTicket, headers=authHeaders)
    >>> r.status # MKTICKET no such resource
    404
    
    No access, no such resource
    
    >>> r = request('MKTICKET', '%s%s' % (home2, 'doesnotexist'),
    ...             headers=authHeaders)
    >>> r.status # MKTICKET no access, no such resource
    403
    
    
    DELTICKET
    
    Status Codes
    
    No access
    
    >>> t = authHeaders2.copy()
    >>> t['Ticket'] = ticket
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # DELTICKET no access
    403
        
    OK (No Content)
    
    >>> t = authHeaders.copy()
    >>> t['Ticket'] = ticket
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # DELTICKET OK (No Content)
    204
    
    Ticket does not exist
    
    >>> t = authHeaders.copy()
    >>> nosuchticket = 'nosuchticket5dfe45210787'
    >>> t['Ticket'] = nosuchticket
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, nosuchticket),
    ...             headers=t)
    >>> r.status # DELTICKET no such ticket
    412
    
    Ticket does not exist, body
    
    >>> t = authHeaders.copy()
    >>> t['Ticket'] = 'nosuchticket5dfe45210787'
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, nosuchticket),
    ...             body=minTicket, headers=t)
    >>> r.status # DELTICKET no such ticket, body
    412
    
    Ticket does not exist, resource does not exist
    
    >>> t = authHeaders.copy()
    >>> t['Ticket'] = 'nosuchticket5dfe45210787'
    >>> r = request('DELTICKET', '%sdoesnotexist?ticket=%s' % (home1, nosuchticket),
    ...             headers=t)
    >>> r.status # DELTICKET no such ticket or resource
    404
    
    Ticket does not exist, resource does not exist, body
    
    >>> t = authHeaders.copy()
    >>> t['Ticket'] = 'nosuchticket5dfe45210787'
    >>> r = request('DELTICKET', '%sdoesnotexist?ticket=%s' % (home1, nosuchticket),
    ...             body=minTicket, headers=t)
    >>> r.status # DELTICKET no such ticket or resource, body
    404    


    Miscellaneous

    Try to delete an already deleted ticket
    
    >>> r = request('MKTICKET', home1, body=minTicket,
    ...             headers=authHeaders)
    >>> r.status # MKTICKET OK
    200
    >>> ticket = r.getheader('Ticket')
    >>> t = authHeaders.copy()
    >>> t['Ticket'] = ticket
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # DELTICKET OK (No Content)
    204
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # DELTICKET ticket already deleted
    412
    
    GET a resource with ticket
    
    >>> r = request('MKTICKET', home1, body=minTicket,
    ...             headers=authHeaders)
    >>> r.status # MKTICKET OK
    200
    >>> ticket = r.getheader('Ticket')
    >>> t = authHeaders.copy()
    >>> t['Ticket'] = ticket
    >>> r = request('GET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # GET with ticket OK
    200
    
    GET with timed out ticket
    
    >>> time.sleep(61)
    >>> r = request('GET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # GET ticket timed out
    412
    
    DELTICKET the timed out ticket
    
    >>> r = request('DELTICKET', '%s?ticket=%s' % (home1, ticket),
    ...             headers=t)
    >>> r.status # DELTICKET ticket already timed out
    412
    '''


def main(argv):
    global host, port, path, tls, url, user1, password1, user2, password2
    
    try:
        opts, args = getopt.getopt(argv, 'u:1:2:h',)
    except getopt.GetoptError:
        usage()
        sys.exit(1)

    up1 = '%s:%s' % (user1, password1)
    up2 = '%s:%s' % (user2, password2)
    
    for (opt, arg) in opts:
        if   opt == '-u': url = arg
        if   opt == '-1': up1 = arg
        if   opt == '-2': up2 = arg
        elif opt == '-h':
            usage()
            sys.exit()

    host, port, path, tls = parseURL(url)
    user1, password1 = parseUser(up1)
    user2, password2 = parseUser(up2)
    
    import socket
    try:
        request('OPTIONS', '/')
    except socket.error, e:
        print 'Error:', e
        sys.exit(1)

    import doctest
    doctest.testmod()
        

if __name__ == "__main__":
    main(sys.argv[1:])    
