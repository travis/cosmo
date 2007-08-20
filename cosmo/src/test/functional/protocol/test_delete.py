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

def test_delete_empty_collection():
    client.mkcol('%s/collectiontodelete' % PRINCIPAL_DAV_PATH)
    test = PRINCIPAL_DAV_PATH
    assert client.response.status == 201
    client.delete('%s/collectiontodelete' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 204
    
def test_delete_collection_with_content():
    client.mkcol('%s/collectiontodeletewithdata' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.put('%s/collectiontodeletewithdata/test.txt' % PRINCIPAL_DAV_PATH, body='asdfasdfasdf')
    assert client.response.status == 201
    client.delete('%s/collectiontodeletewithdata' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 204
    
def test_delete_text_file():
    client.mkcol('%s/collectiontodeletetext' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.put('%s/collectiontodeletetext/test.txt' % PRINCIPAL_DAV_PATH, body='asdfasdfasdf')
    assert client.response.status == 201
    client.delete('%s/collectiontodeletetext/test.txt' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 204
    
def test_delete_empty_calendar():
    client._request('MKCALENDAR', '%s/calendartodelete' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.delete('%s/calendartodelete' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 204
    
def test_delete_populated_calendar():
    client.delete('%s/%s' % (PRINCIPAL_DAV_PATH, CALENDAR))
    assert client.response.status == 204