# #   Copyright (c) 2006-2007 Open Source Applications Foundation
# #
# #   Licensed under the Apache License, Version 2.0 (the "License");
# #   you may not use this file except in compliance with the License.
# #   You may obtain a copy of the License at
# #
# #       http://www.apache.org/licenses/LICENSE-2.0
# #
# #   Unless required by applicable law or agreed to in writing, software
# #   distributed under the License is distributed on an "AS IS" BASIS,
# #   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# #   See the License for the specific language governing permissions and
# #   limitations under the License.
# 
# import cosmoclient
# import random
# import os, sys
# from xml.etree import ElementTree
# 
# import cosmo_test_lib
# from cosmo_test_lib import *
# 
# # COPY and MOVE operations
# # DELETE Calendar Collection
# # Put of todo and vfreebusy
# 
# 
# # STATUS returns 500 error
# # ATTACHMENT returns 400, 'Bad Request: Invalid calendar object: An error ocurred during parsing - line: #'
# # VAVAILABILITY returns 400, 'Bad Request: Invalid calendar object: An error ocurred during parsing - line: #'
# # putting an event with the same uid in two collections causes a 204
# # error parsing TZOFFSETTO:0100
# 
# CALENDAR = 'calendar'
# FILES_DIR =  os.path.dirname(os.path.abspath(sys.modules[__name__].__file__))+'/files/'
# 
# def setup_module(module):
#     cosmo_test_lib.setup_module(module)
#     client.mkcol('%s/applesdataset' % module.PRINCIPAL_DAV_PATH)
#     assert client.response.status == 201
# 
# def test_all_apple_dataset():
#     """Test the whole dataset"""
#     
#     uids = []
#     client._request('MKCALENDAR', '%s/%s/%s' % (PRINCIPAL_DAV_PATH, 'applesdataset', 'defaulted'))
#     assert client.response.status == 201
#     
#     def recursive_upload(full_path, collection):
#     # Recursively run through the directories and upload all the relevant files
#         for filename in os.listdir(full_path):
#             print full_path+'/'+filename
#             if filename.startswith('.') is False and os.path.isdir(full_path+'/'+filename):
#                 if filename == 'put' or filename == 'freebusy':
#                     client._request('MKCALENDAR', '%s/%s' % (collection, str(random.random()).replace('.', '')))
#                 else:    
#                     client._request('MKCALENDAR', '%s/%s' % (collection, filename))
#                 assert client.response.status == 201
#                 recursive_upload(full_path+'/'+filename, collection)
#                     
#             elif filename.startswith('.') is False and filename.endswith('.xml') is False and os.path.isdir(full_path+'/'+filename) is False and open(full_path+'/'+filename).read().find('<?xml') is -1:
#                 up_name = str(random.random()).replace('.', '')+'.ics'
# 
#                     
#                 #find uid, work around for bug in cosmo
#                 for line in open(full_path+'/'+filename).read().splitlines():
#                     if line.find('UID:') is not -1:
#                         uid = line.split('UID:')[-1]
#                         # open(full_path+'/'+filename).read().find('STATUS') is -1 and
#                 if open(full_path+'/'+filename).read().find('VAVAILABILITY') is -1 and open(full_path+'/'+filename).read().find('ATTACHMENT') is -1 and uid not in set(uids) and open(full_path+'/'+filename).read().find('TZOFFSETTO:0100') is -1:
#                     if collection.endswith('applesdataset'):
#                         collection = collection.replace('applesdataset', 'applesdataset/defaulted')
#                     body = open(full_path+'/'+filename).read().replace('$', '')
#                     client.put('%s/%s' % (collection, up_name), body=body)
#                     assert client.response.status == 201 or client.response.status == 204
#                     for line in open(full_path+'/'+filename).read().splitlines():
#                         if line.find('UID:') is not -1:
#                             uids.append(line.split('UID:')[-1])
#                         
#                     
#     
#     recursive_upload(FILES_DIR+'/ApplesResource', '%s/applesdataset' % PRINCIPAL_DAV_PATH)