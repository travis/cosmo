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
        
    
def test_basic_query_1():
    body = open(FILES_DIR+'reports/basicquery/1.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    positive = [ 'BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 
                  'VERSION:2.0','BEGIN:VTIMEZONE','TZID', 
                  'BEGIN:DAYLIGHT', 'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 
                  'TZOFFSETTO:', 'END:', 'BEGIN:STANDARD', 'END:STANDARD',
                  'END:VTIMEZONE', 'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event', 
                  'END:VEVENT']

    ics_list = ['1.ics', '2.ics', '3.ics', '4.ics', '5.ics', '6.ics', '7.ics']
    validate_response_tree(client.response.tree, ics_list, positive=positive)
    
def test_basic_query_2():
    body = open(FILES_DIR+'reports/basicquery/2.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    positive = [ 'BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                 'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT', 
                 'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 
                 'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE',
                 'END:VCALENDAR', 'BEGIN:VEVENT', 'SUMMARY:event 1', 'END:VEVENT']
    
    ics_list = ['1.ics']
    validate_response_tree(client.response.tree, ics_list, positive=positive)
    
def test_basic_query_3():
    body = open(FILES_DIR+'reports/basicquery/3.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    positive = [ 'BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                 'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT', 
                 'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 'END:',
                 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'END:VCALENDAR',
                 'BEGIN:VEVENT', 'SUMMARY', 'END:VEVENT', 'DESCRIPTION:']
    ics_list = ['2.ics', '6.ics', '7.ics']
    #validate_response_tree(client.response.tree, ics_list, positive=positive)
    
def test_basic_query_4():
    body = open(FILES_DIR+'reports/basicquery/4.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={  'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    ics_list = ['3.ics']
    validate_response_tree(client.response.tree, ics_list)
    
def test_basic_query_5():
    body = open(FILES_DIR+'reports/basicquery/5.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={  'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    ics_list = ['3.ics']
    validate_response_tree(client.response.tree, ics_list)
    
def test_basic_query_6():
    body = open(FILES_DIR+'reports/basicquery/6.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={  'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    ics_list = ['2.ics']
    validate_response_tree(client.response.tree, ics_list)
    
def test_freebusy():
    body = open(FILES_DIR+'reports/freebusy/1.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={  'Content-Type': 'text/xml', 'Depth': '1' })
    positive = ['FREEBUSY:', '20060101T150000Z/20060101T160000Z',   
                #'20060101T180000Z/20060101T190000Z', 
                '20060101T210000Z/20060101T220000Z', 
                '20060101T230000Z/20060102T000000Z', '20060102T150000Z/20060102T160000Z',
                '20060102T190000Z/20060102T200000Z', #'20060102T230000Z/20060103T000000Z', 
                '20060103T150000Z/20060103T160000Z', #'20060103T190000Z/20060103T200000Z',   
                '20060103T230000Z/20060104T000000Z', #'20060104T150000Z/20060104T160000Z',
                '20060104T210000Z/20060104T220000Z', #'20060105T010000Z/20060105T000000Z'
                ]
    
    for entry in positive:
        assert client.response.body.find(entry) is not -1

def test_invalid_nosubcomp():
    body = open(FILES_DIR+'reports/invalid/noSubComp.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 403
    
def test_limitexpand_1():
    body = open(FILES_DIR+'reports/limitexpand/1.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:', 'VERSION:2.0',
                'BEGIN:VTIMEZONE','TZID:', 'BEGIN:DAYLIGHT', 
                'DTSTART:', 'RRULE:', 'TZOFFSETFROM', 'TZOFFSETTO', 
                'END:DAYLIGHT', 'BEGIN:STANDARD', 'DTSTART:', 'RRULE:',
                'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 'END:STANDARD', 
                'END:VTIMEZONE', 'BEGIN:VEVENT', 'DTSTAMP:', 'DTSTART;', 'DURATION:',               
                'RRULE:', 'SUMMARY:', 'UID:', 'END:VEVENT', 'END:VCALENDAR']
    
    ics_list = ['5.ics', '6.ics', '7.ics']
    validate_response_tree(client.response.tree, ics_list, positive=positive)
    
def test_limitexpand_2():
    body = open(FILES_DIR+'reports/limitexpand/2.xml').read()
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:', 'VERSION:2.0',      
                'BEGIN:VTIMEZONE', 'TZID:', 'BEGIN:DAYLIGHT', 
                'DTSTART:', 'RRULE:', 'TZOFFSETFROM', 'TZOFFSETTO', 
                'END:DAYLIGHT', 'BEGIN:STANDARD', 'DTSTART:', 'RRULE:',
                'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:', 'END:STANDARD', 
                'END:VTIMEZONE', 'BEGIN:VEVENT', 'DTSTAMP:', 'DTSTART;', 'DURATION:', 
                'RRULE:', 'SUMMARY:', 'UID:', 'END:VEVENT', 'END:VCALENDAR']
    
    ics_list = ['5.ics', '6.ics', '7.ics']
    validate_response_tree(client.response.tree, ics_list, positive=positive)
    find_dict = {'5.ics':(-1, 'RECURRENCE-ID;'), '6.ics':(0, 'RECURRENCE-ID;TZID=US/Eastern:20060104T140000'), '7.ics':(0, 'RECURRENCE-ID;RANGE=THISANDFUTURE;TZID=US/Eastern:20060104T180000')}
    validate_ics_bodies(client.response.tree, find_dict)
    
def test_multiget_basic_vevent_summary():
    body = open(FILES_DIR+'reports/multiget/1.xml').read()
    body = body.replace('/cosmo/home/USER/CALENDAR', PRINCIPAL_DAV_PATH+'/'+CALENDAR)
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    validate_response_tree(client.response.tree, ['1.ics', '2.ics', '3.ics', '4.ics'], positive=['UID'])
    
    find_dict = {'1.ics':(0, 'UID:54E181BC7CCC373042B28842@ninevah.local'), 
                 '2.ics':(0, 'UID:9A6519F71822CD45840C3440@ninevah.local'), 
                 '3.ics':(0, 'UID:DB3F97EF10A051730E2F752E@ninevah.local'), 
                 '4.ics':(0, 'UID:A3217B429B4D2FF2DC2EEE66@ninevah.local')}
    validate_ics_bodies(client.response.tree, find_dict)
    
def test_multiget_four_resources_etag_vcal_only():
    body = open(FILES_DIR+'reports/multiget/2.xml').read()
    body = body.replace('/cosmo/home/USER/CALENDAR', PRINCIPAL_DAV_PATH+'/'+CALENDAR)
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID:-//Cyrusoft International\, Inc.//Mulberry v4.0//EN', 'VERSION:2.0', 'END:VCALENDAR']
    
    validate_response_tree(client.response.tree, ['1.ics', '2.ics', '3.ics', '4.ics'], positive=positive)
    
def test_multiget_four_resources_etag_vtimezone_only():
    body = open(FILES_DIR+'reports/multiget/3.xml').read()
    body = body.replace('/cosmo/home/USER/CALENDAR', PRINCIPAL_DAV_PATH+'/'+CALENDAR)
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                 'BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                 'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                 'END:', 'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 
                 'END:VCALENDAR']
                 
    negative = ['BEGIN:VEVENT', 'SUMMARY', 'END:VEVENT']
    validate_response_tree(client.response.tree, ['1.ics', '2.ics', '3.ics', '4.ics'], positive=positive, negative=negative)
    
def test_multiget_four_resources_etag_summary_uid_vevent_valarm_only():
    body = open(FILES_DIR+'reports/multiget/4.xml').read()
    body = body.replace('/cosmo/home/USER/CALENDAR', PRINCIPAL_DAV_PATH+'/'+CALENDAR)
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VEVENT', 'SUMMARY:', 'UID', 'END:VEVENT', 'END:VCALENDAR']
                  
    negative = ['BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE']
    
    validate_response_tree(client.response.tree, ['1.ics', '2.ics', '3.ics', '4.ics'], positive=positive, negative=negative)
    
    find_dict = {'1.ics':(0, 'SUMMARY:event 1'), 
                 '2.ics':(0, 'SUMMARY:event 2'), 
                 '3.ics':(0, 'SUMMARY:event 3'), 
                 '4.ics':(0, 'SUMMARY:event 4')}
    validate_ics_bodies(client.response.tree, find_dict)

def test_multiget_no_summary():
    body = open(FILES_DIR+'reports/multiget/5.xml').read()
    body = body.replace('/cosmo/home/USER/CALENDAR', PRINCIPAL_DAV_PATH+'/'+CALENDAR)
    client._request('REPORT', '%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR), body=body, headers={ 'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.status == 207
    
    positive = ['BEGIN:VCALENDAR', 'CALSCALE:GREGORIAN', 'PRODID', 'VERSION:2.0',
                'BEGIN:VEVENT', 'SUMMARY:', 'UID', 'END:VEVENT', 'END:VCALENDAR']
    
    negative = ['BEGIN:VTIMEZONE', 'TZID', 'BEGIN:DAYLIGHT',
                'DTSTART:', 'RRULE:', 'TZNAME:', 'TZOFFSETFROM:', 'TZOFFSETTO:',
                'BEGIN:STANDARD', 'END:STANDARD', 'END:VTIMEZONE', 'SUMMARY:event']

    validate_response_tree(client.response.tree, ['1.ics', '2.ics', '3.ics', '4.ics'], positive=positive, negative=negative)
    
    find_dict = {'1.ics':(-1, 'SUMMARY:event 1'), 
                 '2.ics':(-1, 'SUMMARY:event 2'), 
                 '3.ics':(-1, 'SUMMARY:event 3'), 
                 '4.ics':(-1, 'SUMMARY:event 4')}
    validate_ics_bodies(client.response.tree, find_dict)


    

    
                
          
    
    
    
    
    
    
    
    
    
    
    
