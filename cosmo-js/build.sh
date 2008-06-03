#!/bin/bash
DOJO_VERSION="release-1.1.1"
if [ ! -d dojo-src ]; then
    svn co http://svn.dojotoolkit.org/src/tags/$DOJO_VERSION dojo-src
fi

cssBuild=""
case "$1" in
    "release")
        profile=cosmo
        cssBuild="cssOptimize=comments"
         ;;
    "widgets")
        profile=cosmo-widgets
         ;;
    *)
        profile=cosmo-dev
         ;;
esac

##### build dojo files #####
cd dojo-src/util/buildscripts
./build.sh profile=../../../../$profile action=clean,release $cssBuild

cd ../../../

if [ ! -d release ]; then
    mkdir release
else
    rm -rf release/*
fi

mv dojo-src/release/dojo/* release

files="release/dojo/dojo.js\
       release/cosmo/pim.js\
       release/cosmo/login.js\
       release/cosmo/userlist.js"

##### move widgets layer, if appropriate #####
if [ "$1" == "widgets" -a "$WIDGETS_HOME" ]; then
    if [ -d $WIDGETS_HOME/widgets_build ]; then
        rm -rf $WIDGETS_HOME/widgets_build
    fi
    mv release/widgets_build $WIDGETS_HOME
    # add widgets.js to list of files to be gzipped
    files="$files $WIDGETS_HOME/widgets_build/widgets.js"
fi

##### gzip selected files #####
for f in $files
do
    if [ -f $f ]; then
        gzip -9c $f > $f.gzip-compressed.js
    else
        echo "File not found: $f"
    fi
done
