
    curl -i -u test1:test1 -X REPORT \
         -H 'Content-type: text/xml; charst="utf-8"' \
         -d @timerangequery/1.xml \
         http://localhost:8080/cosmo/home/test1/testcalendar/

* The current set of test suites are:

Test Suite for Cosmo CalDAV report testing.

Requirements
____________

*Cosmo 0.3+ server

*Test account on server with:

    username: 'test1'
    password: 'test1'

*Create new calendar for user test:

    curl -i -u test1:test1 -X MKCALENDAR \
         http://localhost:8080/cosmo/home/test1/calendar/

The multiget test files have hardcoded URLs in them, so if you choose
a different username, calendar name, etc  you will need to locally
modify those test files.


Testing Procedure
_________________

* The put subdirectory contains a set of calendar resources to store on
  the server as test data. These resources should be PUT into a
  calendar collection on the server before running any of the
  tests. Example:

    curl -i -u test1:test1 -T put/1.ics \
         http://localhost:8080/cosmo/home/test1/calendar/

* Testing is done by sending REPORT requests to the server with
  content from XML files in the various subdirectories. Example:

    curl -i -u test1:test1 -X REPORT \
         -H 'Content-type: text/xml; charst="utf-8"' \
         -d @basicquery/1.xml \
         http://localhost:8080/cosmo/home/test1/calendar/

* The current set of test suites are:

        - multiget       : multiget reports (used for testing various
                           calendar-data element variants too)
        - basicquery     : basic text queries
        - timerangequery : time-range queries
        - freebusy       : free busy reports
        - limitexpand    : queries using limit/expand recurrence


Resources
_________

1.txt: basic VEVENT, summary "event 1" (tzid=US/Eastern)
2.txt: basic VEVENT, summary "event 2" (tzid=US/Mountain), has
description
3.txt: basic VEVENT, summary "event 3" (tzid=US/Pacific)
4.txt: basic VEVENT with VALARM, summary "event 4"
5.txt: recurring VEVENT (5 consecutive days), summary "event 5"
6.txt: recurring VEVENT (5 consecutive days, one exception), summary
"event 6"
7.txt: as 6.txt but with THISANDFUTURE, summary "event 7"


Detail
______

* multiget:

	1.txt:
		basic multiget of 4 resources returning etag and entire ics data
		result: 1.ics, 2.ics, 3.ics, 4.ics
		
	2.txt:
		basic multiget of 4 resources returning etag and only VCALENDAR
property data (no embedded components)
		result: 1.ics, 2.ics, 3.ics, 4.ics

	3.txt:
		basic multiget of 4 resources returning etag and only VTIMEZONE
components
		result: 1.ics, 2.ics, 3.ics, 4.ics

	4.txt:
		basic multiget of 4 resources returning etag and only
SUMMARY/UID properties inside VEVENT components and VALARMs
		result: 1.ics, 2.ics, 3.ics, 4.ics

	5.txt:
		as 4.txt except that the SUMMARY property value is not returned
		result: 1.ics, 2.ics, 3.ics, 4.ics
		
* basicquery:

	Tests:
	
	1.txt:
		query for resources with VCALENDAR & VEVENT defined
		result: 1.ics, 2.ics, 3.ics, 4.ics, 5.ics, 6.ics, 7.ics
		
	2.txt:
		query for resources where the SUMMARY in a VEVENT contains the
character '1'
		result: 1.ics

	3.txt:
		query for resources where the DESCRIPTION property exists in a
VEVENT
		result: 2.ics, 6.ics, 7.ics

	4.txt:
		query for resources that have a DTSTART in a VEVENT that
contains a TZID parameter containing the text 'Paci'
		result: 3.ics

	5.txt:
		query for resources that have a DTSTART in a VEVENT that
contains a TZID parameter containing the text 'Paci' or 'Moun'
		result: 2.ics, 3.ics

	6.txt:
		query for resources where the SUMMARY in a VEVENT contains the
character '4' or has a DTSTART in a VEVENT that contains a TZID
parameter containing the text 'East'
		result: 1.ics, 2.ics, 4.ics, 5.ics, 6.ics, 7.ics

* timerangequery:

	1.txt:
		query for VEVENTs within time range
		result: 5.ics, 6.ics, 7.ics
		
	2.txt
		query for VEVENTs that have a DTSTART within time range
		result: 1.ics, 2.ics, 3.ics, 4.ics
		
	3.txt
		query for VALARMS within time range
		result: 4.ics
	
	4.txt
		query for VEVENTs that have a DTSTART within time range and have
TZID parameter containing the text 'Paci'
		result: 3.ics
			
	5.txt
		query for VEVENTs that have a DTSTART within time range and have
TZID parameter containing the text 'Paci' or 'Moun'
		result: 2.ics, 3.ics

* freebusy:

	1.txt
		query for free busy with time range
		result: synthesised component
		
* limit/expand:

	1.txt:
		time-range query with limit over same range
		result: 5.ics, 6.ics (partial), 7.ics (partial)
		
	2.txt:
		time-range query with limit over different range
		result: 5.ics, 6.ics (full), 7.ics (full)
		
	3.txt:
		time-range query with expand over same range
		result: 5.ics (x3 instances), 6.ics (x3), 7.ics (x2)
		
	4.txt:
		time-range query with expand over different range
		result: 5.ics (x4 instances), 6.ics (x4), 7.ics (x3)
 
