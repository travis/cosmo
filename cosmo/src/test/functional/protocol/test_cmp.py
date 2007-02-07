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

import cosmo_test_lib

def setup_module(module):
    cosmo_test_lib.setup_module(module)
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
    client.add_user(TEST_USER+"-2", TEST_PASS, TEST_FIRST_NAME, TEST_LAST_NAME, TEST_USER+"-2@osafoundation.org", headers={'x-http-method-override':'PUT'}, request_method=client.post)
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
    
def teardown_module(module):
    client.remove_user('test_modify_user')
    assert client.response.status == 204
    client.remove_user(TEST_USER)
    assert client.response.status == 204  
    client.remove_user(TEST_USER+'-2')
    assert client.response.status == 204  
    
        
    