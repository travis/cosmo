#!/bin/bash
DOJO_VERSION="release-1.0.2"
if [ ! -d "$DOJO_VERSION" ]; then
    svn co http://svn.dojotoolkit.org/dojo/tags/$DOJO_VERSION
    patch -p0 -d release-1.0.2 < dojo.patch 
fi

cd $DOJO_VERSION/util/buildscripts
if [ "$1" == "release" ]; then
    ./build.sh profile=../../../../cosmo action=clean,release
else
    ./build.sh profile=../../../../cosmo-dev action=clean,release
fi

cd ..

gzip -9c ../release/dojo/dojo/dojo.js > ../release/dojo/dojo/dojo.js.gzip-compressed
gzip -9c ../release/dojo/cosmo/pim.js > ../release/dojo/cosmo/pim.js.gzip-compressed
gzip -9c ../release/dojo/cosmo/login.js > ../release/dojo/cosmo/login.js.gzip-compressed

