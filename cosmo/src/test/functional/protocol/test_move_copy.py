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

def test_move_text_data():
    client.mkcol('%s/testcollection1' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.mkcol('%s/testcollection2' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.put('%s/testcollection1/blah.txt' % PRINCIPAL_DAV_PATH, body='BALHALAHLAHLALH')
    assert client.response.status == 201
    client.move('%s/testcollection1/blah.txt' % PRINCIPAL_DAV_PATH, '%s/testcollection2/blah.txt' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    assert len(client.propfind('%s/testcollection1/' % PRINCIPAL_DAV_PATH)) is 1
    assert len(client.propfind('%s/testcollection2/' % PRINCIPAL_DAV_PATH)) is 2
    
    ### More to write but cosmo is failing already at this point
def test_copy_text_data():
    client.mkcol('%s/testcollection3' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.mkcol('%s/testcollection4' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    client.put('%s/testcollection3/blah.txt' % PRINCIPAL_DAV_PATH, body='BALHALAHLAHLALH')
    assert client.response.status == 201
    client.copy('%s/testcollection3/blah.txt' % PRINCIPAL_DAV_PATH, '%s/testcollection4/blah.txt' % PRINCIPAL_DAV_PATH)
    assert client.response.status == 201
    assert len(client.propfind('%s/testcollection3/' % PRINCIPAL_DAV_PATH)) is 2
    assert len(client.propfind('%s/testcollection4/' % PRINCIPAL_DAV_PATH)) is 2
    
    
    
    