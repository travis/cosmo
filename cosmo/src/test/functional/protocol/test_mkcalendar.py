#   Copyright (c) 2006-2007 Open Source Applications Foundation
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

import cosmoclient
import random
import os, sys
from xml.etree import ElementTree

from cosmo_test_lib import *

CALENDAR = 'calendar'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'

def test_mkcalendar_invalid_badxml():
    body = open(FILES_DIR+'mkcalendar/invalidBadXML.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 400

def test_mkcalendar_invalid_caldav_prop_missing():
    body = open(FILES_DIR+'mkcalendar/invalidCaldavProperty.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201
    
def test_mkcalendar_invalid_dav_prop():
    body = open(FILES_DIR+'mkcalendar/invalidDavProperty.xml').read()
    client._request('MKCALENDAR', '%s/%s/' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201


# Cosmo doesn't support setting CALDAV:supported-calendar-component-set 
#def test_mkcalendar_invalid_supported_cal_component():
#    body = open(FILES_DIR+'mkcalendar/invalidSupportedCalendarComponent.xml').read()
#    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
#    assert client.response.status == 400

# Cosmo doesn't support setting CALDAV:supported-calendar-data, and in fact
# I think RFC 4719 forbids this
#def test_mkcalendar_invalid_supported_cal_data():
#    body = open(FILES_DIR+'mkcalendar/invalidSupportedCalendarData.xml').read()
#    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
#    assert client.response.status == 400

def test_mkcalendar_invalid_timezone():
    body = open(FILES_DIR+'mkcalendar/invalidTimezone.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 207
    assert client.response.body.find('<D:responsedescription>Calendar object not parseable: Error at line 22') is not -1

def test_mkcalendar_valid_no_body():
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=None)
    assert client.response.status == 201

def test_mkcalendar_valid_description_no_lang():
    body = open(FILES_DIR+'mkcalendar/validDescriptionNoLang.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201

def test_mkcalendar_valid_full_body():
    body = open(FILES_DIR+'mkcalendar/validFullBody.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201

def test_mkcalendar_valid_no_description():
    body = open(FILES_DIR+'mkcalendar/validNoDescription.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201
    
def test_mkcalendar_valid_no_displayname():
    body = open(FILES_DIR+'mkcalendar/validNoDisplayname.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201
    
def test_mkcalendar_valid_no_supported_component():
    body = open(FILES_DIR+'mkcalendar/validNoSupportedCalendarComponent.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201

def test_mkcalendar_valid_no_timezone():
    body = open(FILES_DIR+'mkcalendar/validNoTimezone.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, str(random.random()).replace('.', '')), body=body, headers={ 'Content-Type': 'text/xml'})
    assert client.response.status == 201

def test_mkcalendar_invalid_content_type():
    client._request('MKCALENDAR', '/'.join([PRINCIPAL_DAV_PATH, 'invalid_content_type']), 
                    headers={'Content-Type':'foo/bar'})
    assert client.response.status == 415
    
malformed_property = """<?xml version='1.0' encoding='UTF-8'?>
<ns0:mkcalendar xmlns:ns0="urn:ietf:params:xml:ns:caldav">
  <ns1:set xmlns:ns1="DAV:">
    <ns1:prop>
      <ns1:displayname>double plus ungood</ns1:displayname>
      <ns0:calendar-timezone><![CDATA[
BEGIN:VCALENDAR
VERSION:2.0
CALSCALE:GREGORIAN
PRODID:-//Apple Computer\, Inc//iCal 3.0//EN
BEGIN:VTIMEZONE
LAST-MODIFIED:20060710T225223Z
TZID:America/Vancouver
BEGIN:DAYLIGHT
TZOFFSETFROM:+0000
DTSTART:20060402T100000
TZNAME:PDT
TZOFFSETTO:-0700
END:DAYLIGHT
BEGIN:STANDARD
TZOFFSETFROM:-0700
DTSTART:20061029T020000
TZNAME:PST
TZOFFSETTO:-0800
END:STANDARD
END:VTIMEZONE
END:VCALENDAR
]]></ns0:calendar-timezone>
    </ns1:prop>
  </ns1:set>
</ns0:mkcalendar>
"""

def test_mkcalendar_malformed_property():
    client._request('MKCALENDAR', '/'.join([PRINCIPAL_DAV_PATH, 'malformed_property']), body=malformed_property,
                    headers={'Content-Type':'text/xml'})
    assert client.response.status == 207
    assert client.response.body.find('<D:status>HTTP/1.1 403 Forbidden</D:status>') is not -1
    assert client.response.body.find('<D:status>HTTP/1.1 424 Failed Dependency</D:status>') is not -1
    assert client.response.body.find('Calendar object not parseable') is not -1
        
    
    