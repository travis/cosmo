#!/bin/bash
if [ -z "$5" ]
then
    echo "usage: "
    echo "$0 [username] [password] [server] [import calendar name] [import calendar url]"
    exit 1
fi
curl $5 > caldata.tmp
curl -u $1:$2 -X POST -H "Content-Type:text/calendar" -T caldata.tmp http://$3/chandler/atom/user/$1/import/$4
rm caldata.tmp
