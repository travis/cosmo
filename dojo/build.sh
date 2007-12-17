#!/bin/bash
DOJO_VERSION="release-1.0.2"
if [ ! -d "$DOJO_VERSION" ]; then
    svn co http://svn.dojotoolkit.org/dojo/tags/$DOJO_VERSION
#    cd $DOJO_VERSION/buildscripts
#    patch -p0 < ../../0.4.3-buildUtil.js.patch
    ant fix-config
fi

cp cosmo-pim.js $DOJO_VERSION/dojo
cp cosmo-login.js $DOJO_VERSION/dojo/

cd $DOJO_VERSION/util/buildscripts
if [ "$1" == "release" ]; then
    ant -Ddocless=true -Dprofile=../../../cosmo clean release
else
    ./build.sh profile=standard action=clean,release
#    ant -Ddocless=true -Dprofile=core clean release
fi

cd ..

gzip -9c ../release/dojo/dojo/dojo.js > ../release/dojo/dojo/dojo.js.gzip-compressed
gzip -9c ../release/dojo/dojo/cosmo-pim.js > ../release/dojo/dojo/cosmo-pim.js.gzip-compressed
gzip -9c ../release/dojo/dojo/cosmo-login.js > ../release/dojo/dojo/cosmo-login.js.gzip-compressed
