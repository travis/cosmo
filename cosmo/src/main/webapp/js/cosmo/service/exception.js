/*
 * Copyright 2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

dojo.provide("cosmo.service.exception");

dojo.declare("cosmo.service.exception.ServiceException", Error,
    // summary: The root of all service exceptions. If your error is not an instance
function(){}, {});

dojo.declare("cosmo.service.exception.CollectionLockedException", cosmo.service.exception.ServiceException,
    // summary: Thrown when another client is trying to update the item on the server
function(){}, {});

dojo.declare("cosmo.service.exception.CantSaveException", cosmo.service.exception.ServiceException,
    // summary: Thrown when we encounter problems with saving an entity.
function(){}, {});

dojo.declare("cosmo.service.exception.ConflictException", cosmo.service.exception.ServiceException,
    // summary: Thrown when we receive a 409.
function(){}, {});

dojo.declare("cosmo.service.exception.ConcurrencyException", cosmo.service.exception.ServiceException,
    // summary: Thrown when we receive a 409.
function(){}, {});

dojo.declare("cosmo.service.exception.ResourceNotFoundException", cosmo.service.exception.ServiceException, 
{
    initializer: function(id){
        //sumamry: Thrown when a resource could not be retrieved from the service.
        //id: The identifier that was used when try to retrieve the item. In RESTful services, this
        //    would be the URL. 
        this.id = id;
        this.message = this.toString();
    }, 
    
    toString: function(){
        return "The resource " + this.id + " does not exist";
    }
});