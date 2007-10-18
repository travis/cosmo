#!/bin/bash
DOJO_VERSION="release-0.4.3"
if [ ! -d "$DOJO_VERSION" ]; then
    svn co http://svn.dojotoolkit.org/dojo/tags/$DOJO_VERSION
    cd $DOJO_VERSION/buildscripts
    patch -p0 < ../../0.4.3-buildUtil.js.patch
    ant fix-config
fi
cd $DOJO_VERSION/buildscripts
if [ "$1" == "release" ]; then
    ant -Ddocless=true -Dprofile=../../../cosmo clean release
else
    ant -Ddocless=true -Dprofile=core clean release
fi
gzip -9c ../release/dojo/dojo.js > ../release/dojo/dojo.js.gzip-compressed
