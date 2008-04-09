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

dojo.require("cosmo.view.cal.common");

cosmo.view.cal.conflict = new function() {

    var self = this; // Closure
    var evReg = null; // Event registry

    // Clearing conflict-info, sorting
    /**
     * Sorts events by start and then end, and clears the collision
     * and tiling placement properties for recalculation
     * FIXME: Separate out sort and clearing collision props
     * Sort should go somewhere generic like cosmo.view.cal.canvasa
     * @return Boolean, true.
     */
    function sortAndClearEvents() {
        var ev = null;

        /**
         * Clear the conflict props on an event
         * @param key String, the Hash key for the event's entry in the Hash
         * @param val CalItem obj, the calendar event to clear
         * conflict properties on
         */
        function clearProps(key, val) {
            ev = val;
            ev.beforeConflicts = [];
            ev.afterConflicts = [];
            ev.conflictDepth = 0;
            ev.maxDepth = 0;
            ev.allDayRow = 0;
        };

        // Sort all the events by start
        cosmo.model.sortEvents(evReg);
        // Reset conflict properties for all the events
        evReg.each(clearProps);

        return true;
    };

    // Timed events
    /**
     * Check all loaded events for conflicts -- (event overlap)
     * Need to look at events that follow the event for conflicts
     * Use in a loop to allow us to jump out as soon as
     * we find an event with a start after the end of the event
     * in question
     * @return Boolean, true.
     */
    function checkConflicts() {
        /**
         * Check conflicts at a specific position in the list --
         * jump out as soon as we find a start after the end of
         * the event in question
         * Only looking for conflicts *after* -- if we find an
         * 'after' conflict we also add it to the 'before' list
         * of the subsequent event at the same time
         * @param i Number, the position in the Hash for the
         * event to check conflicts on.
         */
        function checkConflictAtPos(i) {
            var ev = evReg.getAtPos(i);
            var eventStamp = ev.data.getEventStamp();
            var evStart = eventStamp.getStartDate().getTime();
            var evEnd = eventStamp.getEndDate().getTime();
            var evCheck = null;
            var evCheckStart = 0;

            // Don't check conflicts for non-timed events
            if (eventStamp.getAllDay() || eventStamp.getAnyTime()) {
                return;
            }
            for (var j = i+1; j < evReg.length; j++) {
                evCheck = evReg.getAtPos(j);
                evCheckStart = evCheck.data.getEventStamp().getStartDate().getTime();
                // Quit looking when you see an event that
                // comes after this event's block finishes
                if ((evCheckStart >= evEnd) && (evCheckStart != evStart)) {
                    return;
                }
                // Ignore all non-timed events
                else if (evCheck.data.getEventStamp().getAllDay() || evCheck.data.getEventStamp().getAnyTime()) {
                    // Do nothing
                }
                // Record actual conflicts
                else {
                    ev.afterConflicts.push(evCheck);
                    evCheck.beforeConflicts.push(ev);
                }
            }
        };
        /**
         * Calculate the indent level an event based on the
         * indent level of its preceeding conflicting events
         * @param key String, the Hash key for the event's entry in the Hash
         * @param val CalItem obj, the calendar event to calc the conflict
         * depth for.
         */
        function calcConflictDepth(key, val) {
            var ev = val;
            var beforeEv = null;
            var depthList = [];
            for (var i = 0; i < ev.beforeConflicts.length; i++) {
                beforeEv = ev.beforeConflicts[i];
                depthList.push(beforeEv.conflictDepth);
            }
            // Sort the list of conflict depths -- use explicit numeric sort
            var f = function (a, b) { return (a >= b) ?  1 : -1; };
            depthList.sort(f);
            ev.conflictDepth = findFirstGapInSequence(depthList);
        };
        /**
         * Find the first gap available for indenting an event, in
         * a series of overlapping events
         * @param depthList Array, list of indentation levels for
         * the series of overlapping events.
         * @return Number, first indentation level where there's a
         * an available gap
         */
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
        /**
         * The max indent level of all items that conflict with this
         * one -- used to adjust width of a block
         * @param key String, the Hash key for the event's entry in the Hash
         * @param val CalItem obj, the calendar event to calc the maximum
         * conflict depth for.
         * @return Number, the maximum depth of overlap for this event
         * considering both *before* and *after* conflicts
         */
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
        /**
         * The max depth of all events that conflict with this
         * event in a specific direction -- before, or after
         * @param evArr Array, the conflicting events for this
         * event
         * @return Number, the max level of overlap for this
         * entire contiguous span of conflicting events.
         */
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
     * @return Boolean, true.
     */
    function stackUntimed() {
        // 'Sparse matrix' for untimed events
        var allDayMatrix = [[], [], [], [], [], [], []];

        /**
         * Set the positions in the sparse matrix for all-day
         * event tiling
         * @param key String, the id for the event in the Hash
         * @param val CalItem obj, the event that is being
         * positioned in the resizable all-day event area
         */
        function setMatrixPos(key, val) {
            var ev = val;
            var startCol = 0;
            var endCol = 0;
            var evLength = 0;
            var matrix = allDayMatrix;
            var isPlaced = false;
            var row = 0;
            var eventStamp = ev.data.getEventStamp();
            var allDay = eventStamp.getAllDay();
            var anyTime = eventStamp.getAnyTime();

            // Only look at untimed events
            if (allDay || anyTime) {
                var startDate = eventStamp.getStartDate();
                var endDate = eventStamp.getEndDate();
                // Calc start column and column width
                if (ev.startsBeforeViewRange()) {
                    startCol = 0;
                }
                else {
                    startCol = startDate.getLocalDay();
                }
                if (ev.endsAfterViewRange()) {
                    endCol = 6;
                }
                else {
                    endCol = endDate.getLocalDay();
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
        /**
         * Look in a given row in the matrix to see if there's
         * an empty space from startCol to endCol to put this event
         * @param row Number, the row number in the matrix as
         * it iterates downward along the column
         * @param startCol Number, the left column pos of this event
         * @param endCol Number, the right column end of this eventa
         * @return Boolean, true.
         */
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
    };

    // Public methods
    // *******************
    /**
     * Calculate the conflicts, positions, and indention levels for
     * all the overlapping events on the canvas.
     * @param eR Hash, the itemRegistry of loaded calendar events.
     */
    this.calc = function(iR) {
        evReg = iR;
        return sortAndClearEvents() &&
            checkConflicts() &&
            stackUntimed();
    };
}
cosmo.view.cal.conflict.constructor = null;

