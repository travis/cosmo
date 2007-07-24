# Copyright 2007 Open Source Applications Foundation
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require "cosmo/cosmo_user"
require 'log4r'

include Log4r

module Cosmo

  class CMPResponse < BaseServerResponse
    def initialize(resp, data=nil, time=0)
      super(resp,data,time)
    end
  end
  
  class CMPClient < BaseHttpClient
    @@log = Logger.new 'CMPClient'
    
    CMP_PATH = "/cmp/"
   
    def initialize(server, port,context, adminUser, adminPass)
      super(server,port,context, adminUser, adminPass)
    end
    
    def createUser(user, pass, first, last, email)
      @@log.debug "create #{user} begin"
      @http.start do |http|
        req = Net::HTTP::Put.new("#{@context}#{CMP_PATH}user/#{user}")
        req['Content-Type'] = 'text/xml'
        xml = getNewUserXml(user, pass, first, last, email)
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        startTime = Time.now.to_f
        resp, data = http.request(req, xml)
        endTime = Time.now.to_f
        reqTime = ((endTime - startTime) * 1000).to_i
        @@log.debug "received code #{resp.code}"
        @@log.debug "create user #{user} end (#{reqTime}ms)"
        return CMPResponse.new(resp, data, reqTime)
      end
      
    end
    
    def getNewUserXml(user, pass, first, last, email)
      recordset = <<EOF
<user xmlns="http://osafoundation.org/cosmo/CMP">
  <username>#{user}</username>
  <password>#{pass}</password>
  <firstName>#{first}</firstName>
  <lastName>#{last}</lastName>
  <email>#{email}</email>
</user>
EOF
    end
    
  end
end

#Logger['CMPClient'].outputters = Outputter.stdout
#Logger['CMPClient'].level = Log4r::DEBUG
#cmp = CMPClient.new("localhost","8080","root","cosmo")
#cmp.createUser("blabla", "blabla", "blah", "blah", "blah@blah.com")
