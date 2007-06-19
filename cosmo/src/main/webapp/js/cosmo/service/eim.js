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

dojo.provide("cosmo.service.eim");

cosmo.service.eim.constants = {
    prefix: {
        ITEM: "item",
        NOTE: "note",
        EVENT: "event",
        TASK: "task",
        MAIL: "mail",
        MODBY: "modby",
        OCCURRENCE: "occurrence"
    },
    
    ns: {
        ITEM: "http://osafoundation.org/eim/item/0",
        NOTE: "http://osafoundation.org/eim/note/0",
        EVENT: "http://osafoundation.org/eim/event/0",
        TASK: "http://osafoundation.org/eim/task/0",
        MAIL: "http://osafoundation.org/eim/mail/0",
        MODBY: "http://osafoundation.org/eim/modifiedBy/0",
        OCCURRENCE: "http://osafoundation.org/eim/occurrence/0"
    },
        
    type: {
        TEXT: "text",
        INTEGER: "integer",
        CLOB: "clob",
        DECIMAL: "decimal"
    }
}