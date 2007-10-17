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
                    headers={  'Content-Type': 'text/xml', 'Depth': '1' })
    assert client.response.body.find('DTSTART:20071017T000000Z')
    
    

