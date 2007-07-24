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
include Cosmo

class Net::HTTP  # reopen
   class Report < Net::HTTPRequest
     METHOD = 'REPORT'
     REQUEST_HAS_BODY  = true
     RESPONSE_HAS_BODY = true
   end   
   
   class MakeCalendar < Net::HTTPRequest
     METHOD = 'MKCALENDAR'
     REQUEST_HAS_BODY  = true
     RESPONSE_HAS_BODY = false
   end   
end

module Cosmo

  class CalDAVResponse < BaseServerResponse
    def initialize(resp, data=nil, time=0)
      super(resp, data, time)
    end
  end
  
  class CalDAVClient < BaseHttpClient
    @@log = Logger.new 'CalDAVClient'
    
    DAV_PATH = "/dav/"
    
    def initialize(server, port, context, user, pass)
      super(server,port,context,user,pass)
    end
    
    def get(path)
      @@log.debug "get #{path} begin"
      @http.start do |http|
        req = Net::HTTP::Get.new("#{@context}#{DAV_PATH}#{path}")
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "get #{path} end (#{@reqTime}ms)"
        return CalDAVResponse.new(resp, data, @reqTime)
      end
    end
    
    def put(path, data)
      @@log.debug "put #{path} begin"
      @http.start do |http|
        req = Net::HTTP::Put.new("#{@context}#{DAV_PATH}#{path}")
        http.read_timeout=600
        
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'text/calendar'
        resp, data = time_block { http.request(req, data) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "put #{path} end (#{@reqTime}ms)"
        return CalDAVResponse.new(resp, data, @reqTime)
      end
    end
    
    
    def delete(path)
      @@log.debug "delete #{path} begin"
      @http.start do |http|
        req = Net::HTTP::Delete.new("#{@context}#{DAV_PATH}#{path}")
        http.read_timeout=600
        
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "delete #{path} end (#{@reqTime}ms)"
        return CalDAVResponse.new(resp, data, @reqTime)
      end
    end
    
    def report(path, reportBody, depth="1")
      @@log.debug "report #{path} begin"
      @http.start do |http|
        req = Net::HTTP::Report.new("#{@context}#{DAV_PATH}#{path}")
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'application/xml'
        req['Depth'] = depth
        resp, data = time_block { http.request(req, reportBody) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "report #{path} end (#{@reqTime}ms)"
        return CalDAVResponse.new(resp, data, @reqTime)
      end
    end
    
     def makeCalendar(path, calendarBody)
      @@log.debug "mkcalendar #{path} begin"
      @http.start do |http|
        req = Net::HTTP::MakeCalendar.new("#{@context}#{DAV_PATH}#{path}")
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'application/xml'
        resp, data = time_block { http.request(req, calendarBody) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "mkcalendar #{path} end (#{@reqTime}ms)"
        return CalDAVResponse.new(resp, data, @reqTime)
      end
    end
    
  end
end

#Logger['CalDAVClient'].outputters = Outputter.stdout
#Logger['CalDAVClient'].level = Log4r::DEBUG

#cdc = CalDAVClient.new("localhost","8080","randy","randy")
#cdc.delete("randy/testcal")
#cdc.makeCalendar("randy/testcal", File.new("calendar.xml").read)
#cdc.put("randy/testcal/1.ics", File.new("event1.ics").read)
