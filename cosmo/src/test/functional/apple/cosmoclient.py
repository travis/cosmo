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

import davclient
import copy
import xmlobjects
import pdb
import urllib

from xml.etree import ElementTree

def dict_to_elem(parent, dict_vals, namespace=None):
    for key, value in dict_vals.items():
        if namespace is None:
            element = ElementTree.SubElement(parent, key)
        else:
            element = ElementTree.SubElement(parent, '{'+namespace+'}'+key)
        element.text = value

class CosmoClient(davclient.DAVClient):
    """Class for adding cosmo specific functionality to DAVClient"""
    
    _cosmo_path = '/'
    _cmp_path = _cosmo_path+'cmp'
    _collections_store = {}
    
    def set_cmp_path(self, path):
        _cmp_path = path
        
    def set_cosmo_path(self, path):
        _cosmo_path = path
        
    def add_user(self, username, password, first_name, last_name, email, request_method=None):
        if request_method is None:
            request_method = self.put 
        
        root = ElementTree.Element('{http://osafoundation.org/cosmo/CMP}user')
        vals = {'username':username, 'password':password, 'firstName':first_name, 'lastName':last_name, 'email':email}
        dict_to_elem(root, vals, namespace='http://osafoundation.org/cosmo/CMP')
        request_method(self._cmp_path+'/user/%s'%username, body=unicode(ElementTree.tostring(root), 'utf-8'),
                       headers={'content-type': 'text/xml; charset=utf-8'})
        
    def modify_user(self, user_dict, request_method=None):
        if request_method is None:
            request_method = self.put
            
        root = ElementTree.Element('{http://osafoundation.org/cosmo/CMP}user')
        dict_to_elem(root, user_dict, namespace='http://osafoundation.org/cosmo/CMP')
        request_method(self._cmp_path+'/user/%s'%user_dict['username'], body=unicode(ElementTree.tostring(root), 'utf-8'), headers={'content-type': 'text/xml; charset=utf-8'})
        
    def mkcalendar(self, username=None):
        if username is None:
            username = self._username
            
        self._request()
        
    def remove_user(self, user):
        self.delete(self._cmp_path+'/user/%s' % user)
        
    def get_users(self, headers=None):
        
        self._request('GET', self._cmp_path+'/users', body=None, headers=headers)
        elements = self.response.tree.findall('.//{http://osafoundation.org/cosmo/CMP}username')
        self._users = []
        for element in elements:
            self._users.append(element.text)
        return self._users
    
    # def get_all_events(self, user, collection='/'):
    #     """Get all the events for a given user. Returns list of dict objects with 'href' and 'body'"""
    #     self.propfind(self._cosmo_path+'dav/'+urllib.quote(user)+collection)
    #     hrefs = [ response.find('{DAV:}href').text for response in self.response.tree.getchildren() if (
    #                   response.find('{DAV:}href').text.find('http') is not -1) and (
    #                   response.find('{DAV:}href').text.endswith('.ics') )]
    #     events = []
    #     for ref in hrefs:
    #         event = {'href':ref}
    #         print ref.replace(self._url.geturl(), '')
    #         self.get(ref.replace(self._url.geturl(), ''))
    #         event['body'] = copy.copy(self.response.body)
    #         events.append(event)
    #     return events
        
    def get_all_users_items(self):
        """Get all the events for all users on the server. Returns dict object where key is username and value is list of event dict object from CosmoClient.get_all_events"""
        if not hasattr(self, '_users'):
            self.get_users()
        
        all_events = {}
        for user in self._users:
            try:
                user.encode('ascii')
            except UnicodeEncodeError:
                print "username can't be coerced easily to ascii, skipping"
            else:
                print 'Getting all events for user '+user.encode('ascii', 'ignore')
                items = self.get_all_dav_resources_for_user(user)
                all_events[user] = items
            
        return all_events
        
    def get_all_dav_resources_for_user(self, user, collection='/'):
        all_items = []
        self.propfind(self._cosmo_path+'dav/'+urllib.quote(user)+collection)
        hrefs = [ response.find('{DAV:}href').text for response in self.response.tree.getchildren() if (
                      response.find('{DAV:}href').text.find('http') is not -1) and ( not
                      response.find('{DAV:}href').text.endswith('/') )]

        for ref in hrefs:
            item = {'href':ref}
            self.get(ref.replace(self._url.geturl(), ''))
            item['body'] = copy.copy(self.response.body)
            all_items.append(item)
        return all_items
        
    def get_user_count(self):
        return len(self.get_users())
        
    def mc_get_collection(self, uuid):
        """Get a collections xml as a string object by uuid"""
        path = self._cosmo_path+'mc/collection/'+str(uuid)
        return self.get(path, headers={'content-type':'application/eim+xml; charset=UTF-8'})
        
    def mc_publish_collection(self, xml_object, parent=None):
        """Publish a collection over morsecode from an xmlobject"""
        if parent is None:
            path = self._cosmo_path+'mc/collection/'+xml_object['uuid']
        else:
            path = self._cosmo_path+'mc/collection/'+xml_object['uuid']+'?parent='+parent
        
        self.put(path, unicode(xmlobjects.tostring(xml_object)), 
                 headers={'content-type':'application/eim+xml; charset=UTF-8'})
        if self.response.status == 201:
            self._collections_store[xml_object['uuid']] = {'xmlobject':xml_object, 'token':self.response.getheader('X-MorseCode-SyncToken')}
    
    # def mc_modify_collection(self, xml_object):
    #     """Modify collection over morsecode from an xmlobject"""
    #     path = self._cosmo_path+'mc/collection/'+xml_object['uuid']
    #     
    #     previous_xmlobject = xmlobjects.fromstring(self.mc_get_collection(xml_object['uuid']))
    #     token = self.response.getheader('X-MorseCode-SyncToken')
    #     
    #     if type(xml_object.recordset) is not xmlobjects.element_list:
    #         xml_object.recordset = []
    #     
    #     if type(previous_xmlobject.recordset) is not xmlobjects.element_list:
    #         previous_xmlobject.recordset = []
    #     
    #     def get_recordset_uuid(recordset):
    #         return getattr(getattr(recordset, '{http://osafoundation.org/eim/item/0}record'), 
    #                                           '{http://osafoundation.org/eim/item/0}uuid')
    #         
    #     modified_objects = []
    #     items_for_removal = []
    #     matched_recordsets = []
    #     
    #     for recordset in xml_object.recordset:
    #         if not recordset in previous_xmlobject.recordset:
    #             recordset_uuid = get_recordset_uuid(recordset)
    #             found = False
    #             for previous_recordset in previous_xmlobject.recordset:
    #                 previous_uuid = get_recordset_uuid(previous_recordset)
    #                 if recordset_uuid == previous_uuid:
    #                     found = True
    #                     for record in recorset.record:
    #                         for attribute in [getattr(record, attribute) for attribute in dir(record)]:
    #                             if attribute is not None and attribute != '' and attribute.has_key('empty'):
    #                                 attribute.__delitem__('empty')
    #                                 
    #                     modified_objects.append(recordset)
    #             if found is not True:
    #                 items_for_removal.append(recordset)
    #         else:
    #             matched_recordsets.append(recordset)
    #             
    #     
    #     for recordset in matched_recordsets:
    #         xmlobjects.recordset.remove(recordset)
    #     
    #     for recordset in items_for_removal:
    #         for attribute in [attribute for attribute in dir(recordset) if not attribute.startswith('_')]:
    #             delattr(recordset, attribute)
    #         recordset['{http://osafoundation.org/eim/0}deleted'] = True
    #     
    #     request_body = xmlobjects.tostring(xml_object)
    #     
    #     self.post(path, request_body, headers={'content-type':'application/eim+xml; charset=UTF-8',
    #                                            'X-MorseCode-SyncToken':token})

                