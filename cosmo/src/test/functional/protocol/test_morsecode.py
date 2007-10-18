import cosmoclient
import random
import uuid
import os, sys
from xml.etree import ElementTree
import xmlobjects

from cosmo_test_lib import *

CALENDAR = 'calendar'
FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'

eimml_example = open(FILES_DIR+'eimml_example.xml', 'r').read()

collections = {}

def test_publish():
    
    eimml = xmlobjects.fromstring(eimml_example)
    
    collection_uuid = str(uuid.uuid1())
    record_uuid = str(uuid.uuid1())
    
    eimml['uuid'] = collection_uuid
    for record in eimml.recordset.record:
        record.uuid = record_uuid
        if hasattr(record, 'icalUid'):
            record.icalUid = collection_uuid
    
    client.mc_publish_collection(eimml)
    assert client.response.status == 201
    collections[eimml['uuid']] = {'xobj':eimml, 'token':client.response.getheader('X-MorseCode-SyncToken')}
    collections['example'] = collections[eimml['uuid']]
    
def test_update():
    
    eimml = collections['example']['xobj']
    
    getattr(eimml.recordset, '{http://osafoundation.org/eim/note/0}record').body = 'asdf'
    path = client._cosmo_path+'mc/collection/'+eimml['uuid']
    request_body = xmlobjects.tostring(eimml)
    client.post(path, request_body, headers={'content-type':'application/eim+xml; charset=UTF-8',
                                             'X-MorseCode-SyncToken':collections['example']['token']}
                )
    assert client.response.status == 204
    
def test_get_all_collections():
    
    eimml = xmlobjects.fromstring(eimml_example)

    collection_uuid = str(uuid.uuid1())
    record_uuid = str(uuid.uuid1())

    eimml['uuid'] = collection_uuid
    for record in eimml.recordset.record:
       record.uuid = record_uuid
       if hasattr(record, 'icalUid'):
           record.icalUid = collection_uuid

    client.mc_publish_collection(eimml)
    assert client.response.status == 201
    collections[eimml['uuid']] = {'xobj':eimml, 'token':client.response.getheader('X-MorseCode-SyncToken')}
    
    path = client._cosmo_path+'mc/user/%s' % client._username
    client.get(path, headers={'content-type':'application/eim+xml; charset=UTF-8'})
    assert client.response.status == 200
    
    xobj = xmlobjects.fromstring(client.response.body)
    assert len(xobj.collection) is 4 # cosmolib creates one calendar collection so we should have 3

    
    
    
    
    
    
    
    
    
    
    
    