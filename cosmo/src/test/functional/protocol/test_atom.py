import cosmoclient
import random
import uuid
import os, sys
from xml.etree import ElementTree
import xmlobjects

def setup_module(module):
    client = cosmoclient.CosmoClient('http://qacosmo.osafoundation.org')
    new_user = '%s' % str(uuid.uuid1()).replace('-', '')
    client.set_basic_auth('root', 'cosmo')
    client.add_user(new_user, 'test_pass', 'Test', 'User', '%s@me.com' % new_user)
    assert client.response.status == 201
    client.set_basic_auth(new_user, 'test_pass')
    module.client = client
    module.new_user = new_user

def test_get_service_document():    
    client.get('/chandler/atom/user/%s' % new_user)
    assert client.response.status == 200
    xobj = xmlobjects.fromstring(client.response.body)
    assert len(xobj.workspace) is 2
    assert u'account', u'home' in [xobj.workspace[0].title, xobj.workspace[1].title]
    