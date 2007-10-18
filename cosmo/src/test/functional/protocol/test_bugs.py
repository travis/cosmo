#   Copyright (c) 2007 Open Source Applications Foundation
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

allday_event = """BEGIN:VCALENDAR
VERSION:2.0
X-WR-CALNAME:TestAllDay
PRODID:-//Apple Computer\, Inc//iCal 2.0//EN
X-WR-RELCALID:210CA9E2-90FA-4EF6-A546-99834FC011CC
X-WR-TIMEZONE:US/Pacific
CALSCALE:GREGORIAN
BEGIN:VEVENT
DTSTART;VALUE=DATE:20071017
SUMMARY:TestAllDay
UID:F28A22ED-A4FC-4D91-B979-E8CD02A940BD
SEQUENCE:2
DTSTAMP:20071017T214120Z
DURATION:P1D
END:VEVENT
END:VCALENDAR
"""

def test_allday_freebusy():
    client.put('/'.join([PRINCIPAL_DAV_PATH, CALENDAR, 'allday.ics']), body=allday_event,
               headers={'Content-Type':'text/calendar'})
    assert client.response.status == 201
    body = open(FILES_DIR+'reports/freebusy/1.xml').read()
    body = body.replace('start="20060101T000000Z" end="20060105T000000Z"',
                        'start="20071017T000000Z" end="20071019T000000Z"')
    client._request('REPORT', '/'.join([PRINCIPAL_DAV_PATH, CALENDAR]), body=body, 
                    headers={'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.body.find('FREEBUSY:20071017T070000Z/20071018T070000Z') is not -1
    
principal_report_search = """<?xml version="1.0" encoding="utf-8" ?>
<D:principal-match xmlns:D="DAV:">
    <D:principal-property>
        <D:owner/>
    </D:principal-property>
</D:principal-match>"""    
    
def test_principal_property_search():
    client._request('REPORT', '/'.join([PRINCIPAL_ROOT, 'users', TEST_USER]), body=principal_report_search,
                    headers={'Content-Type': 'text/xml'})
    
single_event = """BEGIN:VCALENDAR
VERSION:2.0
X-WR-CALNAME:IndividualTest
PRODID:-//Apple Computer\, Inc//iCal 2.0//EN
X-WR-RELCALID:E2E9F982-28AB-4AA4-B5E4-D3AC73BF5ADB
X-WR-TIMEZONE:US/Pacific
CALSCALE:GREGORIAN
METHOD:PUBLISH
BEGIN:VTIMEZONE
TZID:US/Pacific
LAST-MODIFIED:20071017T231720Z
BEGIN:DAYLIGHT
DTSTART:20070311T100000
TZOFFSETTO:-0700
TZOFFSETFROM:+0000
TZNAME:PDT
END:DAYLIGHT
BEGIN:STANDARD
DTSTART:20071104T020000
TZOFFSETTO:-0800
TZOFFSETFROM:-0700
TZNAME:PST
END:STANDARD
BEGIN:DAYLIGHT
DTSTART:20080309T010000
TZOFFSETTO:-0700
TZOFFSETFROM:-0800
TZNAME:PDT
END:DAYLIGHT
END:VTIMEZONE
BEGIN:VEVENT
DTSTART;TZID=US/Pacific:20071016T134500
SUMMARY:IndividualTest
UID:9A3D5B13-0A25-46A7-94F3-A848BDFA459D
SEQUENCE:2
DTSTAMP:20071017T231703Z
DURATION:PT1H
END:VEVENT
END:VCALENDAR
"""    
    
def test_put_ics_in_home():
    client.put('/'.join([PRINCIPAL_DAV_PATH, 'test_put_ics_in_home.ics']), body=single_event, 
               headers={'Content-Type':'text/calendar'})
    assert client.response.status == 201
    
current_priv_propfind = """<?xml version="1.0" encoding="utf-8" ?>
<D:propfind xmlns:D="DAV:">
  <D:prop>
    <D:owner/>
    <D:supported-privilege-set/>
    <D:current-user-privilege-set/>
    <D:acl/>
  </D:prop>
</D:propfind>"""    
    
def test_current_user_priv_set():
    client._request('PROPFIND', PRINCIPAL_DAV_PATH, body=current_priv_propfind, 
                    headers={'Content-Type': 'text/xml', 'Depth':'1'})
    assert client.response.body.find('<D:current-user-privilege-set>') is not -1

def test_vtodo_reports():
    all_vtodo = """<calendar-query xmlns:D="DAV:" xmlns="urn:ietf:params:xml:ns:caldav">
  <D:prop>
    <D:getetag/>
    <calendar-data/>
  </D:prop>
  <filter>
    <comp-filter name="VCALENDAR">
      <comp-filter name="VTODO" />
    </comp-filter>
  </filter>
</calendar-query>"""

    completed_vtodo = """<calendar-query xmlns:D="DAV:" xmlns="urn:ietf:params:xml:ns:caldav">
  <D:prop>
    <D:getetag/>
    <calendar-data/>
  </D:prop>
  <filter>
    <comp-filter name="VCALENDAR">
      <comp-filter name="VTODO">
        <prop-filter name="COMPLETED">
          <is-not-defined/>
        </prop-filter>
      </comp-filter>
    </comp-filter>
  </filter>
</calendar-query>"""

    client._request('REPORT', PRINCIPAL_DAV_PATH, body=all_vtodo, headers={'Content-Type': 'text/xml'})
    assert client.response.status == 207
    assert len(client.response.tree.getchildren()) is 0
    
    client._request('REPORT', PRINCIPAL_DAV_PATH, body=completed_vtodo, headers={'Content-Type': 'text/xml'})
    assert client.response.status == 207
    assert len(client.response.tree.getchildren()) is 0

def test_doubleslash():
    client.get(PRINCIPAL_DAV_PATH)
    assert client.response.status == 200
    client.get(PRINCIPAL_DAV_PATH.replace('/dav', '/dav/'))
    assert client.response.status == 404
    
# def test_needs_priviledge():
#     client.set_basic_auth('nobody', 'wrongpass')
#     client.propfind(PRINCIPAL_DAV_PATH)
#     assert False

