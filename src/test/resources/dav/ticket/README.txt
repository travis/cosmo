until automated dav tests are in place, you can use the xml files in this
directory as content for requests sent with curl, like so:

curl -X MKTICKET -u root:cosmo -H 'Content-Type: text/xml; charset="utf-8"' -d
@mkticket.xml http://localhost:8080/home/

curl -X DELTICKET -u root:cosmo -H 'Ticket: deadbeef' http://localhost:8080/home/
