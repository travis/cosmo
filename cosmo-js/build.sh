#!/bin/bash
DOJO_VERSION="release-1.1.0"
if [ ! -d dojo-src ]; then
    svn co http://svn.dojotoolkit.org/src/tags/$DOJO_VERSION dojo-src
fi

cd dojo-src/util/buildscripts
if [ "$1" == "release" ]; then
    ./build.sh profile=../../../../cosmo action=clean,release
else
    ./build.sh profile=../../../../cosmo-dev action=clean,release
fi

cd ../../../

if [ ! -d release ]; then
    mkdir release
else
    rm -rf release/*
fi

mv dojo-src/release/dojo/* release

gzip -9c release/dojo/dojo.js > release/dojo/dojo.js.gzip-compressed.js
gzip -9c release/cosmo/pim.js > release/cosmo/pim.js.gzip-compressed.js
gzip -9c release/cosmo/login.js > release/cosmo/login.js.gzip-compressed.js
gzip -9c release/cosmo/userlist.js > release/cosmo/userlist.js.gzip-compressed.js

