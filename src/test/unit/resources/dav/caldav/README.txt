You can send CalDAV requests with curl, like so:

curl -i -X MKCALENDAR -u test1:test1 http://localhost:8080/cosmo/home/test1/testcalendar

curl -X MKCALENDAR -u test1:test1 -v -H 'Content-Type: text/xml; charset="utf-8"' -d @mkcalendar.xml http://localhost:8080/cosmo/home/test1/testcalendar
