until automated api tests are in place, you can use the xml files in this
directory as content for requests sent with curl, like so:

curl -i -X PUT -u root:cosmo -H 'Content-Type: text/xml; charset="utf-8"' -d @bcm.xml http://localhost:8080/cmp/user/bcm
