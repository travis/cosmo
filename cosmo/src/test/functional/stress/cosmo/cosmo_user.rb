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

require "net/http"

module Cosmo

  #  Represents a statistics store for a CosmoUser.  Every time an operation
  #  is performed by a CosmoUser, information about the operation is stored
  #  in a statistics map in a CosmoUserStats object.
  class CosmoUserStats
    
    def initialize
      @mutex = Mutex.new
      @stats = {}
    end
    
    def registerStatMap(key, name)
      # can't have more than one thread accessing stats map
      @mutex.synchronize do
        if(@stats[key].nil?)
          @stats[key] = createStatMap(name)
        end
      end
    end
    
    attr_reader :stats
    
    # Create a new statistics map.  A statistics map includes:
    #   name
    #   number of requests made
    #   number of successful requests
    #   number of failed requests
    #   array of succesful request times
    #   array of sizes of requests
    #   array of sizes of reqest responses
    #   map of [responsecode, number of times code received] entries
    def createStatMap(name)
      return { :name=>name, :numRequests=>0, :numSuccesses=>0, :numFailures=>0, 
                      :requestTimes=>[], :dataSent=>[], :dataReceived=>[], :responseCodes=>{} }
    end
    
    # calculate the mean of an array of numbers
    def mean(ary)
      ary.inject(0) { |sum, i| sum += i }/ary.length.to_f 
    end
    
    # calculate the variance of an array of numbers
    def variance(population)
      n = 0
      mean = 0.0
      s = 0.0
      population.each { |x|
        n = n + 1
        delta = x - mean
        mean = mean + (delta / n)
        s = s + delta * (x - mean)
      }
      # if you want to calculate std deviation
      # of a sample change this to "s / (n-1)"
      return s / n
    end
  
    # calculate the standard deviation of a population
    # accepts: an array, the population
    # returns: the standard deviation
    def standard_deviation(population)
      Math.sqrt(variance(population))
    end
    
    # update the relevent statistic map
    def reportStat(statType, success, time=nil, dataSent=nil, dataReceived=nil, responseCode=nil)
      # can't have more than one thread accessing stats map
      @mutex.synchronize do
        statMap = @stats[statType]
        statMap[:numRequests] = statMap[:numRequests] + 1
        
        statMap[:requestTimes] << time if !time.nil?
        statMap[:dataSent] << dataSent if !dataSent.nil?
        statMap[:dataReceived] << dataReceived if !dataReceived.nil?
        
        if (success==true)
          statMap[:numSuccesses] = statMap[:numSuccesses] + 1
        else
          statMap[:numFailures] = statMap[:numFailures] + 1
        end
        
        if(!responseCode.nil?)
          responseCodes = statMap[:responseCodes]
          responseCodes[responseCode] = 0 if responseCodes[responseCode].nil?
          responseCodes[responseCode] = responseCodes[responseCode] + 1;
        end
        
      end
    end
    
    # return the total number of successful operations
    def total_op_successes
      sum = 0 
      @stats.each {|key, statMap|
        sum+=statMap[:numSuccesses]
      }
      return sum
    end
    
    # return the total number of failed operations
    def total_op_failures
      sum = 0 
      @stats.each {|key, statMap|
        sum+=statMap[:numFailures]
      }
      return sum
    end
    
    # return the percentage of succesful operations
    def success_percentage
      return total_op_successes.to_f / (total_op_successes + total_op_failures).to_f
    end
    
    # return string reperesentation of all statistics
    def to_s
       str = ""
       @stats.each {|key, statMap|
         str << "----------------------------\n"
         str << statMap[:name] << " stats\n"
         str << "----------------------------\n"
         str << "requests: " << statMap[:numRequests].to_s << "\n"
         if(statMap[:requestTimes].size>0)
           str << "meanTime: " << mean(statMap[:requestTimes]).to_s << "\n"
           str << "minTime: " << statMap[:requestTimes].min.to_s << "\n"
           str << "maxTime: " << statMap[:requestTimes].max.to_s << "\n"
           str << "stdDeviation: " << standard_deviation(statMap[:requestTimes]).to_s << "\n"
         end
         
         responseCodes = statMap[:responseCodes]
         responseCodes.each {|code, num|
           str << "reponse code " << code.to_s << ": " << num.to_s << "\n"
         } 
         
         if(statMap[:dataSent].size>0)
           str << "meanDataSent: " << mean(statMap[:dataSent]).to_s << "\n"
           str << "minDataSent: " << statMap[:dataSent].min.to_s << "\n"
           str << "maxDataSent: " << statMap[:dataSent].max.to_s << "\n"
         end
         
         if(statMap[:dataReceived].size>0)
           str << "meanDataReceived: " << mean(statMap[:dataReceived]).to_s << "\n"
           str << "minDataReceived: " << statMap[:dataReceived].min.to_s << "\n"
           str << "maxDataReceived: " << statMap[:dataReceived].max.to_s << "\n"
         end
         
         str << "successes: " << statMap[:numSuccesses].to_s << "\n"
         str << "failures: " << statMap[:numFailures].to_s << "\n"
       }
       
       str << "******************************\n"
       str << "total successes: " << total_op_successes.to_s << "\n"
       str << "total failures: " << total_op_failures.to_s << "\n"
       str << "success percentage: " << success_percentage.to_s
       
       return str
    end
    
  end
  
  # Represents a cosmo user, that is a user of a cosmo server.
  class CosmoUser
    
    # Inialize cosmo user
    # server - cosmo server
    # port - port server is running on
    # user - username with admin privileges
    # pass - password
    # iterations - number of iterations to run, or number of seconds to run
    # timBased - if true, iterations represents number of seconds to run
    # stats - CosmoUserStats object to report stats to
    def initialize(server, port, context, user, pass, iterations=1, timeBased=false, stats=nil)
      
      # create new user
      @cmpClient = CMPClient.new(server, port, context, user, pass)
      @user = "user" + random_string(10)
      @pass = "password"
      @cmpClient.createUser(@user, @pass, random_string(15), random_string(15), random_string(15) + "@osafoundation.org")
      @iterations = iterations
      @timeBased = timeBased
      @stats = stats.nil? ? CosmoUserStats.new : stats
      registerStats
    end
    
    # a CosmoUser subclass will override this to register its own stats
    def registerStats
    end
    
    attr_reader :stats
    
    # run the user for the specified number of iterations or time
    def run
      preRun
      if @timeBased==true 
        runTimed
      else
        runIterations
      end
      postRun
    end
    
    # perform logic before run
    def preRun
    end
    
    # perform logic after run
    def postRun
    end
    
    def createUser(user, pass, first, last, email)
      resp = @cmpClient.createUser(user, pass, first, last, email)
      if(resp.code!=201)
        fail("user creation failed!")
      end
    end
    
    # run for a specified number of time (iteration seconds)
    def runTimed
      startTime = Time.now.to_i
      endTime = startTime + @iterations
      while Time.now.to_i < endTime
        runIteration
      end
    end
    
    # run for a specified number of iterations
    def runIterations
      for i in 1..@iterations
        runIteration
      end
    end
    
    # run a single iteration
    def runIteration
    end
    
    # return an operation symbol based on a set of probabilities
    # operations - array of operation symbols
    # probabilities - array of corresponding probabilities (must add to 1.0)
    def getNextOperation(operations, probabilities)
      num = rand
      sum = 0
      for i in 0...probabilities.length
        sum = sum + probabilities[i]
        return operations[i] if num <= sum
      end
    end
    
    def random_string( length=nil )
      length = rand(255) if length.nil?
      chars = ("a".."z").to_a + ("A".."Z").to_a + ("0".."9").to_a
      random_string = ""
      0.upto(length) { |i| random_string << chars[rand(chars.size)] }
      return random_string
    end 
  end
  
  # Base HTTP Client that initializes a Net::HTTP object
  class BaseHttpClient
    
    def initialize(server, port, context, user, pass)
      @http = Net::HTTP.new(server, port)
      @user = user
      @pass = pass
      @context = context
    end
    
    def init_req(req)
      req['User-Agent'] = 'Cosmo Stress HTTP Client 1.0'
    end
    
    # Time the execution of a block of code
    # and store the result (milliseconds) in the 
    # instance variable "reqTime).
    def time_block
      startTime = Time.now.to_f
      return_val = yield
      endTime = Time.now.to_f
      @reqTime = ((endTime - startTime) * 1000).to_i
      return return_val
    end
    
    attr_accessor :user, :pass
  end
  
  # Base server response
  class BaseServerResponse
    def initialize(resp, data=nil, time=0)
      @code = resp.code.to_i
      @message = resp.message
      @data = data
      @time = time
    end
    
    attr_reader :code, :time, :data
    
  end
end