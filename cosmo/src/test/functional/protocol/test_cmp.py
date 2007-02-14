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
import random, uuid

import cosmo_test_lib

SERVER_URL = 'http://qacosmo.osafoundation.org:80'
ADMIN_USER = 'root'
ADMIN_PASS = 'cosmo'
PRINCIPAL_ROOT = '/cosmo/dav'

TEST_USER_PREFIX = 'test_user_'

def setup_module(module):
    module.TEST_USER = uuid.uuid1().__str__().replace('-', '')
    module.TEST_PASS = 'test_pass'
    module.TEST_FIRST_NAME = 'Test'
    module.TEST_LAST_NAME = 'User'
    module.TEST_EMAIL = module.TEST_USER+'@osafoundation.org'
    module.PRINCIPAL_ROOT = PRINCIPAL_ROOT
    module.PRINCIPAL_DAV_PATH = '%s/%s' % (PRINCIPAL_ROOT, module.TEST_USER)
    module.TEST_USER_2 = uuid.uuid1().__str__().replace('-', '')
    client = cosmoclient.CosmoClient(module.SERVER_URL)
    client.set_basic_auth(module.ADMIN_USER, module.ADMIN_PASS)
    module.client = client

def test_list_users():
    client.get(client._cmp_path+'/users')
    assert client.response.status == 200
    assert client.response.body.find('root') is not -1
    
def test_view_user():
    client.get(client._cmp_path+'/user/root')
    assert client.response.status == 200
    assert client.response.body.find('root') is not -1

def test_create_user():
    client.add_user(TEST_USER, TEST_PASS, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_EMAIL)
    assert client.response.status == 201

    client.add_user(TEST_USER_2, TEST_PASS, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USER_2+"@osafoundation.org", headers={'x-http-method-override':'PUT'}, request_method=client.post)
    assert client.response.status == 201
    
def test_modify_user():
    client.add_user('test_modify_user', 'test_pass', 'Test', 'Name', 'me@fake.com')
    assert client.response.status == 201
    client.modify_user({'username':'test_modify_user', 'email':'new_email@fake.com'})
    assert client.response.status == 204
    client.get(client._cmp_path+'/user/test_modify_user')
    assert client.response.body.find('new_email@fake.com') is not -1
    client.modify_user({'username':'test_modify_user', 
                        'email':'new_new_email@fake.com'}, 
                        headers={'x-http-method-override':'PUT'}, request_method=client.post)
    assert client.response.status == 204
    client.get(client._cmp_path+'/user/test_modify_user')
    assert client.response.body.find('new_new_email@fake.com') is not -1
    
def test_delete_user():
    client.add_user('test_delete_user', 'test_pass', 'Test', 'Name', 'me@blah.com')
    assert client.response.status == 201
    client.remove_user('test_delete_user')
    assert client.response.status == 204

def test_administrator_tag():
    from xml.etree import ElementTree
    
    client.get(client._cmp_path+'/users')
    assert client.response.status == 200
    assert client.response.body.find('root') is not -1
    root_user_element = [x for x in client.response.tree.findall('{http://osafoundation.org/cosmo/CMP}user') if x.find('{http://osafoundation.org/cosmo/CMP}username').text == 'root'][0]
    assert root_user_element.find('{http://osafoundation.org/cosmo/CMP}administrator').text == 'true'
    test_user_element = [x for x in client.response.tree.findall('{http://osafoundation.org/cosmo/CMP}user') if x.find('{http://osafoundation.org/cosmo/CMP}username').text == TEST_USER][0]
    assert test_user_element.find('{http://osafoundation.org/cosmo/CMP}administrator').text == 'false'
    client.modify_user({'username':TEST_USER, 'administrator':'true'})
    assert client.response.status == 204
    client.get(client._cmp_path+'/users')
    assert client.response.status == 200
    test_user_element = [x for x in client.response.tree.findall('{http://osafoundation.org/cosmo/CMP}user') if x.find('{http://osafoundation.org/cosmo/CMP}username').text == TEST_USER][0]
    assert test_user_element.find('{http://osafoundation.org/cosmo/CMP}administrator').text == 'true'
    client.modify_user({'username':TEST_USER, 'administrator':'false'})
    assert client.response.status  == 204
    
    
def teardown_module(module):
    client.remove_user('test_modify_user')
    assert client.response.status == 204
    client.remove_user(TEST_USER)
    assert client.response.status == 204  
    client.remove_user(TEST_USER_2)
    assert client.response.status == 204  

        
    