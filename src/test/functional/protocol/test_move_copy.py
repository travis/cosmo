
import cosmoclient
import random
import os, sys
from xml.etree import ElementTree

from cosmo_test_lib import *

CALENDAR = 'calendar'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'

def test_move_text_data():
    client.mkcol('/cosmo/home/%s/testcollection1' % TEST_USER)
    assert client.response.status == 201
    client.mkcol('/cosmo/home/%s/testcollection2' % TEST_USER)
    assert client.response.status == 201
    client.put('/cosmo/home/%s/testcollection1/blah.txt' % TEST_USER, body='BALHALAHLAHLALH')
    assert client.response.status == 201
    client.move('/cosmo/home/%s/testcollection1/blah.txt' % TEST_USER, '/cosmo/%s/testcollection2/blah.txt' % TEST_USER)
    assert client.response.status == 201
    client.propfind('/cosmo/home/%s/testcollection1/' % TEST_USER)
    client.propfind('/cosmo/home/%s/testcollection2/' % TEST_USER)
    
    ### More to write but cosmo is failing already at this point
    