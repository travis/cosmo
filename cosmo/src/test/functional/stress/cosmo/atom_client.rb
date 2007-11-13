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
  
  class AtomResponse < BaseServerResponse
    def initialize(resp, data=nil, time=0)
      super(resp, data, time)
    end
  end
  
  class AtomClient < BaseHttpClient
    @@log = Logger.new 'AtomClient'
    
    COL_PATH = "/atom/"
    
    def initialize(server, port, context, user, pass)
      super(server,port,context,user,pass)
    end
    
    def getFullFeed(collection, format=nil, startRange=nil, endRange=nil)
      @@log.debug "getFullFeed #{collection} begin"
      @http.start do |http|
        
        if(format.nil?)
          strRequest = "#{@context}#{COL_PATH}collection/#{collection}/full"
        else
          strRequest = "#{@context}#{COL_PATH}collection/#{collection}/full/#{format}"
        end
        
        strRequest << "?start=#{startRange}&end=#{endRange}" if !startRange.nil?
        req = Net::HTTP::Get.new(strRequest)
        init_req(req)
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "getFullFeed (#{format}) for #{collection} end (#{@reqTime}ms)"
        return AtomResponse.new(resp, data, @reqTime)
      end
    end
    
    def getDashboardFeed(collection, triage_status="dashboard-now", format=nil)
      @@log.debug "getDashboardFeed #{collection} begin"
      @http.start do |http|
        
        if(format.nil?)
          strRequest = "#{@context}#{COL_PATH}collection/#{collection}/#{triage_status}"
        else
          strRequest = "#{@context}#{COL_PATH}collection/#{collection}/#{triage_status}/#{format}"
        end
        
        req = Net::HTTP::Get.new(strRequest)
        init_req(req)
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "getDashboardFeed (#{triage_status}) for #{collection} end (#{@reqTime}ms)"
        return AtomResponse.new(resp, data, @reqTime)
      end
    end
    
    def createEntry(collection, body)
      @@log.debug "post #{collection} begin"
      @http.start do |http|
      
        strRequest = "#{@context}#{COL_PATH}collection/#{collection}"
       
        req = Net::HTTP::Post.new(strRequest)
        init_req(req)
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'application/atom+xml'
        resp, data = time_block { http.request(req, body) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "post for #{collection} end (#{@reqTime}ms)"
        return AtomResponse.new(resp, data, @reqTime)
      end
    end
    
    def updateEntry(item,  body)
        @@log.debug "put #{item} begin"
        @http.start do |http|
      
        strRequest = "#{@context}#{COL_PATH}item/#{item}"
       
        req = Net::HTTP::Put.new(strRequest)
        init_req(req)
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'application/atom+xml'
        resp, data = time_block { http.request(req, body) }
        @@log.debug "received code #{resp.code}"
        @@log.debug "put for #{item} end (#{@reqTime}ms)"
        return AtomResponse.new(resp, data, @reqTime)
      end
    end
  end
end
