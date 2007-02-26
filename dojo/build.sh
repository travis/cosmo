#!/bin/bash

if [ ! -d "release-0.4.1" ]; then
    svn co http://svn.dojotoolkit.org/dojo/tags/release-0.4.1
    cd release-0.4.1/buildscripts
    ant fix-config
fi
cd release-0.4.1/buildscripts
if [ "$1" == "release" ]; then
    ant -Ddocless=true -Dprofile=../../../cosmo clean release
else
    ant -Ddocless=true -Dprofile=core clean release
fi
gzip -c ../release/dojo/dojo.js > ../release/dojo/dojo.js.gzip-compressed

