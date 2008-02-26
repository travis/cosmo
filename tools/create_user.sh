#!/bin/bash
UNAME=`uuidgen`
UNAME=${UNAME:0:8}
generate_user()
{
cat <<-EOF
<?xml version="1.0" encoding="UTF-8"?>
<user xmlns="http://osafoundation.org/cosmo/CMP">
    <username>$UNAME</username>
    <firstName>$UNAME</firstName>
    <lastName>$UNAME</lastName>
    <email>$UNAME@foo.com</email>
    <administrator>false</administrator>
    <password>testing</password>
</user> 
EOF
}
generate_user |
curl -X PUT --data-binary @- $1/cmp/signup -H "Content-Type: text/xml"