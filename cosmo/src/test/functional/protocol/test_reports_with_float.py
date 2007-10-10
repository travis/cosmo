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
TIMEZONE_CALENDAR = 'tzcalendar'

def setup_module(module):
    import cosmo_test_lib
    cosmo_test_lib.setup_module(module)
    for i in range(1, 4):
        ics_name = 'float'+str(i)+'.ics'
        body = open(module.FILES_DIR+'/reports/put/'+ics_name).read()
        client.put('%s/%s/%s' % (module.PRINCIPAL_DAV_PATH, module.CALENDAR, ics_name), body=body, headers={'content-type':'text/calendar'})
        assert client.response.status == 201

    body = open(FILES_DIR+'mkcalendar/validFullBody.xml').read()
    client._request('MKCALENDAR', '%s/%s' % (PRINCIPAL_DAV_PATH, TIMEZONE_CALENDAR), body=body)
    
    for i in range(1, 4):
        ics_name = 'float'+str(i)+'.ics'
        body = open(module.FILES_DIR+'/reports/put/'+ics_name).read()
        client.put('%s/%s/%s' % (module.PRINCIPAL_DAV_PATH, module.TIMEZONE_CALENDAR, ics_name), body=body, headers={'content-type':'text/calendar'})
        assert client.response.status == 201
    
    assert client.response.status == 201
    
def test_basic_query_1():
    body = open(FILES_DIR+'reports/basicquery/1.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207    
    
def test_timerangequery_vevents_in_timerange_1():
    body = open(FILES_DIR+'reports/timerangequery/1.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 
                'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']

    validate_response_tree(client.response.tree, ['5.ics', '6.ics', '7.ics'], positive=positive)

def test_timerangequery_vevents_in_timerange_2():
    body = open(FILES_DIR+'reports/timerangequery/2.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 
                'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']

    validate_response_tree(client.response.tree, ['1.ics', '2.ics', '3.ics', '4.ics'], positive=positive)

def test_timerangequery_vevents_in_timerange_3():
    body = open(FILES_DIR+'reports/timerangequery/3.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 
                'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']

    validate_response_tree(client.response.tree, ['4.ics'], positive=positive)

def test_timerangequery_vevents_in_timerange_4():
    body = open(FILES_DIR+'reports/timerangequery/4.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 
                'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']

    validate_response_tree(client.response.tree, ['3.ics'], positive=positive)

def test_timerangequery_vevents_in_timerange_5():
    body = open(FILES_DIR+'reports/timerangequery/5.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 
                'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event', 'END:VEVENT']

    validate_response_tree(client.response.tree, ['3.ics'], positive=positive)
    
def test_timerangequery_one_in_honolulu():
    body = open(FILES_DIR+'reports/timerangequery/oneInHonolulu.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, TIMEZONE_CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 
                'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                'VERSION:2.0', 'BEGIN:VEVENT', 'DTSTART:20060330T120000', 
                'DTEND:20060330T130000', 'SUMMARY:Floating Event One',
                'DESCRIPTION: This event should appear in Honolulu\, Mountain\, and Eastern', 
                'UID:54E181BC7CCC373042B21884211@ninevah.local', 'END:VEVENT',
                'END:VCALENDAR']

    negative = ['TZID', 'BEGIN:DAYLIGHT','RRULE:', 'TZNAME:',
                'TZOFFSETFROM:', 'TZOFFSETTO:']
                
    validate_response_tree(client.response.tree, ['float1.ics'], positive=positive, negative=negative)
    
def test_timerangequery_two_in_mountain():
    body = open(FILES_DIR+'reports/timerangequery/twoInMountain.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, TIMEZONE_CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 
                'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                'VERSION:2.0', 'BEGIN:VEVENT', 'END:VEVENT', 'END:VCALENDAR']

    validate_response_tree(client.response.tree, ['float1.ics','float2.ics'], positive=positive)    
    assert get_event_body_by_ics_name(client.response.tree, 'float1.ics').find('DTSTART:20060330T120000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float1.ics').find('DTEND:20060330T130000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float2.ics').find('DTSTART:20060330T150000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float2.ics').find('DTEND:20060330T160000') is not -1
    
    
def test_timerangequery_three_in_eastern():
    body = open(FILES_DIR+'reports/timerangequery/threeInEastern.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, TIMEZONE_CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207

    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 
                'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN',
                'VERSION:2.0', 'BEGIN:VEVENT', 'END:VEVENT', 'END:VCALENDAR']

    validate_response_tree(client.response.tree, ['float1.ics','float2.ics', 'float3.ics'], positive=positive)
    
    assert get_event_body_by_ics_name(client.response.tree, 'float1.ics').find('DTSTART:20060330T120000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float1.ics').find('DTEND:20060330T130000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float2.ics').find('DTSTART:20060330T150000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float2.ics').find('DTEND:20060330T160000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float3.ics').find('DTSTART:20060330T170000') is not -1
    assert get_event_body_by_ics_name(client.response.tree, 'float3.ics').find('DTEND:20060330T180000') is not -1

def test_timerangequery_invalid_not_utc_1():
    body = open(FILES_DIR+'reports/timerangequery/invalid_nonUTC1.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 403
    
def test_timerangequery_invalid_not_utc_2():
    body = open(FILES_DIR+'reports/timerangequery/invalid_nonUTC2.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 403

def test_timerangequery_invalid_not_utc_3():
    body = open(FILES_DIR+'reports/timerangequery/invalid_nonUTC3.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 400

def test_timerangequery_invalid_not_utc_4():
    body = open(FILES_DIR+'reports/timerangequery/invalid_nonUTC4.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 400

def test_timerangequery_invalid_not_utc_5():
    body = open(FILES_DIR+'reports/timerangequery/invalid_nonUTC5.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 400

def test_timerangequery_invalid_not_utc_6():
    body = open(FILES_DIR+'reports/timerangequery/invalid_nonUTC6.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 400

def test_timerangequery_01():
    body = open(FILES_DIR+'reports/timerangequery/timerange_01.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
def test_timerangequery_02():
    body = open(FILES_DIR+'reports/timerangequery/timerange_02.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
def test_timerangequery_03():
    body = open(FILES_DIR+'reports/timerangequery/timerange_03.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type':'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
    
    
    
