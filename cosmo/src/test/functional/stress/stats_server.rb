#!/usr/bin/env ruby
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

require 'drb'
require 'optparse'
require "cosmo/cosmo_user.rb"

server = "localhost"
port = "9000"

OptionParser.new do |opts|
    opts.banner = "Usage: #{$0} [options]"

    opts.on("-s", "--server [SERVER]", "server address") do |s|
      server = s
    end
    
    opts.on("-p", "--port [PORT]", "server port") do |p|
      port = p
    end
    
    # No argument, shows at tail.  This will print an options summary.
    opts.on_tail("-h", "--help", "Show this message") do
      puts opts
      exit
    end
    
end.parse!

serverObject = Cosmo::CosmoUserStats.new
DRb.start_service("druby://#{server}:#{port}", serverObject)
DRb.thread.join