#! /usr/bin/env python

# 
# Copyright 2005 Kapor Enterprises, Inc. and Todd Agulnick.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at 
# 
# 	http://www.apache.org/licenses/LICENSE-2.0
# 	
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# createaccounts.py: Create test accounts on a Cosmo server.
#


"""
Create Cosmo Test Accounts

Creates test user accounts on a Cosmo server. Each test account is created
with username "test{seqnum}" where {seqnum} is a number created from the
sequence determined by the first sequence number parameter and the number
of accounts parameter. The password for each account is the same as the
account's username.

Usage: python createaccounts.py [options]

Options:
  -s        url (default is http://localhost:8080/cosmo)
  -u        username (default is root)
  -w        password (default is cosmo)
  -n        number of accounts to create (default is 100)
  -f        first account number in sequence (default is 1)
  -h        display this help text

Examples:
  createaccounts.py Create 100 accounts starting with test1.
  createaccounts.py -f 101 -n 100 Create accounts test101 through test200.
"""

import sys, getopt, httplib, base64

def createAccount(tls, server, port, path, username, password, adminuser, adminpass):
    body = """<?xml version="1.0" encoding="utf-8" ?> 
<user xmlns="http://osafoundation.org/cosmo/CMP">
  <username>%s</username>
  <password>%s</password>
  <firstName>Tommy</firstName>
  <lastName>Tester</lastName>
  <email>%s@nojunkmailplease.com</email>
</user>
""" % (username, password, username)
    body = body.encode("utf-8")

    try:
        if not tls:
            c = httplib.HTTPConnection(server, port)
        else:
            c = httplib.HTTPSConnection(server, port)
        auth = "Basic %s" % base64.encodestring("%s:%s" % (adminuser, 
                                                adminpass)).strip()
        headers = {"Authorization": auth, "Content-Type": "text/xml",
                   "Content-Length": "%d" % len(body)}
        c.request("PUT", "%s/cmp/user/%s" % (path, username), body=body, 
                  headers=headers)
        response = c.getresponse()
    except:
        import traceback
        traceback.print_exc(file=sys.stderr, limit=1)
        sys.stderr.flush()
        return False

    code = response.status
    if code == 201:
        print "Created user account %s" % username
        return True
    elif code == 204:
        print "WARNING: Account %s already exists." % username
        return True
    elif code == 401:
        print "\nERROR: Authorization error. Check username and password."
        return False
    else:
        print "Unhandled response code: %d" % code
        return False

    return True

def usage():
    print __doc__

def parseURL(url):
    """
    Parse URL to host, port, path.
    
    >>> print parseURL('http://localhost:8080/cosmo')
    ('localhost', 8080, '/cosmo')
    >>> print parseURL('https://localhost')
    ('localhost', 443, '')
    >>> print parseURL('localhost')
    ('localhost', 80, '')
    >>> print parseURL('localhost/')
    ('localhost', 80, '')
    """
    import urlparse
    parsed = urlparse.urlparse(url, scheme="http", allow_fragments=0)
    
    if parsed[0] == "http":
        port = 80
        tls = False
    elif parsed[0] == "https":
        port = 443
        tls = True
    else:
        raise Exception("Unknown protocol")
    
    host = parsed[1]
    
    colon = host.rfind(":")
    if colon != -1:
        port = int(host[colon + 1:])
        host = host[:colon]
        
    path = parsed[2]
    
    # Work around some urlparse bugs
    if host == "":
        host = path
        slash = host.rfind("/")
        if slash != -1:
            host = host[:slash]
        path = ""
            
    return host, port, path, tls

def request(tls, *args, **kw):
    """
    Helper function to make requests easier to make.
    """
    if not tls:
        c = httplib.HTTPConnection(host, port)
    else:
        c = httplib.HTTPSConnection(host, port)
    c.request(*args, **kw)
    return c.getresponse()

def main(argv):
    try:
        opts, args = getopt.getopt(argv, "s:u:w:n:f:h")
    except getopt.GetoptError:
        usage()
        sys.exit(2)

    # establish defaults
    server = "localhost"
    port = 8080
    path = "/cosmo"
    url = "http://%s:%s%s" % (server, port, path)
    username = "root"
    password = "cosmo"
    tls = False
    start = 1
    count = 100
    
    for (opt, arg) in opts:
        if opt =="-s":      url = arg
        elif opt == "-u":   username = arg
        elif opt == "-w":   password = arg
        elif opt == "-n":   count = int(arg)
        elif opt == "-f":   start = int(arg)
        elif opt == "-h":
            usage()
            sys.exit()

    server, port, path, tls = parseURL(url)
                
    for i in range(start, start + count):
        if createAccount(tls, server, port, path, "test" + str(i), 
            "test" + str(i), username, password) == False:
            sys.exit(2)

if __name__ == "__main__":
    main(sys.argv[1:])    
