import cosmoclient
import random
import uuid
import os, sys
from xml.etree import ElementTree
import xmlobjects

# def setup_module(module):
#     client = cosmoclient.CosmoClient('http://qacosmo.osafoundation.org')
#     new_user = '%s' % str(uuid.uuid1()).replace('-', '')
#     client.set_basic_auth('root', 'cosmo')
#     client.add_user(new_user, 'test_pass', 'Test', 'User', '%s@me.com' % new_user)
#     assert client.response.status == 201
#     client.set_basic_auth(new_user, 'test_pass')
#     module.client = client
#     module.new_user = new_user
#     
#     # Get home collection url
#     client.get('/atom/user/%s' % new_user)
#     assert client.response.status == 200
#     xobj = xmlobjects.fromstring(client.response.body)
#     assert len(xobj.workspace) is 2
#     assert u'account', u'home' in [xobj.workspace[0].title, xobj.workspace[1].title]
#     home_collection = None
#     for workspace in xobj.workspace:
#         if workspace.title == u'home':
#             home_collection = workspace.collection['href']
#     assert home_collection
#     module.home_collection = home_collection
#     client.headers['Content-Type'] = 'application/atom+xml'
#     client.headers['Accept-Encoding'] = 'application/atom+xml'
# 
# def test_get_home_collection():    
#     client.get('/atom/%s' % home_collection)
#     assert client.response.status == 200
#     xobj = xmlobjects.fromstring(client.response.body)
#     assert len(xobj.entry) is 3
#     
#     
# def test_create_new_entry():
#     """<entry>
#       <id>urn:uuid:264d4fb7-ab97-4b76-a5eb-6732deda86cb</id>
#       <title type="text">Welcome to Chandler Server</title>
#       <updated>2007-07-09T21:32:39.678Z</updated>
#       <published>2007-07-09T21:32:39.678Z</published>
#       <content type="text/html">&lt;div class="vevent">&lt;span class="summary">Welcome to Chandler Server&lt;/span>: &lt;abbr class="dtstart" title="2007-07-09T14:00:00PDT">Jul 9, 2007 2:00 PM PDT&lt;/abbr> to &lt;abbr class="dtend" title="2007-07-09T15:00:00PDT">Jul 9, 2007 3:00 PM PDT&lt;/abbr></content>
#       <summary type="text">Welcome to Chandler Server: Jul 9, 2007 2:00 PM PDT to Jul 9, 2007 3:00 PM PDT</summary>
#       <link rel="self" type="application/atom+xml" href="item/264d4fb7-ab97-4b76-a5eb-6732deda86cb" />
#     </entry>"""
#     xobject = xmlobjects.XMLObject('{http://www.w3.org/2005/Atom}entry')
#     xobject.title = 'Protocol test'
#     xobject.title['type'] = "text"
#     xobject.content = "I've got some info to drop on you"
#     xobject.content['type'] = "text"
#     xobject.summary = "some info"
#     xobject.summary['type'] = "text"
#     client.post('/atom/%s' % home_collection, body=xmlobjects.tostring(xobject))
#     assert client.response.status == 201
    
     