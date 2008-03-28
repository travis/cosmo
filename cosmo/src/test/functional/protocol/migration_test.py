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

import cosmoclient
import cPickle as pickle
import sys, os
import inspect
import difflib

class MigrationTest(object):
    def __init__(self, connection_info, store):
        self.connection_info = connection_info
        self.store = store
        self.end_strings = []

class AllUserEvents(MigrationTest):
    """Verify that all events for all users match"""
    name = 'all_items'
    switch = 'e'
    def setup(self):
        self.client = cosmoclient.CosmoClient(self.connection_info['server_url'])
        self.client.set_basic_auth(self.connection_info['admin_username'], 
                                   self.connection_info['admin_password'])
    
    def collect(self):
        print 'Collecting All User events'
        self.store['all_items'] = self.client.get_all_users_items()
    
    def validate(self):
        total = passed = failed = 0
        failures = {}
        
        print 'Starting validation for All User Events'
        for user, items in self.store['all_items'].items():
            for item in items:
                print 'Verifying %s' % item['href']
                body = self.client.get(item['href'].replace(self.client._url.geturl(), ''))
                try:
                    total = total + 1
                    assert item['body'] == body
                    passed = passed + 1
                except AssertionError:
                    failed = failed + 1
                    diff = '\n'.join([line for line in difflib.unified_diff(item['body'].split('\n'), body.split('\n'))])
                    failure = {'item':item, 'body':body, 'diff':diff}
                    failures[item['href'].replace(self.client._url.geturl(), '')] = failure
                    print 'Failure in '+item['href']
                    print diff
        self.store['all_item_failures'] = failures
        for key, value in failures.items():
            self.end_strings.append(key+' Failed')
            self.end_strings.append(value['diff'])
        self.end_strings.append("Total resources = %s, Passed = %s, Failed = %s" % (total, passed, failed))
        if failed is not 0:
            self.end_strings.append('Failed urls :: \n%s' % '\n'.join(failures.keys()))
        
        
class NumberOfUsers(MigrationTest):
    """Test the number of users"""
    name = 'user_len'
    switch = 'n'
    def setup(self):
        self.client = cosmoclient.CosmoClient(self.connection_info['server_url'])
        self.client.set_basic_auth(self.connection_info['admin_username'], 
                                   self.connection_info['admin_password'])                           
                                   
    def collect(self):
        print 'Getting number of users'
        self.store['number_of_users'] = self.client.get_user_count()
    def validate(self):
        current_count = self.client.get_user_count()
        if self.store['number_of_users'] == current_count:
            self.end_strings.append('Number of users match on both instances')
        else:
            self.end_strings.append("Number of users don't match. %s is the previous number. %s is the new one" % (
                  self.store['number_of_users'], current_count )) 

class TestAccountResouces(MigrationTest):
    name = 'test_account_resources'
    switch = 'z'
    def setup(self):
        self.client = cosmoclient.CosmoClient(self.connection_info['server_url'])
        self.client.set_basic_auth('hub-test', self.connection_info['hub_pass'])
    
    def collect(self):
        print 'Getting all resources for hub-test user'
        self.store['hub_test_resources'] = self.client.get_all_dav_resources_for_user('hub-test')
    def validate(self):
        total = passed = failed = 0
        failures = {}
        
        print 'Starting validation for all resources for hub-test user'
        for item in self.store['hub_test_resources']:
            print 'Verifying %s' % item['href']
            body = self.client.get(item['href'].replace(self.client._url.geturl(), ''))
            try:
                total = total + 1
                assert item['body'] == body
                passed = passed + 1
            except AssertionError:
                failed = failed + 1
                diff = '\n'.join([line for line in difflib.unified_diff(item['body'].split('\n'), body.split('\n'))])
                failure = {'item':item, 'body':body, 'diff':diff}
                failures[item['href'].replace(self.client._url.geturl(), '')] = failure
                print 'Failure in '+item['href']
                print diff
        self.store['test_account_failures'] = failures
        for key, value in failures.items():
            self.end_strings.append(key+' Failed')
            self.end_strings.append(value['diff'])
        self.end_strings.append("Total resources = %s, Passed = %s, Failed = %s" % (total, passed, failed))
        if failed is not 0:
            self.end_strings.append('Failed urls :: \n%s' % '\n'.join(failures.keys()))
            
def main():
    from optparse import OptionParser
    parser = OptionParser()
    parser.add_option("-l", "--config", dest="config_file", default=False,
                     help="The config file for any options you wish to set")
    parser.add_option("-s", "--server_url", dest="server_url",
                     help='Full http or https url with port')
    parser.add_option("-u", "--admin_username", dest="admin_username",
                     help='Cosmo admin username')
    parser.add_option("-p", "--admin_password", dest="admin_password",
                     help='Cosmo admin password')
    parser.add_option("-x", "--hub_pass", dest="hub_pass",
                     help="Cosmo migration user pass")
    parser.add_option("-f", "--file", dest="filename",
                     help="Filename for all_event list pickle", metavar="FILE")
    parser.add_option("-c", "--collect",
                     action="store_true", dest="collect", default=False,
                     help="Get initial dataset and pickle it")
    parser.add_option("-v", "--validate",
                     action="store_true", dest="validate", default=False,
                     help="Validate pickle against")
    
    this_module = sys.modules[__name__]
    test_classes = {}
    
    for cls in [getattr(this_module, cls_name) for cls_name in dir(this_module) if ( 
                          inspect.isclass(getattr(this_module, cls_name))  ) and (
                          issubclass(getattr(this_module, cls_name), MigrationTest)) and (
                          cls_name != 'MigrationTest')]:
        parser.add_option('-'+cls.switch, '--'+cls.name, dest=cls.name, 
                          default=False, help=cls.__doc__, action="store_true")
        test_classes[cls.name] = cls
    
    (options, args) = parser.parse_args()
    options.args = args
    
    if options.config_file:
        print 'Loading config file'
        config_module = __import__(options.config_file.replace('.py', ''))
        for attr in dir(config_module):
            setattr(options, attr, getattr(config_module, attr)) 
     
    if options.collect and not options.filename and not options.validate:
        print 'You cannot specify collect and not validate without the filename option'
        sys.exit()
    
    connection_info = {'admin_username' : options.admin_username,
                       'admin_password' : options.admin_password,
                       'server_url'     : options.server_url,
                       'hub_pass'       : options.hub_pass,
                       'pdb'            : getattr(options, 'pdb', False)}
    
    if options.collect:
        store = {}
    elif options.validate:
        f = open(options.filename, 'r')
        print 'Loading pickle'
        store = pickle.load(f)
    
    tests = []
    for name, cls in test_classes.items():
        if getattr(options, name):
            test = cls(connection_info, store)
            test.setup()
            if getattr(options, name) is True:
                if options.collect and hasattr(test, 'collect'):
                    if not getattr(options, 'pdb', None):
                        test.collect()
                    else:
                        import pdb
                        try:
                            test.collect()
                        except:
                            pdb.post_mortem(sys.exc_info()[2])
                if options.validate and hasattr(test, 'validate'):
                    if not getattr(options, 'pdb', None):
                        test.validate()
                    else:
                        import pdb
                        try:
                            test.validate()
                        except:
                            pdb.post_mortem(sys.exc_info()[2])
            tests.append(test)
    print '+++++++++++++++++++++++++++++++++\n'.join( ['\n'.join(test.end_strings) for test in tests] )
    
    if os.path.isfile(options.filename):
        print 'pickle store filename exists, overwritting'
    f = open(options.filename, 'w')
    pickle.dump(store, f, pickle.HIGHEST_PROTOCOL)
    f.flush()
    f.close()
            
if __name__ == "__main__":
    
    main()

    # def get_number_of_users(connection_info, store):
    #     
    # 
    # def get_all_cosmo_events(server_url, username, password, filename):
    #     
    #     import cosmoclient
    #     import cPickle as pickle
    #     
    #     client = cosmoclient.CosmoClient(server_url)
    #     print 'Setting Auth'
    #     client.set_basic_auth(username, password)
    #     print 'Getting User events'
    #     all_events = client.get_all_users_events()
    #     
    #     f = open(filename, 'w')
    #     
    #     pickle.dump(all_events, f, pickle.HIGHEST_PROTOCOL)
    #     f.flush()
    #     f.close()
    #     print 'Created pickled all_events list at %s' % filename
    #     
    # def compare_pickle_events_to_cosmo(server_url, username, password, filename):
    #     
    #     import cosmoclient
    #     import cPickle as pickle
    #     
    #     client = cosmoclient.CosmoClient(server_url)
    #     print 'Setting Auth'
    #     client.set_basic_auth(username, password)
    #     
    #     f = open(filename, 'r')
    #     
    #     print 'Loading pickle'
    #     all_events = pickle.load(f)
    #     
    #     total = 0
    #     passed = 0
    #     failed = 0
    #     
    #     print 'Starting validation'
    #     for user, events in all_events.items():
    #         for event in events:
    #             print 'Verifying %s' % event['href']
    #             body = client.get(event['href'].replace(client._url.geturl(), ''))
    #             try:
    #                 total = total + 1
    #                 assert event['body'] == body
    #                 passed = passed + 1
    #             except AssertionError:
    #                 failed = failed + 1
    #                 print "failure in %s" % event['href']
    #                 print "Pre::%s" % event['body']
    #                 print "Post::%s"% body
    #     
    #     print "Ran %s tests. %s passed. %s failed" % (total, passed, failed)



