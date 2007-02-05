
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
    client.propfind('%s/testcollection1/' % PRINCIPAL_DAV_PATH)
    client.propfind('%s/testcollection2/' % PRINCIPAL_DAV_PATH)
    
    ### More to write but cosmo is failing already at this point
    