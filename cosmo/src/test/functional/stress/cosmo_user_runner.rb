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

require "cosmo/cosmo_user"
require "cosmo/caldav_user"
require "cosmo/morse_code_user"
require "cosmo/atom_user"

require 'optparse'
require 'drb'

server = "localhost"
port = "8080"
context = "/chandler"
username = "root"
password = "cosmo"
threadMix = [1,0,0]
statsServer = nil
iterations = 100
timed = false
numRounds = 1
debug = false
  
OptionParser.new do |opts|
    opts.banner = "Usage: #{$0} [options]"

    opts.on("-s", "--server [SERVER]", "server address (default localhost)") do |s|
      server = s
    end
    
    opts.on("-c", "--context [CONTEXT]", "application context (default chandler)") do |c|
      context = c
    end
    
    opts.on("-p", "--port [PORT]", "server port (default 8080)") do |p|
      port = p
    end
    
    opts.on("-U", "--user [USER]", "username (default root)") do |u|
      username = u
    end
    
    opts.on("-P", "--password [PASSWORD]", "password (default cosmo)") do |p|
      password = p
    end
    
    opts.on("-m", "--mix x,y,z", Array, "Mix of client threads ([num mc],[num ataom],[num caldav]) (default 1,0,0)") do |list|
      threadMix = list
    end
   
    opts.on("-T", "--time [TIME]", "thread execution time") do |t|
      iterations = t.to_i
      timed = true
    end
    
    opts.on("-i", "--iterations [ITERATIONS]", "number of iterations per thread (default 100)") do |i|
      iterations = i.to_i
    end
    
    opts.on("-r", "--rounds [ROUNDS]", "number of rounds (default 1)") do |r|
      numRounds = r.to_i
    end
    
    opts.on("-S", "--stats [STATS_SEVER]", "stats server") do |ss|
      statsServer = ss
    end
    
    opts.on("-v", "--verbose", "enable verbose output") do |v|
      debug = true
    end
    
    # No argument, shows at tail.  This will print an options summary.
    opts.on_tail("-h", "--help", "Show this message") do
      puts opts
      exit
    end
    
end.parse!
   
if(debug==true)
  Logger['CalDAVClient'].outputters = Outputter.stdout
  Logger['CalDAVClient'].level = Log4r::DEBUG
  Logger['AtomClient'].outputters = Outputter.stdout
  Logger['AtomClient'].level = Log4r::DEBUG
  Logger['MorseCodeClient'].outputters = Outputter.stdout
  Logger['MorseCodeClient'].level = Log4r::DEBUG
  Logger['CMPClient'].outputters = Outputter.stdout
  Logger['CMPClient'].level = Log4r::DEBUG
end

if(statsServer.nil?)
  stats = CosmoUserStats.new
else
  DRb.start_service()
  drbAddress = "druby://#{statsServer}"
  puts drbAddress
  stats = DRbObject.new(nil, drbAddress)
end

for i in 1..numRounds
  threads = []
  users = []

  for i in 1..threadMix[0].to_i
    user = MorseCodeUser.new(server, port, context, username, password, iterations, timed, stats)
    users << user
    thread = Thread.new { user.run }
    threads << thread
  end

  for i in 1..threadMix[1].to_i
    user = AtomUser.new(server, port, context, username, password, iterations, timed, stats)
    users << user
    thread = Thread.new { user.run }
    threads << thread
  end

  for i in 1..threadMix[2].to_i
    user = CalDAVUser.new(server, port, context, username, password, iterations, timed, stats)
    users << user
    thread = Thread.new { user.run }
    threads << thread
  end
  
  for t in threads
    t.join
  end

end

puts stats.to_s

puts "done!"


