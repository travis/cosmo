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

def get_all_cosmo_events(server_url, username, password, filename):
    
    import cosmoclient
    import cPickle as pickle
    
    client = cosmoclient.CosmoClient(server_url)
    print 'Setting Auth'
    client.set_basic_auth(username, password)
    print 'Getting User events'
    all_events = client.get_all_users_events()
    
    f = open(filename, 'w')
    
    pickle.dump(all_events, f, pickle.HIGHEST_PROTOCOL)
    f.flush()
    f.close()
    print 'Created pickled all_events list at %s' % filename
    
def compare_pickle_events_to_cosmo(server_url, username, password, filename):
    
    import cosmoclient
    import cPickle as pickle
    
    client = cosmoclient.CosmoClient(server_url)
    print 'Setting Auth'
    client.set_basic_auth(username, password)
    
    f = open(filename, 'r')
    
    print 'Loading pickle'
    all_events = pickle.load(f)
    
    print 'Starting validation'
    for user, events in all_events.items():
        for event in events:
            print 'Verifying %s' % event['href']
            body = client.get(event['href'].strip(client._url.geturl()))
            assert event['body'] == body
            
def main():

    from optparse import OptionParser

    parser = OptionParser()
    parser.add_option("-s", "--server_url", dest="server_url",
                     help='Full http or https url with port')
    parser.add_option("-u", "--username", dest="username",
                  help='Cosmo username')
    parser.add_option("-p", "--password", dest="password",
                   help='Cosmo password')
    parser.add_option("-f", "--file", dest="filename",
                     help="Filename for all_event list pickle", metavar="FILE")
    parser.add_option("-g", "--get",
                     action="store_true", dest="get", default=False,
                     help="Get initial dataset and pickle it")
    parser.add_option("-v", "--validate",
                     action="store_true", dest="validate", default=False,
                     help="Validate pickle against")
    
    (options, args) = parser.parse_args()
    options.args = args

    if options.get is True:
        get_all_cosmo_events(options.server_url, options.username, options.password, options.filename)
        
    if options.validate is True:
        compare_pickle_events_to_cosmo(options.server_url, options.username, options.password, options.filename)
            
if __name__ == "__main__":
    
    main()
    