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

require "cosmo/morse_code_client"
require "cosmo/cmp_client"

module Cosmo

  class MorseCodeUser < CosmoUser
    
    MAX_COL_SIZE = 3000
    MEAN_COL_SIZE = 50
    DELETE_PROB = 0.333
    
    OPERATIONS = [:publish, :sync, :update, :subscribe, :delete]
    PROBS = [0.005, 0.765, 0.15, 0.0775, 0.0025]
             
    class CollectionHolder
      def initialize(uid, itemUids, sync_token)
        # array of item uids
        @itemUids = itemUids
        # uid of collection
        @uid = uid
        # set of all tokens generated from publish/updates
        @tokens = [sync_token]
      end
    
      # return a random sync token from the list of known tokens
      def randomToken
        @tokens[rand(@tokens.size)]
      end
      
      def randomItemUid
        @itemUids[rand(@itemUids.size)]
      end
      
      attr_accessor :tokens, :itemUids
      attr_reader :doc, :uid
    end 
    
    def initialize(server, port, context, user, pass, iterations=1, timeBased=false, stats=nil)
      super(server, port, context, user, pass, iterations, timeBased, stats)
      @mcClient = MorseCodeClient.new(server, port,context, @user, @pass)
    end
    
    def registerStats
      @stats.registerStatMap(:mcPublish, "morse code publish")
      @stats.registerStatMap(:mcSync, "morse code sync")
      @stats.registerStatMap(:mcNoChangeSync, "morse code noChangeSync")
      @stats.registerStatMap(:mcUpdate, "morse code update")
      @stats.registerStatMap(:mcDelete, "morse code delete")
      @stats.registerStatMap(:mcSubscribe, "morse code subscribe")
    end
    
    def preRun
      # set of all collections published by current user
      @collections = {}
    end
    
    def runIteration
      # wait a random time before continuing with the next operation
        randomWait
        # must have a collection, so if there isn't one...that is our operation
        if(@collections.size==0)
          collection = createCollection
          # only add collection to current set if publish succeeded
          if(!collection.nil?)
            @collections[collection.uid] = collection
          end
        else
          # otherwise figure out what operation to perform
          operation = getNextOperation(OPERATIONS, PROBS)
          case operation
            when :publish
              collection = createCollection
              if(!collection.nil?)
                @collections[collection.uid] = collection
              end
            when :sync
              collection = @collections.to_a[rand(@collections.size)][1]
              # do a no-change sync 20% of the time
              if(rand < 0.20)
                syncNoChangeCollection(collection.uid, collection.tokens.last)
              # and pick a random token the other 80% of the time
              else
                syncCollection(collection.uid, collection.randomToken)
              end
            when :update
              collection = @collections.to_a[rand(@collections.size)][1]
              updateCollection(collection)
            when :subscribe
              collection = @collections.to_a[rand(@collections.size)][1]
              subscribeCollection(collection.uid)
            when :delete
              collection = @collections.to_a[rand(@collections.size)][1]
              deleted = deleteCollection(collection.uid)
              @collections.delete(collection.uid) if deleted==true
          end
        end
    end
    
    def randomWait
      sleep(rand/2.to_f)
    end
    
    def createCollection
      colUid = random_string(40)
      itemUids = []
      for num in 0..random_collection_size
        itemUids << random_string(40)
      end
      colXml = generateCollectionXml(colUid, itemUids, [])
      resp = @mcClient.publish(colUid, colXml)
      if(resp.code==201)
        @stats.reportStat(:mcPublish, true, resp.time, colXml.length, nil, 201)
        return CollectionHolder.new(colUid, itemUids, resp.sync_token)
      else
        @stats.reportStat(:mcPublish, false, nil, nil, nil, resp.code)
        return nil
      end
    end
    
    def syncCollection(uid, token=nil)
      @mcClient.sync_token = token if !token.nil?
      resp = @mcClient.sync(uid)
      if(resp.code==200)
        @stats.reportStat(:mcSync, true, resp.time, nil, resp.data.length, 200)
      else
        @stats.reportStat(:mcSync, false, nil, nil, nil, resp.code) 
      end
      return resp.sync_token
    end
    
    def syncNoChangeCollection(uid, token)
      @mcClient.sync_token = token
      resp = @mcClient.sync(uid)
      if(resp.code==200)
        @stats.reportStat(:mcNoChangeSync, true, resp.time, nil, resp.data.length, 200)
      else
        @stats.reportStat(:mcNoChangeSync, false, nil, nil, nil, resp.code) 
      end
      return resp.sync_token
    end
    
    def subscribeCollection(uid)
      resp = @mcClient.subscribe(uid)
      if(resp.code==200)
        @stats.reportStat(:mcSubscribe, true, resp.time, nil, resp.data.length, 200)
      else
        @stats.reportStat(:mcSubscribe, false, nil, nil, nil. resp.code) 
      end
      return resp.sync_token
    end
    
    def deleteCollection(uid)
      resp = @mcClient.delete(uid)
      @stats.reportStat(:mcDelete, resp.code==204, resp.time, nil, nil, resp.code)
      return (resp.code==204)
    end
    
    def updateCollection(collection)
      # update existing item and add an item, and maybe delete an item
      # if the collection is big enough
      modifyUid = collection.randomItemUid
      modifyUids = [modifyUid, random_string(40)]
      deleteUid = nil
      deleteUids = []
      if(collection.itemUids.size>5 && rand <= DELETE_PROB)
        deleteUid = collection.randomItemUid
        while deleteUid == modifyUid
          deleteUid = collection.randomItemUid
        end
        deleteUids << deleteUid
      end
      updateXml = generateCollectionXml(collection.uid, modifyUids, deleteUids)
      
      @mcClient.sync_token = collection.tokens.last
      resp = @mcClient.update(collection.uid, updateXml)
      
      if(resp.code==204)
        @stats.reportStat(:mcUpdate, true, resp.time, updateXml.length, nil, 204)
        collection.tokens << resp.sync_token
        collection.itemUids << modifyUids.last
        collection.itemUids.delete(deleteUid) if !deleteUid.nil?
      else
        @stats.reportStat(:mcUpdate, false, nil, nil, nil, resp.code)
      end
    end
     
    def generateCollectionXml(uid, modifyItemUids, deleteItemUids=nil)
      xml =<<EOF
      <eim:collection uuid="#{uid}"
                  name="#{random_string}"
                  xmlns:eim="http://osafoundation.org/eim/0">
EOF
  
      for itemUid in modifyItemUids
        xml << generateItemXml(itemUid)
      end
      
      for itemUid in deleteItemUids
        xml << generateDeleteItemXml(itemUid)
      end
      
      xml << "</eim:collection>"
      return xml
    end
    
    def generateItemXml(uid)
      recurring = (rand < 0.05)
      recordset = <<EOF
      <eim:recordset uuid="#{uid}">
        <item:record xmlns:item="http://osafoundation.org/eim/item/0">
          <item:uuid eim:key="true" eim:type="text">#{uid}</item:uuid>
          <item:title eim:type="text">#{random_string}</item:title>
          <item:triage eim:type="text">100 10.00 1</item:triage>
          <item:createdOn eim:type="decimal">1171318773</item:createdOn>
        </item:record>
        <modby:record xmlns:modby="http://osafoundation.org/eim/modifiedBy/0">
    		<modby:uuid eim:key="true" eim:type="text">#{uid}</modby:uuid>
    		<modby:timestamp eim:key="true" eim:type="decimal">1171318773</modby:timestamp>
    		<modby:userid eim:key="true" eim:type="text">user1</modby:userid>
    		<modby:action eim:key="true" eim:type="integer">100</modby:action>
    	</modby:record>
        <note:record xmlns:note="http://osafoundation.org/eim/note/0">
          <note:uuid eim:key="true" eim:type="text">#{uid}</note:uuid>
          <note:body eim:type="clob">#{random_string}</note:body>
          <note:icalUid eim:type="text">#{uid}</note:icalUid>
        </note:record>
        #{generateEventXml(uid)}
        #{generateMessageXml(uid) if rand < 0.05}
        #{generateDisplayAlarmXml(uid) if rand < 0.03}
      </eim:recordset>
EOF
    end
    
    def generateDeleteItemXml(uid)
      "<eim:recordset eim:deleted=\"true\" uuid=\"#{uid}\"/>"
    end
    
    def generateEventXml(uid)
      if rand < 0.05
        return generateRecurringEventXml(uid)
      else
        return generateStandardEventXml(uid)
      end
    end
    
    def generateStandardEventXml(uid)
      recordset = <<EOF
      <event:record xmlns:event="http://osafoundation.org/eim/event/0">
          <event:uuid eim:key="true" eim:type="text">#{uid}</event:uuid>
          <event:dtstart eim:type="text">#{random_date}</event:dtstart>
          <event:duration eim:type="text">#{random_duration}</event:duration>
          <event:location eim:type="text"/>
          <event:rrule eim:type="text"/>
          <event:exrule eim:type="text" />
          <event:rdate eim:type="text" />
          <event:exdate eim:type="text" />
          <event:status eim:type="text">CONFIRMED</event:status>
        </event:record>
EOF
    end
    
    def generateRecurringEventXml(uid)
      recordset = <<EOF
      <event:record xmlns:event="http://osafoundation.org/eim/event/0">
          <event:uuid eim:key="true" eim:type="text">#{uid}</event:uuid>
          <event:dtstart eim:type="text">;VALUE=DATE-TIME:20070501T080000</event:dtstart>
          <event:duration eim:type="text">#{random_duration}</event:duration>
          <event:location eim:type="text"/>
          <event:rrule eim:type="text">;FREQ=DAILY;UNTIL=20080506T045959Z</event:rrule>
          <event:exrule eim:type="text" />
          <event:rdate eim:type="text" />
          <event:exdate eim:type="text" />
          <event:status eim:type="text">CONFIRMED</event:status>
        </event:record>
EOF
    end
    
    def generateDisplayAlarmXml(uid)
       recordset = <<EOF
      <displayAlarm:record xmlns:displayAlarm="http://osafoundation.org/eim/displayAlarm/0">
  	  <displayAlarm:uuid eim:key="true" eim:type="text">#{uid}</displayAlarm:uuid>
  	  <displayAlarm:description eim:type="text">display alarm</displayAlarm:description>
  	  <displayAlarm:trigger eim:type="text">;VALUE=DATE-TIME:20080101T075900Z</displayAlarm:trigger>
  	</displayAlarm:record>
EOF
    end
    
    def generateMessageXml(uid)
      recordset = <<EOF
      <mail:record xmlns:mail='http://osafoundation.org/eim/mail/0'>
        <mail:uuid eim:type='text' eim:key='true'>#{uid}</mail:uuid>
        <mail:messageId eim:type='text'>#{random_string(10)}</mail:messageId>
        <mail:headers eim:type='clob'/>
        <mail:fromAddress eim:type='text'>#{random_string(20)}</mail:fromAddress>
        <mail:toAddress eim:type='text'>#{random_string(20)}</mail:toAddress>
        <mail:ccAddress eim:type='text'/>
        <mail:bccAddress eim:type='text'>#{random_string(20)}</mail:bccAddress>
        <mail:originators eim:type='text'>#{random_string(20)}</mail:originators>
        <mail:dateSent eim:type='text'/>
        <mail:inReplyTo eim:type='text'/>
        <mail:references eim:type='clob'>#{random_string}</mail:references>
      </mail:record>
EOF
    end
    
    def random_date
      date = ";VALUE=DATE-TIME:2007" + random_integer_string(12) + random_integer_string(28) +
        "T" + random_integer_string(23) + random_integer_string(59) + "00Z"
    end
    
    def random_duration
      durs = ["PT30M", "PT60M"]
      return durs[rand(durs.size)]
    end
    
    def random_integer_string(max)
      randInt = rand(max)
      while randInt==0
        randInt = rand(max)
      end
      
      if(randInt < 10)
        randInt = "0" + randInt.to_s
      else
        randInt = randInt.to_s
      end
    
      return randInt  
    end
    
    def random_collection_size
      # generate exponentially distributed number
      result = (-Math.log(1.0 - rand)/(1/MEAN_COL_SIZE.to_f)).to_i
      while(result==0 || result > MAX_COL_SIZE)
        result = (-Math.log(1.0 - rand)/(1/MEAN_COL_SIZE.to_f)).to_i
      end
      return result
    end
    
  end
end
