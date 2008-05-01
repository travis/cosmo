#!/bin/bash
# Given a version number, grabs the appropriate tag from svn, builds binaries and pushes them to downloads server
# NOTE: This script currently requires access (password or keybased) and knowledge of the BUILD_SERVER ssh key passphrase
# TODO: Add permissions checking, document necessary machine access
NO_ARGS=0 
E_OPTERROR=65

if [ $# -eq "$NO_ARGS" ]  # Script invoked with no command-line args?
then
  echo "Usage: `basename $0` tagname"
  exit $E_OPTERROR        # Exit and explain usage, if no argument(s) given.
fi

BUILD_SERVER=tutu2.osafoundation.org
BUILD_HOME=/home/builder/release
JAVA_HOME=/usr/lib/jvm/java-6-sun
VERSION_STRING=$1
SVN_TAG=rel_$VERSION_STRING
RELEASE_HOME=$BUILD_HOME/$SVN_TAG
CHANDLER_WAR_FILENAME=chandler-webapp-$VERSION_STRING.tar.gz
OSAF_BUNDLE_FILENAME=osaf-server-bundle-$VERSION_STRING.tar.gz

CHANDLER_WAR=$RELEASE_HOME/cosmo/dist/$CHANDLER_WAR_FILENAME
OSAF_BUNDLE=$RELEASE_HOME/snarf/dist/$OSAF_BUNDLE_FILENAME

DOWNLOADS_SERVER=downloads.osafoundation.org
DOWNLOADS_HOME=/www/downloads.osafoundation.org/cosmo/releases/$VERSION_STRING

ssh builder@$DOWNLOADS_SERVER "mkdir $DOWNLOADS_HOME"
ssh builder@$BUILD_SERVER "cd $BUILD_HOME; export JAVA_HOME=$JAVA_HOME; svn co http://svn.osafoundation.org/server/cosmo/tags/$SVN_TAG; cd $SVN_TAG/cosmo; mvn -Prelease clean package; cd ../snarf; mvn -Prelease clean package; cd ../;"
ssh builder@$BUILD_SERVER "eval \`ssh-agent -s\`; echo \$SSH_AUTH_SOCK; ssh-add; scp $CHANDLER_WAR $OSAF_BUNDLE $DOWNLOADS_SERVER:$DOWNLOADS_HOME"
ssh builder@$DOWNLOADS_SERVER "python distIndex.py SR $VERSION_STRING $VERSION_STRING $OSAF_BUNDLE_FILENAME $CHANDLER_WAR_FILENAME"

