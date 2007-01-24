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


SERVER_URL = 'http://qacosmo.osafoundation.org:8080'
ADMIN_USER = 'root'
ADMIN_PASS = 'cosmo'

TEST_USER = 'test_user_report%s' % str(random.random()).replace('.', '')
TEST_PASS = 'test_pass'
TEST_FIRST_NAME = 'Test'
TEST_LAST_NAME = 'User'
TEST_EMAIL = TEST_USER+'@osafoundation.org'
CALENDAR = 'calendar'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'
    

def test_mkcalendar_invalid_badxml():
    body = open(FILES_DIR+'mkcalendar/invalidBadXML.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 400

def test_mkcalendar_invalid_caldav_prop_missing():
    body = open(FILES_DIR+'mkcalendar/invalidCaldavProperty.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 400
    
def test_mkcalendar_invalid_dav_prop():
    body = open(FILES_DIR+'mkcalendar/invalidDavProperty.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s/' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 400

def test_mkcalendar_invalid_supported_cal_component():
    body = open(FILES_DIR+'mkcalendar/invalidSupportedCalendarComponent.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 400

def test_mkcalendar_invalid_supported_cal_data():
    body = open(FILES_DIR+'mkcalendar/invalidSupportedCalendarData.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 400

def test_mkcalendar_invalid_timezone():
    body = open(FILES_DIR+'mkcalendar/invalidTimezone.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 207
    for propstat_element in client.response.tree.find('{DAV:}response').findall('{DAV:}propstat'):
        if propstat_element.find('{DAV:}prop').find('{DAV:}displayname') is not None:
            assert propstat_element.find('{DAV:}status').text == 'HTTP/1.1 200 OK'
        elif propstat_element.find('{DAV:}prop').find('{urn:ietf:params:xml:ns:caldav}calendar-timezone') is not None:
            assert propstat_element.find('{DAV:}status').text == 'HTTP/1.1 409 Conflict'
        else:
            assert False, 'extra propstat element'

def test_mkcalendar_valid_no_body():
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=None)
    assert client.response.status == 201

def test_mkcalendar_valid_description_no_lang():
    body = open(FILES_DIR+'mkcalendar/validDescriptionNoLang.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 201

def test_mkcalendar_valid_full_body():
    body = open(FILES_DIR+'mkcalendar/validFullBody.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 201

def test_mkcalendar_valid_no_description():
    body = open(FILES_DIR+'mkcalendar/validNoDescription.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 201
    
def test_mkcalendar_valid_no_displayname():
    body = open(FILES_DIR+'mkcalendar/validNoDisplayname.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 201
    
def test_mkcalendar_valid_no_supported_component():
    body = open(FILES_DIR+'mkcalendar/validNoSupportedCalendarComponent.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 201

def test_mkcalendar_valid_no_timezone():
    body = open(FILES_DIR+'mkcalendar/validNoTimezone.xml').read()
    client._request('MKCALENDAR', '/cosmo/home/%s/%s' % (TEST_USER, str(random.random()).replace('.', '')), body=body)
    assert client.response.status == 201