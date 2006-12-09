#   Copyright (c) 2006 Open Source Applications Foundation
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

import davclient, copy

class CosmoClient(davclient.DAVClient):
    """Class for adding cosmo specific functionality to DAVClient"""
    
    _cosmo_path = 'cosmo/'
    _cmp_path = _cosmo_path+'cmp'
    
    def set_cmp_path(self, path):
        _cmp_path = path
        
    def set_cosmo_path(self, path):
        _cosmo_path = path
        
    def get_users(self, headers=None):
        
        self._request('GET', self._cmp_path+'/users', body=None, headers=headers)
        elements = self.response.tree.findall('.//{http://osafoundation.org/cosmo/CMP}username')
        self._users = []
        for element in elements:
            self._users.append(element.text)
        return self._users
    
    def get_all_events(self, user, collection='/'):
        """Get all the events for a given user. Returns list of dict objects with 'href' and 'body'"""
        self.propfind(self._cosmo_path+'home/'+user+collection)
        hrefs = self.response.tree.findall('//{DAV:}href')
        events = []
        for ref in hrefs:
            if ref.text.endswith('.ics'):
                event = {'href':ref.text}
                self.get(ref.text.strip(self._url.geturl()))
                event['body'] = copy.copy(self.response.body)
                events.append(event)
        return events
        
    def get_all_users_events(self):
        """Get all the events for all users on the server. Returns dict object where key is username and value is list of event dict object from CosmoClient.get_all_events"""
        if not hasattr(self, '_users'):
            self.get_users()
        
        all_events = {}
        for user in self._users:
            print 'Getting all events for user "%s"' % user
            events = self.get_all_events(user)
            all_events[user] = events
            
        return all_events
        
                
                
                