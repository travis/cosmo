You can send CalDAV requests with curl, like so:

curl -i -X MKCALENDAR -u bcm:abc123 http://localhost:8080/home/bcm/calendar/

curl -X MKCALENDAR -u bcm:abc123 -H 'Content-Type: text/xml; charset="utf-8"' -d @mkcalendar.xml http://localhost:8080/home/bcm/calendar/
