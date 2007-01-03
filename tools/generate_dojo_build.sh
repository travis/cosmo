#!/bin/bash
cd ../src/main/webapp/js
#mkdir build
cd build
#svn co http://svn.dojotoolkit.org/dojo/tags/release-0.4.1
cd release-0.4.1/buildscripts
cp ../../../../../../../tools/cosmo.profile.js profiles
rm -rf ../../../lib/dojo 
ant -Dprofile=cosmo clean release 
mv ../release/dojo/ ../../../lib/
