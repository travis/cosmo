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
import functest
import random, uuid, os, sys
from uuid import uuid1

SERVER_URL = functest.registry.get('url', 'http://qacosmo.osafoundation.org')
ADMIN_USER = functest.registry.get('admin_user', 'root')
ADMIN_PASS = functest.registry.get('admin_pass', 'cosmo')
PATH = functest.registry.get('path', '/')
if not PATH.endswith('/'):
    PATH += '/'
PRINCIPAL_ROOT = PATH+'dav'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'

TEST_USER_PREFIX = 'test_user_'

def setup_module(module, server_url=SERVER_URL, admin_user=ADMIN_USER, admin_pass=ADMIN_PASS, user_prefix=TEST_USER_PREFIX):
    # Set module vars
    module.SERVER_URL = server_url
    module.ADMIN_USER = admin_user
    module.ADMIN_PASS = admin_pass
    module.TEST_USER = str(uuid.uuid1()).replace('-', '')
    module.TEST_PASS = 'test_pass'
    module.TEST_FIRST_NAME = 'Test'
    module.TEST_LAST_NAME = 'User'
    module.TEST_EMAIL = module.TEST_USER+'@osafoundation.org'
    module.PRINCIPAL_ROOT = PRINCIPAL_ROOT
    module.PRINCIPAL_DAV_PATH = '%s/%s' % (PRINCIPAL_ROOT, module.TEST_USER)
    module.PRINCIPAL_MS_PATH = PRINCIPAL_ROOT.replace('/dav', '/mc/collection')
    module.FILES_DIR = FILES_DIR
    
    #Setup client and users
    path = PATH
    cosmoclient.CosmoClient._cosmo_path = path
    cosmoclient.CosmoClient._cmp_path = path+'cmp'
    
    client = cosmoclient.CosmoClient(module.SERVER_URL)
    client.set_basic_auth(module.ADMIN_USER, module.ADMIN_PASS)
    module.client = client
    client.add_user(module.TEST_USER, module.TEST_PASS, module.TEST_FIRST_NAME, module.TEST_LAST_NAME, module.TEST_EMAIL)
    client.set_basic_auth(module.TEST_USER, module.TEST_PASS)
    
    if hasattr(module, 'CALENDAR'):
        client._request('MKCALENDAR', '%s/%s' % (module.PRINCIPAL_DAV_PATH, module.CALENDAR))
        assert client.response.status == 201
        for i in range(1, 8):
            ics_name = str(i)+'.ics'
            body = open(module.FILES_DIR+'/reports/put/'+ics_name).read()
            client.put('%s/%s/%s' % (module.PRINCIPAL_DAV_PATH, module.CALENDAR, ics_name), body=body, headers={'content-type':'text/calendar'})
            assert client.response.status == 201
        
def teardown_module(module):
    module.client.set_basic_auth(module.ADMIN_USER, module.ADMIN_PASS)
    module.client.remove_user(module.TEST_USER)
    assert module.client.response.status == 204

def validate_response_tree(tree, ics_list, positive=[], negative=[], verify_etags=True):
    """Validate that response tree contains a response for each ics in list, and that each ics contains the positive strings and does not contain any of the negative strings"""
    # Get all response elements
    response_elements = tree.findall('{DAV:}response')
    # Get als href elements
    href_elements = []
    for elem in response_elements:
        href = elem.find('{DAV:}href')
        assert href is not None
        href_elements.append(href)
    # Get all of the caldata elements
    caldata_elements = []
    for element in response_elements:
        # Verify etags
        assert element.find('{DAV:}propstat').find('{DAV:}prop').find('{DAV:}getetag') is not None
        caldata = element.find('{DAV:}propstat').find('{DAV:}prop').find('{urn:ietf:params:xml:ns:caldav}calendar-data')
        assert caldata is not None
        caldata_elements.append(caldata)
    # Verify that all the lengths match
    assert len(ics_list) is len(response_elements) is len(href_elements) is len(caldata_elements)
    
    # Verify that every href element matches an ics in the list
    for element in href_elements:
        assert element.text.split('/')[-1] in set(ics_list)

    for element in caldata_elements:
        # Verify that every caldata response contains all the text matches
        for item in positive:
            assert element.text.find(item) is not -1, item
        # Verify that every caldata response does not contain and of the negative text matches
        for item in negative:
            assert element.text.find(item) is -1, item
    

def validate_ics_bodies(tree, find_dict):
    """Given a dict of ics files and their search criteria will verify each ics body"""
    for response_element in tree.findall('{DAV:}response'):
        result_check = find_dict[response_element.find('{DAV:}href').text.split('/')[-1]]
        if result_check[0] is -1:
            assert response_element.find('{DAV:}propstat').find('{DAV:}prop').find('{urn:ietf:params:xml:ns:caldav}calendar-data').text.find(result_check[1]) is -1
        else:
            assert response_element.find('{DAV:}propstat').find('{DAV:}prop').find('{urn:ietf:params:xml:ns:caldav}calendar-data').text.find(result_check[1]) is not -1
            
            
def get_event_body_by_ics_name(tree, ics_name):
    for response in tree.findall('{DAV:}response'):
        if response.find('{DAV:}href').text.split('/')[-1] == ics_name:
            for propstat in response.findall('{DAV:}propstat'):
                for prop in propstat.findall('{DAV:}prop'):
                    if prop.find('{urn:ietf:params:xml:ns:caldav}calendar-data') is not None:
                        return prop.find('{urn:ietf:params:xml:ns:caldav}calendar-data').text
    
    
    
    
    
    
    
    
    
    
    
    
    
        
