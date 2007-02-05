import cosmoclient
import random
import os, sys
from xml.etree import ElementTree

import cosmo_test_lib
from cosmo_test_lib import *

# COPY and MOVE operations
# DELETE Calendar Collection
# Put of todo and vfreebusy

CALENDAR = 'calendar'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'

def setup_module(module):
    cosmo_test_lib.setup_module(module)
    client.mkcol('%s/applesdataset' % module.PRINCIPAL_DAV_PATH)
    assert client.response.status == 201

def test_all_apple_dataset():
    """Test the whole dataset"""
    def recursive_upload(full_path, collection):
    # Recursively run through the directories and upload all the relevant files
        for filename in os.listdir(full_path):
            print full_path+'/'+filename
            if filename.startswith('.') is False and os.path.isdir(full_path+'/'+filename):
                client._request('MKCALENDAR', '%s/%s' % (collection, filename))
                assert client.response.status == 201
                recursive_upload(full_path+'/'+filename, collection+'/'+filename)
                    
            elif filename.endswith('.xml') is False and os.path.isdir(full_path+'/'+filename) is False:
                if filename.endswith('.txt'):
                    up_name = filename.replace('.txt', '.ics')
                else:
                    up_name = filename
                client.put('%s/%s' % (collection, up_name), f=open(full_path+'/'+filename))
                assert client.response.status == 201
    
    recursive_upload(FILES_DIR+'/ApplesResource', '%s/applesdataset' % PRINCIPAL_DAV_PATH)