/*
 * Copyright 2006 Open Source Applications Foundation
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

dojo.provide('cosmo.view.cal.conflict');

cosmo.view.cal.conflict = new function() {
    
    // Public methods
    // ****************
    // Clearing conflict info, sorting
    /**
     * Sorts events by start and then end, and clears the collision
     * and tiling placement properties for recalculation
     * FIXME: Separate out sort and clearing collision props
     * Sort should go somewhere generic like cosmo.view.cal.canvas
     */
    this.sortAndClearEvents = function(evReg) {
        
        var ev = null;
        // Sorting events by start
        function comp(a, b) {
            utcA = a.data.start.toUTC();
            utcB = b.data.start.toUTC();
            if (utcA > utcB) {
                return 1;
            }
            else if (utcA < utcB) {
                return -1;
            }
            // If start is equal, sort longer events first
            else {
                utcA = a.data.end.toUTC();
                utcB = b.data.end.toUTC();
                if (utcA < utcB) {
                    return 1;
                }
                // Be sure to handle equal values
                else if (utcA >= utcB) {
                    return -1;
                }
            }
        };
        // Clear the conflict props on an event
        function clearProps(key, val) {
            ev = val;
            ev.beforeConflicts = [];
            ev.afterConflicts = [];
            ev.conflictDepth = 0;
            ev.maxDepth = 0;
            ev.allDayRow = 0;
        };
        
        // Sort all the events by start
        evReg.sort(comp);
        // Reset conflict properties for all the events
        evReg.each(clearProps);
        
        return true;
    };

    // Timed events
    /**
     * Check all loaded events for conflicts -- (event overlap)
     * Need to look at events that follow the event for conflicts
     * Use call to method in loop to allow us to jump out as soon as
     * we find an event with a start after the end of the event
     * in question
     */
    this.checkConflicts = function(evReg) {
        
        // Check conflicts at a specific position in the list --
        // jump out as soon as we find a start after the end of
        // the event in question
        // Only looking for conflicts *after* -- if we find an
        // 'after' conflict we also add it to the 'before' list
        // of the subsequent event at the same time
        function checkConflictAtPos(i) {
            var ev = evReg.getAtPos(i);
            var evStart = ev.data.start.getTime();
            var evEnd = ev.data.end.getTime();
            var evCheck = null;
            var evCheckStart = 0;

            // Don't check conflicts for all day events
            if (ev.data.allDay) {
                return;
            }
            for (var j = i+1; j < evReg.length; j++) {
                evCheck = evReg.getAtPos(j);
                evCheckStart = evCheck.data.start.getTime();
                // Quit looking when you see an event that
                // comes after this event's block finishes
                if ((evCheckStart >= evEnd) && (evCheckStart != evStart)) {
                    return;
                }
                // Ignore all day events
                else if (evCheck.data.allDay) {
                    // Do nothing
                }
                // Record actual conflicts
                else {
                    ev.afterConflicts.push(evCheck);
                    evCheck.beforeConflicts.push(ev);
                }
            }
        };

        // Calculate the indent level an event based on the
        // indent level of its preceeding conflicting events
        function calcConflictDepth(key, val) {
            var ev = val;
            var beforeEv = null;
            var depthList = [];
            for (var i = 0; i < ev.beforeConflicts.length; i++) {
                beforeEv = ev.beforeConflicts[i];
                depthList.push(beforeEv.conflictDepth);
            }
            ev.conflictDepth = findFirstGapInSequence(depthList);
        };
        
        // Find the first gap available for indenting an event
        function findFirstGapInSequence(depthList) {
            if (depthList.length) {
                for (var i = 0; i < depthList.length; i++) {
                    if (i != depthList[i]) {
                        return i;
                    }
                }
                return i;
            }
            else {
                return  0;
            }
        };
        
        // The max indent level of all items that conflict with this
        // one -- used to adjust width of a block
        function calcMaxDepth(key, val) {
            var ev = val;
            var max = 0;
            var maxBefore = getMaxDepth(ev.beforeConflicts);
            var maxAfter = getMaxDepth(ev.afterConflicts);
            max = ev.conflictDepth;
            max = maxBefore > max ? maxBefore : max;
            max = maxAfter > max ? maxAfter : max;
            ev.maxDepth = max;
        };
        
        // The max depth of all events that conflict with this
        // event in a specific direction -- before, or after
        function getMaxDepth(evArr) {
            var max = 0;
            for (var i = 0; i < evArr.length; i++) {
                max =  evArr[i].conflictDepth > max ?
                    evArr[i].conflictDepth : max;
            }
            return max;
        };
        
        // Create the list of 'after' conflicts for each event
        // The event also adds itself as a 'before' conflict
        // for 'after' conflicts it finds
        for (var i = 0; i < evReg.length; i++) {
            checkConflictAtPos(i);
        }

        // Calc the actual indent level for each event
        evReg.each(calcConflictDepth);

        // Calc the full width of all the overlapped events
        // for the area to figure the width of the blocks
        evReg.each(calcMaxDepth);

        return true;
    };
    
    // All-day events
    /**
     * Set matrix position for each untimed event
     */
    this.stackUntimed = function(evReg) {
    
        // 'Sparse matrix' for untimed events
        var allDayMatrix = [[], [], [], [], [], [], []];
    
        function setMatrixPos(key, val) {
            var ev = val;
            var startCol = 0;
            var endCol = 0;
            var evLength = 0;
            var matrix = allDayMatrix;
            var isPlaced = false;
            var row = 0;

            // Only look at untimed events
            if (ev.data.allDay) {
                // Calc start column and column width
                if (ev.startsBeforeViewRange()) {
                    startCol = 0;
                }
                else {
                    startCol = ev.data.start.getLocalDay();
                }
                if (ev.endsAfterViewRange()) {
                    endCol = 6;
                }
                else {
                    endCol = ev.data.end.getLocalDay();
                }
                evLength = endCol - startCol;

                // Move row-by-row looking for a space
                // that's wide enough for this event
                while (!isPlaced) {
                    // Found a place -- record its row pos
                    if (placeEventInRow(row, startCol, endCol)) {
                        isPlaced = true;
                        ev.allDayRow = row;
                        return true;
                    }
                    // Keep looking
                    else {
                        isPlaced = false;
                        row++;
                    }
                }
            }
        }
        
        //Look in a given row in the matrix to see if there's
        // an empty space from startCol to endCol to put this event
        function placeEventInRow(row, startCol, endCol) {
            var matrix = allDayMatrix;
            // If we find a value in any of the positions, the
            // space is already occupied ... sorry, Charlie
            for (var i = startCol; i < endCol+1; i++) {
                if (matrix[i][row]) {
                    return false;
                }
            }
            // If we look at the entire horiz range, and it's open,
            // record values for each position in the matrix this
            // event occupies
            for (var i = startCol; i < endCol+1; i++) {
                matrix[i][row] = 1;
            }
            return true;
        }
        
        evReg.each(setMatrixPos);
        return true;
    }
}
cosmo.view.cal.conflict.constructor = null;


