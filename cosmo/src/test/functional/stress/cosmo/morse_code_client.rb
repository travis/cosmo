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
require "rexml/document"
require 'log4r'

include Log4r
include Cosmo

module Cosmo

  class MorseCodeResponse < BaseServerResponse
    
    def initialize(sync_token, resp, data=nil, time=0)
      super(resp, data, time)
      @sync_token = sync_token
    end
    
    def doc
      REXML::Document.new @data if @data
    end
    
    def write_doc
      doc.write( $stdout, 0 )
    end
    
    attr_reader :sync_token
    
  end
  
  class MorseCodeClient < BaseHttpClient
    @@log = Logger.new 'MorseCodeClient'
    
    COL_PATH = "/mc/collection/"
    
    def initialize(server, port, context, user, pass)
      super(server, port, context, user, pass)
    end
    
    attr_accessor :sync_token
    
    def subscribe(collection)
      @@log.debug "subscribe #{collection} begin"
      @http.start do |http|
        req = Net::HTTP::Get.new("#{@context}#{COL_PATH}#{collection}")
        http.read_timeout=600
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @sync_token = resp.response['X-MorseCode-SyncToken']
        @@log.debug "received sync token: #{@sync_token}"
        @@log.debug "subscribe #{collection} end (#{@reqTime}ms)"
        return MorseCodeResponse.new(@sync_token, resp, data, @reqTime)
      end
    end
    
    def sync(collection)
      @@log.debug "sync #{collection} begin"
      @http.start do |http|
        req = Net::HTTP::Get.new("#{@context}#{COL_PATH}#{collection}")
        http.read_timeout=600
        @@log.debug "using synctoken: #{@sync_token}"
  
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['X-MorseCode-SyncToken'] = @sync_token
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @sync_token = resp.response['X-MorseCode-SyncToken']
        @@log.debug "received sync token: #{@sync_token}"
        @@log.debug "sync #{collection} end (#{@reqTime}ms)"
        return MorseCodeResponse.new(@sync_token, resp, data, @reqTime)
      end
    end
    
    def publish(collection, eimml)
      @@log.debug "publish #{collection} begin"
      @http.start do |http|
        req = Net::HTTP::Put.new("#{@context}#{COL_PATH}#{collection}")
        http.read_timeout=600
        
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'application/eim+xml'
        resp, data = time_block { http.request(req, eimml) }
        @@log.debug "received code #{resp.code}"
        @sync_token = resp.response['X-MorseCode-SyncToken']
        @@log.debug "received sync token: #{@sync_token}"
        @@log.debug "publish #{collection} end (#{@reqTime}ms)"
        return MorseCodeResponse.new(@sync_token, resp, data, @reqTime)
      end
    end
    
    def update(collection, eimml)
      @@log.debug "update #{collection} begin"
      @http.start do |http|
        req = Net::HTTP::Post.new("#{@context}#{COL_PATH}#{collection}")
        http.read_timeout=600
        
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['X-MorseCode-SyncToken'] = @sync_token
        req['Content-Type'] = 'application/eim+xml'
        resp, data = time_block { http.request(req, eimml) }
        @@log.debug "received code #{resp.code}"
        @sync_token = resp.response['X-MorseCode-SyncToken']
        @@log.debug "received sync token: #{@sync_token}"
        @@log.debug "update #{collection} end (#{@reqTime}ms)"
        return MorseCodeResponse.new(@sync_token, resp, data, @reqTime)
      end
    end
    
    def delete(collection)
      @@log.debug "delete #{collection} begin"
      @http.start do |http|
        req = Net::HTTP::Delete.new("#{@context}#{COL_PATH}#{collection}")
        http.read_timeout=600
        
        # we make an HTTP basic auth by passing the
        # username and password
        req.basic_auth @user, @pass
        req['Content-Type'] = 'application/eim+xml'
        resp, data = time_block { http.request(req) }
        @@log.debug "received code #{resp.code}"
        @sync_token = resp.response['X-MorseCode-SyncToken']
        @@log.debug "delete #{collection} end (#{@reqTime}ms)"
        return MorseCodeResponse.new(@sync_token, resp, data, @reqTime)
      end
    end
  end
end

#mc = MorseCodeClient.new("localhost","8080","randy","randy")
#mc.delete("col1")
#mc.publish("col1", File.new("test1.xml").read)
#mc.subscribe("col1")
#mc.update("col1", File.new("test1update.xml").read)
#mcr = mc.sync("col1")
#puts "code = #{mcr.code}"
#mcr.doc.write( $stdout, 0 )
